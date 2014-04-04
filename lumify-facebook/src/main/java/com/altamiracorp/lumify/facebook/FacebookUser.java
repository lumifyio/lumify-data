package com.altamiracorp.lumify.facebook;

import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.security.LumifyVisibility;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.RowKeyHelper;
import com.altamiracorp.securegraph.*;
import com.altamiracorp.securegraph.mutation.ElementMutation;
import com.altamiracorp.securegraph.type.GeoPoint;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import static com.altamiracorp.lumify.core.model.ontology.OntologyLumifyProperties.CONCEPT_TYPE;
import static com.altamiracorp.lumify.core.model.properties.EntityLumifyProperties.GEO_LOCATION;
import static com.altamiracorp.lumify.core.model.properties.LumifyProperties.*;
import static com.altamiracorp.lumify.facebook.FacebookConstants.*;

public class FacebookUser {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(FacebookBolt.class);
    private static final String PROCESS = FacebookUser.class.getName();
    private LumifyVisibility lumifyVisibility;

    public Vertex process(JSONObject userJson, Graph graph, AuditRepository auditRepository, OntologyRepository ontologyRepository, User user, Authorizations authorizations) throws ParseException {
        lumifyVisibility = new LumifyVisibility();
        //create entity for each user with the properties if one doesn't already exist
        String name = userJson.getString(NAME);
        Long name_uid = userJson.getLong(UID);
        String username = userJson.getString(USERNAME);
        Concept emailConcept = ontologyRepository.getConceptByIRI(EMAIL_ADDRESS);

        Vertex userVertex;
        String userVid = generateUserVertexId(name_uid.toString());
        Vertex queryVertex = graph.getVertex(userVid, authorizations);
        if (queryVertex == null) {
            LOGGER.error("Could not find user in system, with given profile_id.");
            throw new RuntimeException();
        } else {
            Iterable<String> titles = TITLE.getPropertyValues(queryVertex);
            for (String title : titles) {
                if (title.equals(name)) {
                    userVertex = queryVertex;
                    LOGGER.info("Vertex previously processed: ", userVertex.getId());
                    return userVertex;
                }
            }
            userVertex = queryVertex;
        }

        ElementMutation<Vertex> userVertexMutation = userVertex.prepareMutation();
        DISPLAY_NAME.setProperty(userVertexMutation, username, lumifyVisibility.getVisibility());
        TITLE.setProperty(userVertexMutation, name, lumifyVisibility.getVisibility());

        //get relationships for vertex and write audit message for each post

        if (userJson.has(SEX) && !userJson.getString(SEX).equals(JSONObject.NULL)) {
            String gender = userJson.getString(SEX);
            GENDER.setProperty(userVertexMutation, gender, lumifyVisibility.getVisibility());
        }

        if (userJson.has(EMAIL) && !userJson.getString(EMAIL).equals(JSONObject.NULL)) {
            String email = userJson.getString(EMAIL);
            Vertex emailVertex = null;

            Iterator<Vertex> emailIterator = graph.query(authorizations).has(EMAIL_ADDRESS, email).vertices().iterator();
            Vertex queryEmailVertex = emailIterator.next();
            if (queryEmailVertex == null) {
                ElementMutation<Vertex> emailBuilder = graph.prepareVertex(lumifyVisibility.getVisibility(), authorizations);
                TITLE.setProperty(emailBuilder, email, lumifyVisibility.getVisibility());
                CONCEPT_TYPE.setProperty(emailBuilder, emailConcept.getTitle(), lumifyVisibility.getVisibility());
                emailVertex = emailBuilder.save();
                auditRepository.auditVertexElementMutation(AuditAction.UPDATE, emailBuilder, emailVertex, PROCESS, user, lumifyVisibility.getVisibility());
            } else {
                while (queryEmailVertex != null) {
                    Iterable<String> titles = TITLE.getPropertyValues(queryVertex);
                    for (String title : titles) {
                        if (title.equals(name)) {
                            emailVertex = queryEmailVertex;
                            break;
                        }
                    }
                    queryEmailVertex = emailIterator.next();
                }
            }
            Edge edge = graph.addEdge(userVertex, emailVertex, EMAIL_RELATIONSHIP, lumifyVisibility.getVisibility(), authorizations);
            auditRepository.auditRelationship(AuditAction.CREATE, userVertex, emailVertex, edge, PROCESS, "", user, lumifyVisibility.getVisibility());
            graph.flush();
        }

        if (userJson.has(COORDS) && !userJson.get(COORDS).equals(JSONObject.NULL)) {
            JSONObject coordinates = userJson.getJSONObject(COORDS);
            GeoPoint geo = new GeoPoint(coordinates.getDouble("latitude"), coordinates.getDouble("longitude"));
            GEO_LOCATION.setProperty(userVertexMutation, geo, lumifyVisibility.getVisibility());
        }

        if (userJson.has(BIRTHDAY_DATE) && (userJson.get(BIRTHDAY_DATE) instanceof String)) {
            String birthday_date = userJson.getString(BIRTHDAY_DATE);
            SimpleDateFormat birthdayFormat = new SimpleDateFormat(BIRTHDAY_FORMAT);
            birthdayFormat.setLenient(true);
            if (birthday_date.length() > BIRTHDAY_FORMAT.length()) {
                birthdayFormat = new SimpleDateFormat(BIRTHDAY_FORMAT + "/yyyy");
            }
            Date birthday = birthdayFormat.parse(birthday_date);
            BIRTHDAY.setProperty(userVertexMutation, birthday, lumifyVisibility.getVisibility());
        }
        //create and save profile picture
        auditRepository.auditVertexElementMutation(AuditAction.UPDATE, userVertexMutation, userVertex, PROCESS, user, lumifyVisibility.getVisibility());
        userVertex = userVertexMutation.save();
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

    protected void createProfilePhotoVertex(Vertex pictureVertex, Vertex userVertex, Graph graph, AuditRepository auditRepository, OntologyRepository ontologyRepository, User user, Authorizations authorizations) {
        ElementMutation<Vertex> userVertexMutation = userVertex.prepareMutation();
        ElementMutation<Vertex> pictureVertexMutation = pictureVertex.prepareMutation();
        // TO-DO: Change GLYPH_ICON to ENTITY_IMAGE_URL
        userVertexMutation.setProperty(GLYPH_ICON.getKey(), new Text("/artifact/" + pictureVertex.getId() + "/raw", TextIndexHint.EXACT_MATCH), lumifyVisibility.getVisibility());
        pictureVertexMutation.setProperty(GLYPH_ICON.getKey(), new Text("/artifact/" + pictureVertex.getId() + "/raw", TextIndexHint.EXACT_MATCH), lumifyVisibility.getVisibility());

        auditRepository.auditVertexElementMutation(AuditAction.UPDATE, userVertexMutation, userVertex, PROCESS, user, lumifyVisibility.getVisibility());
        auditRepository.auditVertexElementMutation(AuditAction.UPDATE, pictureVertexMutation, pictureVertex, PROCESS, user, lumifyVisibility.getVisibility());
        userVertex = userVertexMutation.save();
        pictureVertex = pictureVertexMutation.save();

        String labelDisplay = ontologyRepository.getDisplayNameForLabel(ENTITY_HAS_IMAGE_PROFILE_PHOTO);

        Iterator<Edge> edges = userVertex.getEdges(pictureVertex, Direction.IN, labelDisplay, authorizations).iterator();
        if (!edges.hasNext()) {
            Edge edge = graph.addEdge(userVertex, pictureVertex, ENTITY_HAS_IMAGE_PROFILE_PHOTO, lumifyVisibility.getVisibility(), authorizations);
            auditRepository.auditRelationship(AuditAction.CREATE, userVertex, pictureVertex, edge, PROCESS, "", user, lumifyVisibility.getVisibility());
        }

        LOGGER.info("Saving Facebook picture to accumulo and as graph vertex: %s", pictureVertex.getId());
    }

    public String generateUserVertexId(String profileId) {
        return FACEBOOK_VERTEX_ID + profileId;
    }
}
