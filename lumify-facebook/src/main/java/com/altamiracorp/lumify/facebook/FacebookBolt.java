package com.altamiracorp.lumify.facebook;

import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.Visibility;
import org.apache.hadoop.fs.FileSystem;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class FacebookBolt extends BaseLumifyBolt {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(FacebookBolt.class);
    private static final String PROCESS = FacebookBolt.class.getName();
    private static final String USERNAME = "username";
    private static final String AUTHOR_UID = "author_uid";
    private static final String MESSAGE = "message";
    private static final String POST_CONCEPT = "document";
    private static FileSystem fileSystem;
    private static Vertex savedArtifact;


    @Override
    public void safeExecute(Tuple input) throws Exception {
        setHdfsFileSystem();
        JSONObject json = getJsonFromTuple(input);
        calculateTupleType(json);
    }

    private void calculateTupleType(JSONObject jsonObject) throws Exception {
        String name;
        if (jsonObject.has(AUTHOR_UID)) {
            Long name_uid = jsonObject.getLong(AUTHOR_UID);
            name = name_uid.toString();
            LOGGER.info("Facebook tuple is a post: %s", name);
            FacebookPost facebookPost = new FacebookPost();
            ArtifactExtractedInfo postExtractedInfo = facebookPost.processPostArtifact(jsonObject);
            setSavedArtifact(postExtractedInfo);
            Vertex post = facebookPost.processPostVertex(jsonObject, savedArtifact, graph, auditRepository, ontologyRepository, getUser());
            post.setProperty(PropertyName.DISPLAY_TYPE.toString(), POST_CONCEPT, new Visibility(""));
            InputStream in = new ByteArrayInputStream(jsonObject.getString(MESSAGE).getBytes());
            workQueueRepository.pushArtifactHighlight(post.getId().toString());
        } else {
            name = jsonObject.getString(USERNAME);
            LOGGER.info("Facebook tuple is a user: %s", name);
            FacebookUser facebookUser = new FacebookUser();
            Vertex userVertex = facebookUser.process(jsonObject, graph, auditRepository, ontologyRepository, getUser());
            if (userVertex.getPropertyValue(PropertyName.GLYPH_ICON.toString(), 0) == null) {
                ArtifactExtractedInfo profilePicExtractedInfo = facebookUser.createProfilePhotoArtifact(jsonObject, userVertex);
                setSavedArtifact(profilePicExtractedInfo);
                facebookUser.createProfilePhotoVertex(savedArtifact, userVertex, graph, auditRepository, getUser());
            }
        }
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