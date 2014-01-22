package com.altamiracorp.lumify.storm.video;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.model.artifactThumbnails.ArtifactThumbnailRepository;
import com.altamiracorp.lumify.core.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.core.model.videoFrames.VideoFrameRepository;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.google.inject.Inject;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.altamiracorp.lumify.core.util.CollectionUtil.toList;

public class VideoPreviewBolt extends BaseLumifyBolt {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(VideoPreviewBolt.class);
    private VideoFrameRepository videoFrameRepository;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
        try {
            mkdir(ArtifactThumbnailRepository.LUMIFY_VIDEO_PREVIEW_HDFS_PATH);
        } catch (IOException e) {
            collector.reportError(e);
        }
    }

    @Override
    protected void safeExecute(Tuple input) throws Exception {
        JSONObject json = getJsonFromTuple(input);
        String artifactRowKey = json.getString("artifactRowKey");
        LOGGER.info("[VideoPreviewBolt] Generating video preview for %s", artifactRowKey);

        try {
            Iterable<VideoFrame> videoFrames = videoFrameRepository.findAllByArtifactRowKey(artifactRowKey, getUser());
            List<VideoFrame> videoFramesForPreview = getFramesForPreview(videoFrames);
            for (VideoFrame v : videoFramesForPreview) {
                LOGGER.info(v.getRowKey().toString());
            }
            BufferedImage previewImage = createPreviewImage(videoFramesForPreview, getUser());
            saveImage(artifactRowKey, previewImage, getUser());
        } catch (IOException e) {
            throw new RuntimeException("Could not create preview image for artifact: " + artifactRowKey, e);
        }
        LOGGER.debug("Finished [VideoPreviewBolt]: %s", artifactRowKey);
    }

    private Path saveImage(String artifactRowKey, BufferedImage previewImage, User user) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(previewImage, "png", out);
        Path path = new Path(ArtifactThumbnailRepository.getVideoPreviewPath(artifactRowKey));
        FSDataOutputStream hdfsOut = getHdfsFileSystem().create(path);
        try {
            hdfsOut.write(out.toByteArray());
        } finally {
            hdfsOut.close();
        }
        return path;
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
