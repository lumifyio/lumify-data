package com.altamiracorp.lumify.videoPreview;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.SaveFileResults;
import com.altamiracorp.lumify.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.model.videoFrames.VideoFrameRepository;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ViewPreviewGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewPreviewGenerator.class.getName());
    public static final int FRAMES_PER_PREVIEW = 20;
    private int previewFrameWidth = 360;
    private int previewHeight = 240;
    private final ArtifactRepository artifactRepository;
    private final VideoFrameRepository videoFrameRepository;

    @Inject
    public ViewPreviewGenerator(ArtifactRepository artifactRepository, VideoFrameRepository videoFrameRepository) {
        this.artifactRepository = artifactRepository;
        this.videoFrameRepository = videoFrameRepository;
    }

    public void createPreview(Artifact artifact, User user) {
        try {
            List<VideoFrame> videoFrames = videoFrameRepository.findAllByArtifactRowKey(artifact.getRowKey().toString(), user);
            List<VideoFrame> videoFramesForPreview = getFramesForPreview(videoFrames);
            for (VideoFrame v : videoFramesForPreview) {
                LOGGER.info(v.getRowKey().toString());
            }
            BufferedImage previewImage = createPreviewImage(videoFramesForPreview, user);
            SaveFileResults saveFileResults = saveImage(previewImage, user);
            artifact.getGenericMetadata().setVideoPreviewImageHdfsFilePath(saveFileResults.getFullPath());
        } catch (IOException e) {
            throw new RuntimeException("Could not create preview image for artifact: " + artifact.getRowKey(), e);
        }
    }

    private SaveFileResults saveImage(BufferedImage previewImage, User user) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(previewImage, "png", out);
        return artifactRepository.saveFile(new ByteArrayInputStream(out.toByteArray()), user);
    }

    private BufferedImage createPreviewImage(List<VideoFrame> videoFrames, User user) {
        BufferedImage previewImage = new BufferedImage(previewFrameWidth * videoFrames.size(), previewHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g = previewImage.getGraphics();
        for (int i = 0; i < videoFrames.size(); i++) {
            Image img = videoFrameRepository.loadImage(videoFrames.get(i), user);
            int dx1 = i * previewFrameWidth;
            int dy1 = 0;
            int dx2 = dx1 + previewFrameWidth;
            int dy2 = previewHeight;
            int sx1 = 0;
            int sy1 = 0;
            int sx2 = img.getWidth(null);
            int sy2 = img.getHeight(null);
            g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
        }
        return previewImage;
    }

    private List<VideoFrame> getFramesForPreview(List<VideoFrame> videoFrames) {
        ArrayList<VideoFrame> results = new ArrayList<VideoFrame>();
        double skip = (double) videoFrames.size() / (double) FRAMES_PER_PREVIEW;
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
}
