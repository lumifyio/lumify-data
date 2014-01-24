package com.altamiracorp.lumify.facebook;

import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.RowKeyHelper;
import com.altamiracorp.securegraph.*;
import com.altamiracorp.securegraph.type.GeoPoint;
import com.beust.jcommander.internal.Lists;
import org.apache.commons.io.IOUtils;
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

import static com.altamiracorp.lumify.facebook.FacebookConstants.*;

public class FacebookUser {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(FacebookBolt.class);
    private static final String PROCESS = FacebookUser.class.getName();
    private FacebookBolt facebookBolt = new FacebookBolt();

    public Vertex process(JSONObject userJson, Graph graph, AuditRepository auditRepository, OntologyRepository ontologyRepository, User user) throws ParseException {
        //TODO set visibility
        Visibility visibility = new Visibility("");
        //create entity for each user with the properties if one doesn't already exist
        String name = userJson.getString(NAME);
        Long name_uid = userJson.getLong(UID);
        String username = userJson.getString(USERNAME);
        Concept emailConcept = ontologyRepository.getConceptByName(EMAIL_ADDRESS);

        Vertex userVertex;
        String userVid = generateUserVertexId(name_uid.toString());
        Vertex queryVertex = graph.getVertex(userVid, user.getAuthorizations());
        if (queryVertex == null) {
            LOGGER.error("Could not find user in system, with given profile_id.");
            throw new RuntimeException();
        } else {
            Iterable <Object> titles = queryVertex.getPropertyValues(PropertyName.TITLE.toString());
            for (Object title : titles) {
                if (title.toString().equals(name)) {
                    userVertex = queryVertex;
                    LOGGER.info("Vertex previously processed: ", userVertex.getId());
                    return userVertex;
                }
            }
            userVertex = queryVertex;
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
            Vertex emailVertex = null;

            Iterator<Vertex> emailIterator = graph.query(user.getAuthorizations()).has(EMAIL_ADDRESS, email).vertices().iterator();
            Vertex queryEmailVertex = emailIterator.next();
            if (queryEmailVertex == null) {
                VertexBuilder emailBuilder;
                emailBuilder = graph.prepareVertex(visibility, user.getAuthorizations());
                emailBuilder.setProperty(PropertyName.TITLE.toString(), email, visibility);
                emailBuilder.setProperty(PropertyName.CONCEPT_TYPE.toString(), emailConcept.getId(), visibility);
                emailVertex = emailBuilder.save();

                auditRepository.auditEntity(AuditAction.CREATE.toString(), emailVertex.getId(), userVertex.getId().toString(), email, emailConcept.getId().toString(), PROCESS, "", user);
                auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), emailVertex, PropertyName.TITLE.toString(), PROCESS, "", user);

            } else {
                while (queryEmailVertex != null) {
                    Iterable <Object> titles = queryVertex.getPropertyValues(PropertyName.TITLE.toString());
                    for (Object title : titles) {
                        if (title.toString().equals(name)) {
                            emailVertex = queryEmailVertex;
                            break;
                        }
                    }
                    queryEmailVertex = emailIterator.next();
                }
            }
            graph.addEdge(userVertex, emailVertex, EMAIL_RELATIONSHIP, visibility, user.getAuthorizations());
            graph.flush();
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
            String rowKey = RowKeyHelper.buildSHA256KeyString(raw);

            ArtifactExtractedInfo artifactExtractedInfo = new ArtifactExtractedInfo();
            artifactExtractedInfo.setMimeType("image/png");
            artifactExtractedInfo.setRowKey(rowKey);
            artifactExtractedInfo.setConceptType(FACEBOOK_PROFILE_IMAGE);
            artifactExtractedInfo.setTitle(facebookPictureTitle);
            artifactExtractedInfo.setSource(facebookPictureSource);
            artifactExtractedInfo.setProcess(PROCESS);
            artifactExtractedInfo.setRaw(raw);
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
        String labelDisplay = ENTITY_HAS_IMAGE_PROFILE_PHOTO;

        Iterator<Edge> edges = userVertex.getEdges(pictureVertex, Direction.IN, labelDisplay, user.getAuthorizations()).iterator();
        if (!edges.hasNext()) {
            graph.addEdge(userVertex, pictureVertex, labelDisplay, visibility, user.getAuthorizations());
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

    public String generateUserVertexId (String profileId) {
        return FACEBOOK_VERTEX_ID + profileId;
    }
}
