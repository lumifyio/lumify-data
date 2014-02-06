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
import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;

import static com.altamiracorp.lumify.facebook.FacebookConstants.*;

public class FacebookPost {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(FacebookBolt.class);
    private static final String PROCESS = FacebookPost.class.getName();


    protected ArtifactExtractedInfo processPostArtifact(JSONObject post) throws Exception {
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

    protected Vertex processPostVertex(JSONObject post, Vertex posting, Graph graph, AuditRepository auditRepository, OntologyRepository ontologyRepository, User user) throws Exception {
        //TODO set visibility
        Visibility visibility = new Visibility("");
        Long name_uid = post.getLong(AUTHOR_UID);
        String author_uid = name_uid.toString();
        LOGGER.info("Saving Facebook post to accumulo and as graph vertex: ", posting.getId());
        Concept profileConcept = ontologyRepository.getConceptByName(FACEBOOK_PROFILE);
        String profileConceptId = profileConcept.getId().toString();

        //create entities for each of the ids tagged or author and the relationships
        Vertex authorVertex;
        String authorVid = generateUserVertexId(author_uid);
        Vertex queryVertex = graph.getVertex(authorVid, user.getAuthorizations());
        if (queryVertex == null) {
            ElementMutation<Vertex> authorBuilder = graph.prepareVertex(authorVid, visibility, user.getAuthorizations());
            authorBuilder.setProperty(PROFILE_ID, new Text(author_uid, TextIndexHint.EXACT_MATCH), visibility);
            authorBuilder.setProperty(PropertyName.TITLE.toString(), new Text(author_uid), visibility);
            authorBuilder.setProperty(PropertyName.CONCEPT_TYPE.toString(), new Text(profileConceptId, TextIndexHint.EXACT_MATCH), visibility);
            authorVertex = authorBuilder.save();
            auditRepository.auditVertexElementMutation(authorBuilder, authorVertex, PROCESS, user);
        } else {
            authorVertex = queryVertex;
        }
        graph.addEdge(authorVertex, posting, POSTED_RELATIONSHIP, visibility, user.getAuthorizations());
        String postedRelationshipLabelDisplayName = ontologyRepository.getDisplayNameForLabel(POSTED_RELATIONSHIP);
        auditRepository.auditRelationship(AuditAction.CREATE, posting, authorVertex, postedRelationshipLabelDisplayName, PROCESS, "", user);
        graph.flush();

        if (post.get(TAGGEED_UIDS) instanceof JSONObject) {
            Iterator tagged = post.getJSONObject(TAGGEED_UIDS).keys();
            while (tagged.hasNext()) {
                String next = tagged.next().toString();
                Vertex taggedVertex;
                String taggedVid = generateUserVertexId(next);
                Vertex nextQueryVertex = graph.getVertex(taggedVid, user.getAuthorizations());
                if (nextQueryVertex == null) {
                    ElementMutation<Vertex> taggedBuilder = graph.prepareVertex(taggedVid, visibility, user.getAuthorizations());
                    taggedBuilder.setProperty(PROFILE_ID, new Text(next, TextIndexHint.EXACT_MATCH), visibility);
                    taggedBuilder.setProperty(PropertyName.TITLE.toString(), new Text(next), visibility);
                    taggedBuilder.setProperty(PropertyName.CONCEPT_TYPE.toString(), new Text(profileConceptId, TextIndexHint.EXACT_MATCH), visibility);
                    taggedVertex = taggedBuilder.save();
                    auditRepository.auditVertexElementMutation(taggedBuilder, taggedVertex, PROCESS, user);
                } else {
                    taggedVertex = nextQueryVertex;
                }
                graph.addEdge(posting, taggedVertex, MENTIONED_RELATIONSHIP, visibility, user.getAuthorizations());
                String mentionedRelationshipLabelDisplayName = ontologyRepository.getDisplayNameForLabel(MENTIONED_RELATIONSHIP);
                auditRepository.auditRelationship(AuditAction.CREATE, posting, taggedVertex, mentionedRelationshipLabelDisplayName, PROCESS, "", user);
                graph.flush();
            }
        }

        if (post.has(COORDS) && !post.getJSONObject(COORDS).equals(JSONObject.NULL)) {
            JSONObject coordinates = post.getJSONObject(COORDS);
            GeoPoint geo = new GeoPoint(coordinates.getDouble("latitude"), coordinates.getDouble("longitude"));
            ElementMutation<Vertex> postingMutation = posting.prepareMutation();
            postingMutation.setProperty(PropertyName.GEO_LOCATION.toString(), geo, visibility);
            auditRepository.auditVertexElementMutation(postingMutation, posting, PROCESS, user);
            posting = postingMutation.save();
        }

        return posting;
    }

    public String generateUserVertexId(String profileId) {
        return FACEBOOK_VERTEX_ID + profileId;
    }
}
