package com.altamiracorp.lumify.storm.video;

import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.model.artifactThumbnails.ArtifactThumbnailRepository;
import com.altamiracorp.lumify.core.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.core.model.videoFrames.VideoFrameRepository;
import com.altamiracorp.lumify.core.model.workQueue.WorkQueueRepository;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.Visibility;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;
import com.google.inject.Inject;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.altamiracorp.lumify.core.model.properties.MediaLumifyProperties.VIDEO_PREVIEW_IMAGE;
import static com.altamiracorp.lumify.core.util.CollectionUtil.toList;

public class VideoPreviewBolt extends BaseLumifyBolt {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(VideoPreviewBolt.class);
    private VideoFrameRepository videoFrameRepository;

    @Override
    protected void safeExecute(Tuple input) throws Exception {
        JSONObject json = getJsonFromTuple(input);
        String artifactGraphVertexId = json.getString(WorkQueueRepository.KEY_GRAPH_VERTEX_ID);
        LOGGER.info("[VideoPreviewBolt] Generating video preview for %s", artifactGraphVertexId);

        try {
            Iterable<VideoFrame> videoFrames = videoFrameRepository.findAllByArtifactGraphVertexId(artifactGraphVertexId, getUser());
            List<VideoFrame> videoFramesForPreview = getFramesForPreview(videoFrames);
            for (VideoFrame v : videoFramesForPreview) {
                LOGGER.info(v.getRowKey().toString());
            }
            BufferedImage previewImage = createPreviewImage(videoFramesForPreview, getUser());
            saveImage(artifactGraphVertexId, previewImage, getUser());
        } catch (IOException e) {
            throw new RuntimeException("Could not create preview image for artifact: " + artifactGraphVertexId, e);
        }
        LOGGER.debug("Finished [VideoPreviewBolt]: %s", artifactGraphVertexId);
    }

    private void saveImage(String artifactGraphVertexId, BufferedImage previewImage, User user) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(previewImage, "png", out);
        Vertex artifactVertex = graph.getVertex(artifactGraphVertexId, user.getAuthorizations());
        Visibility visibility = new Visibility("");
        StreamingPropertyValue spv = new StreamingPropertyValue(new ByteArrayInputStream(out.toByteArray()), byte[].class);
        spv.searchIndex(false);
        VIDEO_PREVIEW_IMAGE.setProperty(artifactVertex, spv, visibility);
        graph.flush();
    }

    private BufferedImage createPreviewImage(List<VideoFrame> videoFrames, User user) {
        BufferedImage previewImage = new BufferedImage(ArtifactThumbnailRepository.PREVIEW_FRAME_WIDTH * videoFrames.size(), ArtifactThumbnailRepository.PREVIEW_FRAME_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics g = previewImage.getGraphics();
        for (int i = 0; i < videoFrames.size(); i++) {
            Image img = videoFrameRepository.loadImage(videoFrames.get(i), user);
            int dx1 = i * ArtifactThumbnailRepository.PREVIEW_FRAME_WIDTH;
            int dy1 = 0;
            int dx2 = dx1 + ArtifactThumbnailRepository.PREVIEW_FRAME_WIDTH;
            int dy2 = ArtifactThumbnailRepository.PREVIEW_FRAME_HEIGHT;
            int sx1 = 0;
            int sy1 = 0;
            int sx2 = img.getWidth(null);
            int sy2 = img.getHeight(null);
            g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
        }
        return previewImage;
    }

    private List<VideoFrame> getFramesForPreview(Iterable<VideoFrame> videoFramesIterable) {
        List<VideoFrame> videoFrames = toList(videoFramesIterable);

        ArrayList<VideoFrame> results = new ArrayList<VideoFrame>();
        double skip = (double) videoFrames.size() / (double) ArtifactThumbnailRepository.FRAMES_PER_PREVIEW;
        for (double i = 0; i < videoFrames.size(); i += skip) {
            results.add(videoFrames.get((int) Math.floor(i)));
        }
        if (results.size() < 20) {
            results.add(videoFrames.get(videoFrames.size() - 1));
        }
        if (results.size() > 20) {
            results.remove(results.size() - 1);
        }
        return results;
    }

    @Inject
    public void setVideoFrameRepository(VideoFrameRepository videoFrameRepository) {
        this.videoFrameRepository = videoFrameRepository;
    }
}
