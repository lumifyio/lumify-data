package com.altamiracorp.lumify.facebook;


import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.search.SearchProvider;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.google.inject.Inject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hadoop.fs.FileSystem;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class FacebookBolt extends BaseLumifyBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookBolt.class);
    private static final String PROCESS = FacebookBolt.class.getName();
    private SearchProvider searchProvider;
    private static final String USERNAME = "username";
    private static final String AUTHOR_UID = "author_uid";
    private static final String MESSAGE = "message";
    private static FileSystem fileSystem;
    private static GraphVertex savedArtifact;


    @Override
    public void safeExecute(Tuple input) throws Exception {
        setHdfsFileSystem();
        JSONObject json = getJsonFromTuple(input);
        String tupleName = calculateTupleType(json);
    }

    private String calculateTupleType(JSONObject jsonObject) throws Exception {
        String name;
        if (jsonObject.has(AUTHOR_UID)) {
            Long name_uid = jsonObject.getLong(AUTHOR_UID);
            name = name_uid.toString();
            LOGGER.info(String.format("Facebook tuple is a post: %s", name));
            FacebookPost facebookPost = new FacebookPost();
            ArtifactExtractedInfo postExtractedInfo = facebookPost.processPostArtifact(jsonObject);
            setSavedArtifact(postExtractedInfo);
            GraphVertex post = facebookPost.processPostVertex(jsonObject, savedArtifact, graphRepository, auditRepository, ontologyRepository, getUser());
            InputStream in = new ByteArrayInputStream(jsonObject.getString(MESSAGE).getBytes());
            searchProvider.add(post, in);
            workQueueRepository.pushArtifactHighlight(post.getId());
        } else {
            name = jsonObject.getString(USERNAME);
            LOGGER.info(String.format("Facebook tuple is a user: %s", name));
            FacebookUser facebookUser = new FacebookUser();
            GraphVertex userVertex = facebookUser.process(jsonObject, graphRepository, auditRepository, ontologyRepository, getUser());
            ArtifactExtractedInfo profilePicExtractedInfo = facebookUser.createProfilePhotoArtifact(jsonObject, userVertex);
            setSavedArtifact(profilePicExtractedInfo);
            facebookUser.createProfilePhotoVertex(savedArtifact, userVertex, graphRepository, auditRepository, getUser());
        }
        return name;
    }

    @Inject
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }
    public User getUser() {
        return super.getUser();
    }

    private void setHdfsFileSystem() {
        this.fileSystem = getHdfsFileSystem();
    }

    public void setHdfsFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    private void setSavedArtifact(ArtifactExtractedInfo artifactExtractedInfo) {
        this.savedArtifact = saveArtifact(artifactExtractedInfo);
    }

}