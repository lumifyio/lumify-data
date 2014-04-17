package com.altamiracorp.lumify.facebook;

import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.facebook.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.ontology.OntologyLumifyProperties;
import com.altamiracorp.lumify.core.security.LumifyVisibility;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.securegraph.Vertex;
import org.apache.hadoop.fs.FileSystem;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.altamiracorp.lumify.core.model.properties.LumifyProperties.GLYPH_ICON;
import static com.altamiracorp.lumify.facebook.FacebookConstants.*;

public class FacebookBolt extends BaseLumifyBolt {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(FacebookBolt.class);
    private static final String PROCESS = FacebookBolt.class.getName();
    private LumifyVisibility lumifyVisibility;
    private static FileSystem fileSystem;
    private static Vertex savedArtifact;

    @Override
    public void safeExecute(Tuple input) throws Exception {
        lumifyVisibility = new LumifyVisibility();
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
            Vertex post = facebookPost.processPostVertex(jsonObject, savedArtifact, graph, auditRepository, ontologyRepository, getUser(), getAuthorizations());
            OntologyLumifyProperties.DISPLAY_TYPE.setProperty(post, POST_CONCEPT, lumifyVisibility.getVisibility());
            InputStream in = new ByteArrayInputStream(jsonObject.getString(MESSAGE).getBytes());
            graph.flush();
        } else {
            name = jsonObject.getString(USERNAME);
            LOGGER.info("Facebook tuple is a user: %s", name);
            FacebookUser facebookUser = new FacebookUser();
            Vertex userVertex = facebookUser.process(jsonObject, graph, auditRepository, ontologyRepository, getUser(), getAuthorizations());
            // TO-DO: Replace GLYPH_ICON with ENTITY_IMAGE_URL
            if (userVertex.getPropertyValue(GLYPH_ICON.getKey()) == null) {
                ArtifactExtractedInfo profilePicExtractedInfo = facebookUser.createProfilePhotoArtifact(jsonObject, userVertex);
                setSavedArtifact(profilePicExtractedInfo);
                facebookUser.createProfilePhotoVertex(savedArtifact, userVertex, graph, auditRepository, ontologyRepository, getUser(), getAuthorizations());
            }
            graph.flush();
        }
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    private void setHdfsFileSystem() {
        this.fileSystem = getHdfsFileSystem();
    }

    public void setHdfsFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    private void setSavedArtifact(ArtifactExtractedInfo artifactExtractedInfo) throws Exception {
        this.savedArtifact = saveArtifact(artifactExtractedInfo);
    }
}