package com.altamiracorp.lumify.facebook;

import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.LabelName;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.securegraph.*;
import com.altamiracorp.securegraph.type.GeoPoint;
import com.beust.jcommander.internal.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class FacebookUser {
    private static final String NAME = "name";
    private static final String UID = "uid";
    private static final String SEX = "sex";
    private static final String GENDER = "gender";
    private static final String EMAIL = "email";
    private static final String EMAIL_ADDRESS = "emailAddress";
    private static final String EMAIL_RELATIONSHIP = "personHasEmailAddress";
    private static final String BIRTHDAY_DATE = "birthday_date";
    private static final String BIRTHDAY = "birthday";
    private static final String PIC = "pic";
    private static final String BIRTHDAY_FORMAT = "MM/dd";
    private static final String USERNAME = "username";
    private static final String PROFILE_ID = "profileId";
    private static final String COORDS = "coords";
    private static final String FACEBOOK_PROFILE_IMAGE = "facebookProfileImage";
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(FacebookBolt.class);
    private static final String PROCESS = FacebookUser.class.getName();
    private FacebookBolt facebookBolt = new FacebookBolt();
    ;

    public Vertex process(JSONObject userJson, Graph graph, AuditRepository auditRepository, OntologyRepository ontologyRepository, User user) throws ParseException {
        //TODO set visibility
        Visibility visibility = new Visibility("");
        //create entity for each user with the properties if one doesn't already exist
        String name = userJson.getString(NAME);
        Long name_uid = userJson.getLong(UID);
        String username = userJson.getString(USERNAME);
        Concept emailConcept = ontologyRepository.getConceptByName(EMAIL_ADDRESS, user);

        Vertex userVertex;
        Iterator<Vertex> verticesIterator = graph.query(user.getAuthorizations()).has(PropertyName.TITLE.toString(), name_uid.toString()).vertices().iterator();
        if (!verticesIterator.hasNext()) {
            Iterator<Vertex> verticesByNameIterator = graph.query(user.getAuthorizations()).has(PropertyName.TITLE.toString(), name.toString()).vertices().iterator();
            if (!verticesByNameIterator.hasNext()) {
                LOGGER.error("Could not find user in system, with given profile_id.");
                throw new RuntimeException();
            } else {
                // TODO what happens if verticesByNameIterator contains multiple users
                userVertex = verticesByNameIterator.next();
                LOGGER.info("Vertex previously processed: ", userVertex.getId());
                return userVertex;
            }
        } else {
            // TODO what happens if verticesIterator contains multiple users
            userVertex = verticesIterator.next();
        }
        List<String> modifiedProperties = Lists.newArrayList
                (PropertyName.TITLE.toString(), PropertyName.CONCEPT_TYPE.toString(), PropertyName.DISPLAY_NAME.toString(), PROFILE_ID);


        userVertex.setProperty(PropertyName.DISPLAY_NAME.toString(), username, visibility);
        userVertex.setProperty(PropertyName.TITLE.toString(), name, visibility);

        //get relationships for vertex and write audit message for each post

        if (userJson.has(SEX) && !userJson.getString(SEX).equals(JSONObject.NULL)) {
            String gender = userJson.getString(SEX);
            userVertex.setProperty(GENDER, gender, visibility);
            modifiedProperties.add(GENDER);
        }

        if (userJson.has(EMAIL) && !userJson.getString(EMAIL).equals(JSONObject.NULL)) {
            String email = userJson.getString(EMAIL);
            Vertex emailVertex;

            Iterator<Vertex> emailIterator = graph.query(user.getAuthorizations()).has(EMAIL_ADDRESS, email).vertices().iterator();
            if (!emailIterator.hasNext()) {
                emailVertex = graph.addVertex(visibility);
                emailVertex.setProperty(PropertyName.TITLE.toString(), email, visibility);
                emailVertex.setProperty(PropertyName.CONCEPT_TYPE.toString(), emailConcept.getId(), visibility);
                auditRepository.auditEntity(AuditAction.CREATE.toString(), emailVertex.getId(), userVertex.getId().toString(), email, emailConcept.getId().toString(), PROCESS, "", user);
                auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), emailVertex, PropertyName.TITLE.toString(), PROCESS, "", user);

            } else {
                // TODO what happens if emailIterator contains multiple users
                emailVertex = emailIterator.next();
            }
            graph.addEdge(userVertex, emailVertex, EMAIL_RELATIONSHIP, visibility);
        }

        if (userJson.has(COORDS) && !userJson.get(COORDS).equals(JSONObject.NULL)) {
            JSONObject coordinates = userJson.getJSONObject(COORDS);
            GeoPoint geo = new GeoPoint(coordinates.getDouble("latitude"), coordinates.getDouble("longitude"));
            userVertex.setProperty(PropertyName.GEO_LOCATION.toString(), geo, visibility);
            modifiedProperties.add(PropertyName.GEO_LOCATION.toString());
        }

        if (userJson.has(BIRTHDAY_DATE) && (userJson.get(BIRTHDAY_DATE) instanceof String)) {
            String birthday_date = userJson.getString(BIRTHDAY_DATE);
            SimpleDateFormat birthdayFormat = new SimpleDateFormat(BIRTHDAY_FORMAT);
            birthdayFormat.setLenient(true);
            if (birthday_date.length() > BIRTHDAY_FORMAT.length()) {
                birthdayFormat = new SimpleDateFormat(BIRTHDAY_FORMAT + "/yyyy");
            }
            Date birthday = birthdayFormat.parse(birthday_date);
            userVertex.setProperty(BIRTHDAY.toString(), birthday.getTime(), visibility);
            modifiedProperties.add(BIRTHDAY);
        }
        //create and save profile picture
        for (String property : modifiedProperties) {
            auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), userVertex, property, PROCESS, "", user);
        }
        return userVertex;
    }

    public ArtifactExtractedInfo createProfilePhotoArtifact(JSONObject userJson, Vertex userVertex) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String picUrl = userJson.get(PIC).toString();
        String facebookPictureTitle = userJson.getString(NAME) + " Facebook Profile Picture";
        String facebookPictureSource = "Facebook profile picture";
        try {
            URL url = new URL(picUrl);
            InputStream is = url.openStream();
            IOUtils.copy(is, os);
            byte[] raw = os.toByteArray();
            ArtifactRowKey build = ArtifactRowKey.build(raw);
            String rowKey = build.toString();

            ArtifactExtractedInfo artifactExtractedInfo = new ArtifactExtractedInfo();
            artifactExtractedInfo.setMimeType("image/png");
            artifactExtractedInfo.setRowKey(rowKey);
            artifactExtractedInfo.setConceptType(FACEBOOK_PROFILE_IMAGE);
            artifactExtractedInfo.setTitle(facebookPictureTitle);
            artifactExtractedInfo.setSource(facebookPictureSource);
            artifactExtractedInfo.setProcess(PROCESS);
            if (raw.length > Artifact.MAX_SIZE_OF_INLINE_FILE) {
                String path = "/lumify/artifacts/raw/" + rowKey;
                FSDataOutputStream rawFile = facebookBolt.getFileSystem().create(new Path(path));
                try {
                    rawFile.write(raw);
                } finally {
                    rawFile.close();
                }
                artifactExtractedInfo.setRawHdfsPath(path);
            } else {
                artifactExtractedInfo.setRaw(raw);
            }
            return artifactExtractedInfo;
        } catch (IOException e) {
            LOGGER.warn("Failed to create image for vertex: %s", userVertex.getId());
            new IOException(e);
        }
        return null;
    }

    protected void createProfilePhotoVertex(Vertex pictureVertex, Vertex userVertex, Graph graph, AuditRepository auditRepository, User user) {
        //TODO set visibility
        Visibility visibility = new Visibility("");
        List<String> modifiedProperties = new ArrayList<String>();
        userVertex.setProperty(PropertyName.GLYPH_ICON.toString(), "/artifact/" + pictureVertex.getId() + "/raw", visibility);
        pictureVertex.setProperty(PropertyName.GLYPH_ICON.toString(), "/artifact/" + pictureVertex.getId() + "/raw", visibility);
        modifiedProperties.add(PropertyName.GLYPH_ICON.toString());
        String labelDisplay = LabelName.HAS_IMAGE.toString();

        Iterator<Edge> edges = userVertex.getEdges(pictureVertex, Direction.IN, labelDisplay, user.getAuthorizations()).iterator();
        if (!edges.hasNext()) {
            graph.addEdge(userVertex, pictureVertex, labelDisplay, visibility);
        }

        auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), userVertex, PropertyName.GLYPH_ICON.toString(),
                PROCESS, "", user);
        auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), pictureVertex, PropertyName.GLYPH_ICON.toString(),
                PROCESS, "", user);
        auditRepository.auditRelationships(AuditAction.CREATE.toString(), userVertex, pictureVertex, labelDisplay, PROCESS, "", user);
        LOGGER.info("Saving Facebook picture to accumulo and as graph vertex: %s", pictureVertex.getId());
    }

    public void setFacebookBolt(FacebookBolt bolt) {
        this.facebookBolt = bolt;
    }
}
