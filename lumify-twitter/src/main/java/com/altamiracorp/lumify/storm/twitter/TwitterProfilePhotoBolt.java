/*
 * Copyright 2013 Altamira Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.altamiracorp.lumify.storm.twitter;

import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.LabelName;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.altamiracorp.lumify.core.model.ontology.PropertyName.GLYPH_ICON;
import static com.altamiracorp.lumify.storm.twitter.TwitterConstants.*;

/**
 * This bolt attempts to retrieve a processed Twitter user's profile
 * photo.
 */
public class TwitterProfilePhotoBolt extends BaseTwitterForkBolt {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(TwitterProfilePhotoBolt.class);

    /**
     * The twitter user profile image URL property.
     */
    private static final String PROFILE_IMAGE_URL_PROPERTY = "profile_image_url";

    /**
     * The profile image MIME type.
     */
    private static final String PROFILE_IMAGE_MIME_TYPE = "image/png";

    /**
     * The image artifact title format string.
     */
    private static final String IMAGE_ARTIFACT_TITLE_FMT = "%s Twitter Profile Picture";

    /**
     * The image artifact source.
     */
    private static final String IMAGE_ARTIFACT_SOURCE = "Twitter profile picture";

    /**
     * The format string for the HDFS path for the raw artifact.
     */
    private static final String HDFS_PATH_FMT = "/lumify/artifacts/raw/%s";

    /**
     * The glyph icon property value format.
     */
    private static final String GLYPH_ICON_FMT = "/artifact/%s/raw";
    private static final String PROCESS = TwitterProfilePhotoBolt.class.getName();

    /**
     * Create a new TwitterProfilePhotoBolt.
     *
     * @param boltId the bolt ID
     */
    public TwitterProfilePhotoBolt(final String boltId) {
        super(boltId);
    }

    @Override
    protected void executeFork(final Tuple input) throws Exception {
        String tweeterId = input.getStringByField(TWITTER_USER_VERTEX_ID_FIELD);
        try {
            JSONObject tweeter = (JSONObject) input.getValueByField(TWITTER_USER_JSON_FIELD);
            // only execute if we have a PROFILE_IMAGE_URL
            if (tweeter.has(PROFILE_IMAGE_URL_PROPERTY)) {
                User user = getUser();
                String tweetId = input.getStringByField(TWEET_VERTEX_ID_FIELD);
                String tweetText = input.getStringByField(TWEET_TEXT_FIELD);
                GraphVertex tweeterVertex = graphRepository.findVertex(tweeterId, user);

                URL url = new URL(tweeter.get(PROFILE_IMAGE_URL_PROPERTY).toString());
                InputStream imgIn = url.openStream();
                ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
                IOUtils.copy(imgIn, imgOut);

                byte[] rawImg = imgOut.toByteArray();
                String rowKey = ArtifactRowKey.build(rawImg).toString();

                ArtifactExtractedInfo artifactInfo = new ArtifactExtractedInfo();
                artifactInfo.setMimeType(PROFILE_IMAGE_MIME_TYPE);
                artifactInfo.setRowKey(rowKey);
                artifactInfo.setConceptType(TWEETER_PROFILE_IMAGE_CONCEPT);
                artifactInfo.setTitle(String.format(IMAGE_ARTIFACT_TITLE_FMT, tweeter.getString(SCREEN_NAME_PROPERTY)));
                artifactInfo.setSource(IMAGE_ARTIFACT_SOURCE);
                artifactInfo.setProcess(PROCESS);
                if (rawImg.length > Artifact.MAX_SIZE_OF_INLINE_FILE) {
                    FSDataOutputStream hdfsOut = getHdfsFileSystem().create(new Path(String.format(HDFS_PATH_FMT, rowKey)));
                    try {
                        hdfsOut.write(rawImg);
                    } finally {
                        hdfsOut.close();
                    }
                } else {
                    artifactInfo.setRaw(rawImg);
                }

                GraphVertex imageVertex = saveArtifact(artifactInfo);
                String imageId = imageVertex.getId();

                LOGGER.debug("Saving tweeter profile picture to accumulo and as graph vertex: %s", imageId);

                tweeterVertex.setProperty(GLYPH_ICON.toString(), String.format(GLYPH_ICON_FMT, imageId));
                graphRepository.save(tweeterVertex, user);
                auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), tweeterVertex, PropertyName.GLYPH_ICON.toString(), PROCESS, "", user);

                String labelDisplay = ontologyRepository.getDisplayNameForLabel(LabelName.HAS_IMAGE.toString(), user);
                graphRepository.findOrAddRelationship(tweeterId, imageId, LabelName.HAS_IMAGE, user);
                auditRepository.auditRelationships(AuditAction.CREATE.toString(), tweeterVertex, imageVertex, labelDisplay, PROCESS, "", getUser());
            }
        } catch (IOException ioe) {
            String msg = String.format("Unable to create image for vertex: %s", tweeterId);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.warn(msg, ioe);
            } else {
                LOGGER.warn(msg);
            }
        }
    }
}
