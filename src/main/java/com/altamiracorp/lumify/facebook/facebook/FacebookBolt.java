package com.altamiracorp.lumify.facebook.facebook;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.altamiracorp.lumify.core.model.artifact.ArtifactMetadata;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.core.model.artifact.ArtifactType;
import com.altamiracorp.lumify.core.model.graph.GraphGeoLocation;
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
    private static final Joiner FILEPATH_JOINER = Joiner.on('/');
    private String dataDir;
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




    @Override
    public void safeExecute(Tuple input) throws Exception {
        JSONObject json = getJsonFromTuple(input);
        String tupleName = calculateTupleType(json);
    }

    private String calculateTupleType(JSONObject jsonObject) throws Exception {
        String name;
        if (jsonObject.has(AUTHOR_UID)) {
            name = jsonObject.getString(AUTHOR_UID);
            InputStream in = openFile(name);
            LOGGER.info("Facebook tuple is a post. Writing: %s to accumulo", name);
            processPost(jsonObject);
        } else {
            name = jsonObject.getString(USERNAME);
            LOGGER.info("Facebook tuple is a user. Creating entity for: %s", name);
            processUser(jsonObject);
        }
        return name;
    }

    private void processUser(JSONObject user) throws ParseException {
        //create entity for each user with the properties if one doesn't already exist
        String name = user.getString(NAME);
        String profileId = user.getString(UID);
        String username = user.getString(USERNAME);
        boolean newVertex = false;

        GraphVertex userVertex = graphRepository.findVertexByPropertyAndType(PROFILE_ID, profileId, VertexType.ENTITY, getUser());
        if (userVertex == null) {
            newVertex = true;
            userVertex = new InMemoryGraphVertex();
            userVertex.setProperty(PROFILE_ID, profileId);
        }

        List<String> modifiedProperties = Lists.newArrayList
                (PropertyName.TITLE.toString(), PropertyName.TYPE.toString(), PropertyName.SUBTYPE.toString(), PropertyName.DISPLAY_NAME.toString());


        userVertex.setProperty(PropertyName.DISPLAY_NAME, username);
        userVertex.setProperty(PropertyName.TITLE, name);
        userVertex.setProperty(PropertyName.TYPE, VertexType.ENTITY.toString());
        userVertex.setProperty(PropertyName.SUBTYPE, FACEBOOK_PROFILE);
        graphRepository.save(userVertex, getUser());

        if (newVertex) {
            auditRepository.audit(userVertex.getId(), auditRepository.createEntityAuditMessage(), getUser());
        }

        if (user.has(SEX) && !user.getString(SEX).equals(JSONObject.NULL)){
            String gender = user.getString(SEX);
            userVertex.setProperty(GENDER, gender);
            modifiedProperties.add(GENDER);
        }

        if (user.has(EMAIL) && !user.getString(EMAIL).equals(JSONObject.NULL)){
            String email = user.getString(EMAIL);
            GraphVertex emailVertex = graphRepository.findVertexByPropertyAndType(EMAIL_ADDRESS, email, VertexType.ENTITY, getUser());
            if (emailVertex == null) {
                emailVertex = new InMemoryGraphVertex();
                userVertex.setProperty(EMAIL, email);
            }
            graphRepository.saveRelationship(userVertex.getId(), emailVertex.getId(), EMAIL_RELATIONSHIP, getUser());
        }

        if (user.has(COORDS) && !user.get(COORDS).equals(JSONObject.NULL)) {
            JSONArray coordinates = user.getJSONObject(COORDS).getJSONArray(COORDS);
            userVertex.setProperty(PropertyName.GEO_LOCATION, new GraphGeoLocation(coordinates.getDouble(0), coordinates.getDouble(1)));
            modifiedProperties.add(PropertyName.GEO_LOCATION.toString());
        }

        if (user.has(BIRTHDAY_DATE) && !user.getString(BIRTHDAY_DATE).equals(JSONObject.NULL)){
            String birthday_date = user.getString(BIRTHDAY_DATE);
            SimpleDateFormat birthdayFormat = new SimpleDateFormat("MM/dd/yyyy");
            try {
                Date birthday = birthdayFormat.parse(birthday_date);
                userVertex.setProperty(BIRTHDAY, birthday);
                modifiedProperties.add(BIRTHDAY);
            } catch (ParseException e) {
                new RuntimeException("Cannot parse " + birthday_date);
            }
        }
        //create and save profile picture
        modifiedProperties.addAll(createProfilePhotoArtifact(user, userVertex));
    }

    public List<String> createProfilePhotoArtifact(JSONObject user, GraphVertex userVertex) {
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
            artifactExtractedInfo.setTitle(user.getString(USERNAME) + " Facebook Profile Picture");
            artifactExtractedInfo.setSource("Facebook profile picture");
            artifactExtractedInfo.setRaw(raw);

            ArtifactMetadata metadata = artifact.getMetadata();
            metadata.setCreateDate(new Date());
            metadata.setRaw(raw);
            metadata.setFileName(fileName);
            metadata.setFileExtension(FilenameUtils.getExtension(fileName));
            metadata.setMimeType("image/png");

            GraphVertex profile = saveArtifact(artifactExtractedInfo);
            LOGGER.info("Saving Facebook profile picture to accumulo and as graph vertex: " + profile.getId());

            userVertex.setProperty(PropertyName.GLYPH_ICON.toString(), "/artifact/" + rowKey + "/raw");
            modifiedProperties.add(PropertyName.GLYPH_ICON.toString());
            graphRepository.save(userVertex, getUser());

            graphRepository.findOrAddRelationship(userVertex.getId(), profile.getId(), LabelName.HAS_IMAGE, getUser());
        } catch (IOException e) {
            LOGGER.warn("Failed to create image for vertex: " + userVertex.getId());
            new IOException(e);
        }
        return modifiedProperties;
    }


    private void processPost(JSONObject post) {
        //extract knowledge from post
        String message = post.getString(MESSAGE);
        String author_uid = post.getString(AUTHOR_UID);
        JSONArray tagged_uids = post.getJSONArray(TAGGEED_UIDS);
        Date time = new Date(post.getLong(TIMESTAMP)*1000);
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
        if (message.length() > 140) {
            artifactExtractedInfo.setTitle(message.substring(0, 139));
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
        }
        graphRepository.saveRelationship(authorVertex.getId(), posting.getId(), POSTED_RELATIONSHIP, getUser());

        for (int i = 1; i < tagged_uids.length(); i++) {
            JSONObject tagged = tagged_uids.getJSONObject(i);
            GraphVertex taggedVertex = graphRepository.findVertexByPropertyAndType(PROFILE_ID, tagged.toString(), VertexType.ENTITY, getUser());
            if (taggedVertex == null) {
                taggedVertex = new InMemoryGraphVertex();
                authorVertex.setProperty(PROFILE_ID, tagged.toString());
            }
            graphRepository.saveRelationship(taggedVertex.getId(), posting.getId(), MENTIONED_RELATIONSHIP, getUser());
        }

        if (post.has(COORDS) && !post.get(COORDS).equals(JSONObject.NULL)) {
            JSONArray coordinates = post.getJSONObject(COORDS).getJSONArray(COORDS);
            Geoshape geo = Geoshape.point(coordinates.getDouble(0), coordinates.getDouble(1));
            posting.setProperty(PropertyName.GEO_LOCATION, geo);
            modifiedProperties.add(PropertyName.GEO_LOCATION.toString());
        }

        graphRepository.save(posting, getUser());
        auditRepository.audit(posting.getId(), auditRepository.vertexPropertyAuditMessages(posting, modifiedProperties), getUser());

    }

    @Inject
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }
}