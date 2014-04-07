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
import com.altamiracorp.securegraph.Authorizations;
import com.altamiracorp.securegraph.Edge;
import com.altamiracorp.securegraph.Graph;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.mutation.ElementMutation;
import com.altamiracorp.securegraph.type.GeoPoint;
import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;

import static com.altamiracorp.lumify.core.model.ontology.OntologyLumifyProperties.CONCEPT_TYPE;
import static com.altamiracorp.lumify.core.model.properties.EntityLumifyProperties.GEO_LOCATION;
import static com.altamiracorp.lumify.core.model.properties.LumifyProperties.TITLE;
import static com.altamiracorp.lumify.facebook.FacebookConstants.*;

public class FacebookPost {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(FacebookBolt.class);
    private static final String PROCESS = FacebookPost.class.getName();
    private LumifyVisibility lumifyVisibility;

    protected ArtifactExtractedInfo processPostArtifact(JSONObject post) throws Exception {
        lumifyVisibility = new LumifyVisibility();
        //extract knowledge from post
        String message = post.getString(MESSAGE);
        Long name_uid = post.getLong(AUTHOR_UID);
        String author_uid = name_uid.toString();
        Date time = new Date(post.getLong(TIMESTAMP) * 1000);
        //use extracted information
        String rowKey = RowKeyHelper.buildSHA256KeyString(post.toString().getBytes());

        ArtifactExtractedInfo artifactExtractedInfo = new ArtifactExtractedInfo();
        artifactExtractedInfo.setText(message);
        artifactExtractedInfo.setSource(FACEBOOK);
        artifactExtractedInfo.setRaw(post.toString().getBytes());
        artifactExtractedInfo.setMimeType("text/plain");
        artifactExtractedInfo.setRowKey(rowKey);
        artifactExtractedInfo.setConceptType(FACEBOOK_POST);
        artifactExtractedInfo.setAuthor(author_uid);
        artifactExtractedInfo.setDate(time);
        artifactExtractedInfo.setProcess(PROCESS);
        if (message.length() == 0) {
            //TODO: perform the query for the image and create the artifact/vertex and connect
            artifactExtractedInfo.setTitle("Facebook Image Post");
        } else if (message.length() > 140) {
            String shortTitle = message.substring(0, 137) + "...";
            artifactExtractedInfo.setTitle(shortTitle);
        } else {
            artifactExtractedInfo.setTitle(message);
        }

        //write artifact with extracted info to accumulo and create entity
        return artifactExtractedInfo;
    }

    protected Vertex processPostVertex(JSONObject post, Vertex posting, Graph graph, AuditRepository auditRepository, OntologyRepository ontologyRepository, User user, Authorizations authorizations) throws Exception {
        Long name_uid = post.getLong(AUTHOR_UID);
        String author_uid = name_uid.toString();
        LOGGER.info("Saving Facebook post to accumulo and as graph vertex: ", posting.getId());
        Concept profileConcept = ontologyRepository.getConceptByIRI(FACEBOOK_PROFILE);
        String profileConceptId = profileConcept.getTitle();

        //create entities for each of the ids tagged or author and the relationships
        Vertex authorVertex;
        String authorVid = generateUserVertexId(author_uid);
        Vertex queryVertex = graph.getVertex(authorVid, authorizations);
        if (queryVertex == null) {
            ElementMutation<Vertex> authorBuilder = graph.prepareVertex(authorVid, lumifyVisibility.getVisibility(), authorizations);
            PROFILE_ID.setProperty(authorBuilder, author_uid, lumifyVisibility.getVisibility());
            TITLE.setProperty(authorBuilder, author_uid, lumifyVisibility.getVisibility());
            CONCEPT_TYPE.setProperty(authorBuilder, profileConceptId, lumifyVisibility.getVisibility());
            authorVertex = authorBuilder.save();
            auditRepository.auditVertexElementMutation(AuditAction.UPDATE, authorBuilder, authorVertex, PROCESS, user, lumifyVisibility.getVisibility());
        } else {
            authorVertex = queryVertex;
        }
        Edge edge = graph.addEdge(authorVertex, posting, POSTED_RELATIONSHIP, lumifyVisibility.getVisibility(), authorizations);
        auditRepository.auditRelationship(AuditAction.CREATE, posting, authorVertex, edge, PROCESS, "", user, lumifyVisibility.getVisibility());
        graph.flush();

        if (post.get(TAGGED_UIDS) instanceof JSONObject) {
            Iterator tagged = post.getJSONObject(TAGGED_UIDS).keys();
            while (tagged.hasNext()) {
                String next = tagged.next().toString();
                Vertex taggedVertex;
                String taggedVid = generateUserVertexId(next);
                Vertex nextQueryVertex = graph.getVertex(taggedVid, authorizations);
                if (nextQueryVertex == null) {
                    ElementMutation<Vertex> taggedBuilder = graph.prepareVertex(taggedVid, lumifyVisibility.getVisibility(), authorizations);
                    PROFILE_ID.setProperty(taggedBuilder, next, lumifyVisibility.getVisibility());
                    TITLE.setProperty(taggedBuilder, next, lumifyVisibility.getVisibility());
                    CONCEPT_TYPE.setProperty(taggedBuilder, profileConceptId, lumifyVisibility.getVisibility());
                    taggedVertex = taggedBuilder.save();
                    auditRepository.auditVertexElementMutation(AuditAction.UPDATE, taggedBuilder, taggedVertex, PROCESS, user, lumifyVisibility.getVisibility());
                } else {
                    taggedVertex = nextQueryVertex;
                }
                Edge mentionedEdge = graph.addEdge(posting, taggedVertex, MENTIONED_RELATIONSHIP, lumifyVisibility.getVisibility(), authorizations);
                auditRepository.auditRelationship(AuditAction.CREATE, posting, taggedVertex, mentionedEdge, PROCESS, "", user, lumifyVisibility.getVisibility());
                graph.flush();
            }
        }

        if (post.has(COORDS) && !post.getJSONObject(COORDS).equals(JSONObject.NULL)) {
            JSONObject coordinates = post.getJSONObject(COORDS);
            GeoPoint geo = new GeoPoint(coordinates.getDouble("latitude"), coordinates.getDouble("longitude"));
            ElementMutation<Vertex> postingMutation = posting.prepareMutation();
            GEO_LOCATION.setProperty(postingMutation, geo, lumifyVisibility.getVisibility());
            auditRepository.auditVertexElementMutation(AuditAction.UPDATE, postingMutation, posting, PROCESS, user, lumifyVisibility.getVisibility());
            posting = postingMutation.save();
        }

        return posting;
    }

    public String generateUserVertexId(String profileId) {
        return FACEBOOK_VERTEX_ID + profileId;
    }
}
