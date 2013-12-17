package com.altamiracorp.lumify.facebook;


import java.io.ByteArrayInputStream;
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

import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.altamiracorp.lumify.core.model.artifact.ArtifactMetadata;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.core.model.artifact.ArtifactType;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.graph.GraphGeoLocation;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.model.TitanGraphSession;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.core.model.ontology.LabelName;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.ontology.VertexType;
import com.altamiracorp.lumify.core.model.search.SearchProvider;
import com.beust.jcommander.internal.Lists;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.google.common.base.Joiner;
import com.google.inject.Inject;

public class FacebookBolt extends BaseLumifyBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookBolt.class);
    private SearchProvider searchProvider;
    private static final String PROFILE_ID = "profileId";
    private static final String USERNAME = "username";
    private static final String AUTHOR_UID = "author_uid";
    private static final String NAME = "name";
    private static final String UID = "uid";
    private static final String SEX = "sex";
    private static final String GENDER = "gender";
    private static final String EMAIL = "email";
    private static final String EMAIL_ADDRESS = "emailAddress";
    private static final String EMAIL_RELATIONSHIP = "personHasEmailAddress";
    private static final String COORDS = "coords";
    private static final String BIRTHDAY_DATE = "birthday_date";
    private static final String BIRTHDAY = "birthday";
    private static final String PIC = "pic";
    private static final String MESSAGE = "message";
    private static final String TAGGEED_UIDS = "tagged_uids";
    private static final String TIMESTAMP = "timestamp";
    private static final String POSTED_RELATIONSHIP = "postPostedByProfile";
    private static final String MENTIONED_RELATIONSHIP = "postMentionedProfile";
    private static final String FACEBOOK = "Facebook";
    private static final String FACEBOOK_PROFILE = "facebookProfile";
    private static final String BIRTHDAY_FORMAT = "MM/dd";


    @Override
    public void safeExecute(Tuple input) throws Exception {
        JSONObject json = getJsonFromTuple(input);
        String tupleName = calculateTupleType(json);
    }

    private String calculateTupleType(JSONObject jsonObject) throws Exception {
        String name;
        if (jsonObject.has(AUTHOR_UID)) {
            Long name_uid = jsonObject.getLong(AUTHOR_UID);
            name = name_uid.toString();
            LOGGER.info(String.format("Facebook tuple is a post: %s", name));
            GraphVertex post = processPost(jsonObject);
            InputStream in = new ByteArrayInputStream(jsonObject.getString(MESSAGE).getBytes());
            searchProvider.add(post, in);
            workQueueRepository.pushArtifactHighlight(post.getId());
        } else {
            name = jsonObject.getString(USERNAME);
            LOGGER.info(String.format("Facebook tuple is a user: %s", name));
            processUser(jsonObject);
        }
        return name;
    }

    private void processUser(JSONObject user) throws ParseException {
        //create entity for each user with the properties if one doesn't already exist
        String name = user.getString(NAME);
        Long name_uid = user.getLong(UID);
        String profileId = name_uid.toString();
        String username = user.getString(USERNAME);
        Concept profileConcept = ontologyRepository.getConceptByName(FACEBOOK_PROFILE, getUser());

        GraphVertex userVertex = graphRepository.findVertexByPropertyAndType(PROFILE_ID, profileId, VertexType.ENTITY, getUser());

        List<String> modifiedProperties = Lists.newArrayList
                (PropertyName.TITLE.toString(), PropertyName.TYPE.toString(), PropertyName.SUBTYPE.toString(), PropertyName.DISPLAY_NAME.toString(), PROFILE_ID);


        userVertex.setProperty(PropertyName.DISPLAY_NAME, username);
        userVertex.setProperty(PropertyName.TITLE, name);
        userVertex.setProperty(PropertyName.TYPE, VertexType.ENTITY.toString());
        userVertex.setProperty(PropertyName.SUBTYPE, profileConcept.getId());
        graphRepository.save(userVertex, getUser());

        //get relationships for vertex and write audit message for each post
        List <GraphVertex> postings = graphRepository.getRelatedVertices(userVertex.getId(), getUser());
        GraphVertex posting = postings.get(0);

        if (user.has(SEX) && !user.getString(SEX).equals(JSONObject.NULL)) {
            String gender = user.getString(SEX);
            userVertex.setProperty(GENDER, gender);
            modifiedProperties.add(GENDER);
        }

        if (user.has(EMAIL) && !user.getString(EMAIL).equals(JSONObject.NULL)) {
            String email = user.getString(EMAIL);
            GraphVertex emailVertex = graphRepository.findVertexByPropertyAndType(EMAIL_ADDRESS, email, VertexType.ENTITY, getUser());
            if (emailVertex == null) {
                emailVertex = new InMemoryGraphVertex();
                userVertex.setProperty(EMAIL, email);
            }
            graphRepository.saveRelationship(userVertex.getId(), emailVertex.getId(), EMAIL_RELATIONSHIP, getUser());
        }

        if (user.has(COORDS) && !user.get(COORDS).equals(JSONObject.NULL)) {
            JSONObject coordinates = user.getJSONObject(COORDS);
            Geoshape geo = Geoshape.point(coordinates.getDouble("latitude"), coordinates.getDouble("longitude"));
            userVertex.setProperty(PropertyName.GEO_LOCATION, geo);
            modifiedProperties.add(PropertyName.GEO_LOCATION.toString());
        }

        if (user.has(BIRTHDAY_DATE) && (user.get(BIRTHDAY_DATE) instanceof String) ) {
            String birthday_date = user.getString(BIRTHDAY_DATE);
            SimpleDateFormat birthdayFormat = new SimpleDateFormat(BIRTHDAY_FORMAT);
            birthdayFormat.setLenient(true);
            if (birthday_date.length() > BIRTHDAY_FORMAT.length()) {
                birthdayFormat = new SimpleDateFormat(BIRTHDAY_FORMAT + "/yyyy");
            }
            Date birthday = birthdayFormat.parse(birthday_date);
            userVertex.setProperty(BIRTHDAY, birthday.getTime());
            modifiedProperties.add(BIRTHDAY);
        }
        //create and save profile picture
        modifiedProperties.addAll(createProfilePhotoArtifact(user, userVertex, posting));
        auditRepository.audit(userVertex.getId(), auditRepository.vertexPropertyAuditMessages(userVertex, modifiedProperties), getUser());
        graphRepository.save(userVertex, getUser());
    }

    public List<String> createProfilePhotoArtifact(JSONObject user, GraphVertex userVertex, GraphVertex postingVertex) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        List<String> modifiedProperties = new ArrayList<String>();
        try {
            URL url = new URL(user.get(PIC).toString());
            InputStream is = url.openStream();
            IOUtils.copy(is, os);
            byte[] raw = os.toByteArray();
            ArtifactRowKey build = ArtifactRowKey.build(raw);
            String rowKey = build.toString();
            String fileName = user.getString(USERNAME) + "ProfilePicture";
            Artifact artifact = new Artifact(rowKey);

            ArtifactExtractedInfo artifactExtractedInfo = new ArtifactExtractedInfo();
            artifactExtractedInfo.setMimeType("image/png");
            artifactExtractedInfo.setRowKey(rowKey);
            artifactExtractedInfo.setArtifactType(ArtifactType.IMAGE.toString());
            artifactExtractedInfo.setTitle(user.getString(NAME) + " Facebook Profile Picture");
            artifactExtractedInfo.setSource("Facebook profile picture");
            artifactExtractedInfo.setRaw(raw);

            ArtifactMetadata metadata = artifact.getMetadata();
            metadata.setCreateDate(new Date());
            metadata.setRaw(raw);
            metadata.setFileName(fileName);
            metadata.setFileExtension(FilenameUtils.getExtension(fileName));
            metadata.setMimeType("image/png");

            GraphVertex profile = saveArtifact(artifactExtractedInfo);
            LOGGER.info(String.format("Saving Facebook profile picture to accumulo and as graph vertex: %s", profile.getId()));

            userVertex.setProperty(PropertyName.GLYPH_ICON.toString(), "/artifact/" + rowKey + "/raw");
            modifiedProperties.add(PropertyName.GLYPH_ICON.toString());
            String labelDisplay = LabelName.HAS_IMAGE.toString();
            Object sourceTitle = userVertex.getProperty(PropertyName.TITLE.toString());
            Object destTitle = profile.getProperty(PropertyName.TITLE.toString());
            graphRepository.save(userVertex, getUser());
            graphRepository.findOrAddRelationship(userVertex.getId(), profile.getId(), labelDisplay, getUser());
            auditRepository.audit(userVertex.getId(), auditRepository.relationshipAuditMessageOnSource(labelDisplay, destTitle, postingVertex.getProperty(PropertyName.TITLE).toString()), getUser());
            auditRepository.audit(profile.getId(), auditRepository.relationshipAuditMessageOnDest(labelDisplay, sourceTitle, postingVertex.getProperty(PropertyName.TITLE).toString()), getUser());
        } catch (IOException e) {
            LOGGER.warn(String.format("Failed to create image for vertex: %s", userVertex.getId()));
            new IOException(e);
        }
        return modifiedProperties;
    }


    private GraphVertex processPost(JSONObject post) {
        //extract knowledge from post
        String message = post.getString(MESSAGE);
        Long name_uid = post.getLong(AUTHOR_UID);
        String author_uid = name_uid.toString();
        Date time = new Date(post.getLong(TIMESTAMP) * 1000);
        //use extracted information
        ArtifactRowKey build = ArtifactRowKey.build(post.toString().getBytes());
        String rowKey = build.toString();

        ArtifactExtractedInfo artifactExtractedInfo = new ArtifactExtractedInfo();
        artifactExtractedInfo.setText(message);
        artifactExtractedInfo.setSource(FACEBOOK);
        artifactExtractedInfo.setRaw(post.toString().getBytes());
        artifactExtractedInfo.setMimeType("text/plain");
        artifactExtractedInfo.setRowKey(rowKey);
        artifactExtractedInfo.setArtifactType(ArtifactType.DOCUMENT.toString());
        artifactExtractedInfo.setAuthor(author_uid);
        artifactExtractedInfo.setDate(time);
        if (message.length() == 0) {
            artifactExtractedInfo.setTitle("Facebook Image Post");
        } else if (message.length() > 140) {
            String shortTitle = message.substring(0, 137) + "...";
            artifactExtractedInfo.setTitle(shortTitle);
        } else {
            artifactExtractedInfo.setTitle(message);
        }

        //write artifact with extracted info to accumulo and create entity
        GraphVertex posting = saveArtifact(artifactExtractedInfo);
        LOGGER.info("Saving Facebook post to accumulo and as graph vertex: " + posting.getId());
        List<String> modifiedProperties = new ArrayList<String>();

        //create entities for each of the ids tagged or author and the relationships
        GraphVertex authorVertex = graphRepository.findVertexByPropertyAndType(PROFILE_ID, author_uid, VertexType.ENTITY, getUser());
        if (authorVertex == null) {
            authorVertex = new InMemoryGraphVertex();
            authorVertex.setProperty(PROFILE_ID, author_uid);
            authorVertex.setProperty(PropertyName.TYPE.toString(), VertexType.ENTITY.toString());
            graphRepository.save(authorVertex, getUser());
            graphRepository.commit();
            auditRepository.audit(posting.getId(), auditRepository.resolvedEntityAuditMessageForArtifact(authorVertex.getId()), getUser());
            auditRepository.audit(authorVertex.getId(), auditRepository.resolvedEntityAuditMessage(posting.getId()), getUser());
        }
        graphRepository.saveRelationship(authorVertex.getId(), posting.getId(), POSTED_RELATIONSHIP, getUser());
        String postedRelationshipLabelDisplayName = ontologyRepository.getDisplayNameForLabel(POSTED_RELATIONSHIP, getUser());
        String text = posting.getProperty(PropertyName.TITLE).toString();
        auditRepository.audit(authorVertex.getId(), auditRepository.relationshipAuditMessageOnSource(postedRelationshipLabelDisplayName, text, ""), getUser());
        auditRepository.audit(posting.getId(), auditRepository.relationshipAuditMessageOnDest(postedRelationshipLabelDisplayName, authorVertex, text), getUser());//        auditRepository.audit(posting.getId(), auditRepository.relationshipAuditMessageOnArtifact(POSTED_RELATIONSHIP, ));
        if (post.get(TAGGEED_UIDS) instanceof JSONObject) {
            Iterator tagged = post.getJSONObject(TAGGEED_UIDS).keys();
            while (tagged.hasNext()) {
                String next = tagged.next().toString();
                GraphVertex taggedVertex = graphRepository.findVertexByPropertyAndType(PROFILE_ID, next, VertexType.ENTITY, getUser());
                if (taggedVertex == null) {
                    taggedVertex = new InMemoryGraphVertex();
                    taggedVertex.setProperty(PROFILE_ID, next);
                    taggedVertex.setProperty(PropertyName.TYPE.toString(), VertexType.ENTITY.toString());
                    graphRepository.save(taggedVertex, getUser());
                    graphRepository.commit();
                    auditRepository.audit(posting.getId(), auditRepository.resolvedEntityAuditMessageForArtifact(taggedVertex.getId()), getUser());
                    auditRepository.audit(taggedVertex.getId(), auditRepository.resolvedEntityAuditMessage(posting.getId()), getUser());
                }
                graphRepository.saveRelationship(posting.getId(), taggedVertex.getId(), MENTIONED_RELATIONSHIP, getUser());
                String mentionedRelationshipLabelDisplayName = ontologyRepository.getDisplayNameForLabel(MENTIONED_RELATIONSHIP, getUser());
                auditRepository.audit(taggedVertex.getId(), auditRepository.relationshipAuditMessageOnSource(mentionedRelationshipLabelDisplayName, text, ""), getUser());
                auditRepository.audit(posting.getId(), auditRepository.relationshipAuditMessageOnDest(mentionedRelationshipLabelDisplayName, taggedVertex, text), getUser());
            }
        }

        if (post.has(COORDS) && !post.getJSONObject(COORDS).equals(JSONObject.NULL)) {
            JSONObject coordinates = post.getJSONObject(COORDS);
            Geoshape geo = Geoshape.point(coordinates.getDouble("latitude"), coordinates.getDouble("longitude"));
            posting.setProperty(PropertyName.GEO_LOCATION, geo);
            modifiedProperties.add(PropertyName.GEO_LOCATION.toString());
        }

        graphRepository.save(posting, getUser());
        auditRepository.audit(posting.getId(), auditRepository.vertexPropertyAuditMessages(posting, modifiedProperties), getUser());

        return posting;
    }

    @Inject
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }
}