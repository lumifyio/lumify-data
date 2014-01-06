package com.altamiracorp.lumify.facebook;

import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.core.model.artifact.ArtifactType;
import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.ontology.VertexType;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.HdfsLimitOutputStream;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class FacebookPost {
    private static final String PROFILE_ID = "profileId";
    private static final String COORDS = "coords";
    private static final String TAGGEED_UIDS = "tagged_uids";
    private static final String TIMESTAMP = "timestamp";
    private static final String POSTED_RELATIONSHIP = "postPostedByProfile";
    private static final String MENTIONED_RELATIONSHIP = "postMentionedProfile";
    private static final String FACEBOOK = "Facebook";
    private static final String MESSAGE = "message";
    private static final String AUTHOR_UID = "author_uid";
    private static final String FACEBOOK_PROFILE = "facebookProfile";
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(FacebookBolt.class);
    private static final String PROCESS = FacebookPost.class.getName();
    private FacebookBolt facebookBolt = new FacebookBolt();


    protected ArtifactExtractedInfo processPostArtifact(JSONObject post) throws Exception {
        //extract knowledge from post
        String message = post.getString(MESSAGE);
        Long name_uid = post.getLong(AUTHOR_UID);
        String author_uid = name_uid.toString();
        Date time = new Date(post.getLong(TIMESTAMP) * 1000);
        //use extracted information
        ArtifactRowKey build = ArtifactRowKey.build(post.toString().getBytes());
        String rowKey = build.toString();

        HdfsLimitOutputStream textOut = new HdfsLimitOutputStream(facebookBolt.getFileSystem(), Artifact.MAX_SIZE_OF_INLINE_FILE);
        try {
            if (message != null) {
                textOut.write(message.getBytes());
            }
        } finally {
            textOut.close();
        }

        ArtifactExtractedInfo artifactExtractedInfo = new ArtifactExtractedInfo();
        if (textOut.hasExceededSizeLimit()) {
            artifactExtractedInfo.setTextHdfsPath(textOut.getHdfsPath().toString());
        } else {
            artifactExtractedInfo.setText(new String (textOut.getSmall()));
        }
        artifactExtractedInfo.setSource(FACEBOOK);
        artifactExtractedInfo.setRaw(post.toString().getBytes());
        artifactExtractedInfo.setMimeType("text/plain");
        artifactExtractedInfo.setRowKey(rowKey);
        artifactExtractedInfo.setArtifactType(ArtifactType.DOCUMENT.toString());
        artifactExtractedInfo.setAuthor(author_uid);
        artifactExtractedInfo.setDate(time);
        artifactExtractedInfo.setProcess(PROCESS);
        if (message.length() == 0) {
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

    protected GraphVertex processPostVertex (JSONObject post, GraphVertex posting, GraphRepository graphRepository, AuditRepository auditRepository, OntologyRepository ontologyRepository, User user) throws Exception {
        Long name_uid = post.getLong(AUTHOR_UID);
        String author_uid = name_uid.toString();
        LOGGER.info("Saving Facebook post to accumulo and as graph vertex: ", posting.getId());
        Concept profileConcept = ontologyRepository.getConceptByName(FACEBOOK_PROFILE, user);
        String profileConceptId = profileConcept.getId();
        List<String> modifiedProperties = new ArrayList<String>();

        //create entities for each of the ids tagged or author and the relationships
        GraphVertex authorVertex = graphRepository.findVertexByPropertyAndType(PROFILE_ID, author_uid, VertexType.ENTITY, user);
        if (authorVertex == null) {
            authorVertex = new InMemoryGraphVertex();
            authorVertex.setProperty(PROFILE_ID, author_uid);
            authorVertex.setProperty(PropertyName.TITLE.toString(), author_uid);
            authorVertex.setProperty(PropertyName.CONCEPT_TYPE.toString(), profileConceptId);
            graphRepository.save(authorVertex, user);
            graphRepository.commit();
            auditRepository.auditEntity(AuditAction.CREATE.toString(), authorVertex.getId(), posting.getId(), profileConceptId, author_uid, PROCESS, "", user);
            auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), authorVertex, PROFILE_ID, PROCESS, "", user);
            auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), authorVertex, PropertyName.CONCEPT_TYPE.toString(), PROCESS, "", user);
        }
        graphRepository.saveRelationship(authorVertex.getId(), posting.getId(), POSTED_RELATIONSHIP, user);
        String postedRelationshipLabelDisplayName = ontologyRepository.getDisplayNameForLabel(POSTED_RELATIONSHIP, user);
        auditRepository.auditRelationships(AuditAction.CREATE.toString(), posting, authorVertex, postedRelationshipLabelDisplayName, PROCESS, "", user);
        if (post.get(TAGGEED_UIDS) instanceof JSONObject) {
            Iterator tagged = post.getJSONObject(TAGGEED_UIDS).keys();
            while (tagged.hasNext()) {
                String next = tagged.next().toString();
                GraphVertex taggedVertex = graphRepository.findVertexByPropertyAndType(PROFILE_ID, next, VertexType.ENTITY, user);
                if (taggedVertex == null) {
                    taggedVertex = new InMemoryGraphVertex();
                    taggedVertex.setProperty(PROFILE_ID, next);
                    taggedVertex.setProperty(PropertyName.TITLE.toString(), next);
                    taggedVertex.setProperty(PropertyName.CONCEPT_TYPE.toString(), profileConceptId);
                    graphRepository.save(taggedVertex, user);
                    graphRepository.commit();
                    auditRepository.auditEntity(AuditAction.CREATE.toString(), taggedVertex.getId(), posting.getId(), profileConceptId, next, PROCESS, "", user);
                    auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), taggedVertex, PROFILE_ID, PROCESS, "", user);
                    auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), taggedVertex, PropertyName.CONCEPT_TYPE.toString(), PROCESS, "", user);
                }
                graphRepository.saveRelationship(posting.getId(), taggedVertex.getId(), MENTIONED_RELATIONSHIP, user);
                String mentionedRelationshipLabelDisplayName = ontologyRepository.getDisplayNameForLabel(MENTIONED_RELATIONSHIP, user);
                auditRepository.auditRelationships(AuditAction.CREATE.toString(), posting, taggedVertex, mentionedRelationshipLabelDisplayName, PROCESS, "", user);
            }
        }

        if (post.has(COORDS) && !post.getJSONObject(COORDS).equals(JSONObject.NULL)) {
            JSONObject coordinates = post.getJSONObject(COORDS);
            Geoshape geo = Geoshape.point(coordinates.getDouble("latitude"), coordinates.getDouble("longitude"));
            posting.setProperty(PropertyName.GEO_LOCATION, geo);
            modifiedProperties.add(PropertyName.GEO_LOCATION.toString());
            auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), posting, PropertyName.GEO_LOCATION.toString(), PROCESS, "", user);
        }

        graphRepository.save(posting, user);

        return posting;
    }
}
