package com.altamiracorp.reddawn.videoPreview;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.SaveFileResults;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrame;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrameRepository;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
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
    private int framesPerPreview = 20;
    private int previewFrameWidth = 360;
    private int previewHeight = 240;
    private ArtifactRepository artifactRepository = new ArtifactRepository();
    private VideoFrameRepository videoFrameRepository = new VideoFrameRepository();

    public void createPreview(RedDawnSession session, Artifact artifact) {
        try {
            List<VideoFrame> videoFrames = videoFrameRepository.findAllByArtifactRowKey(session.getModelSession(), artifact.getRowKey().toString());
            List<VideoFrame> videoFramesForPreview = getFramesForPreview(videoFrames);
            for (VideoFrame v : videoFramesForPreview) {
                LOGGER.info(v.getRowKey().toString());
            }
            BufferedImage previewImage = createPreviewImage(session, videoFramesForPreview);
            SaveFileResults saveFileResults = saveImage(session, previewImage);
            artifact.getGenericMetadata().setVideoPreviewImageHdfsFilePath(saveFileResults.getFullPath());
        } catch (IOException e) {
            throw new RuntimeException("Could not create preview image for artifact: " + artifact.getRowKey(), e);
        }
    }

    private SaveFileResults saveImage(RedDawnSession session, BufferedImage previewImage) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(previewImage, "png", out);
        return artifactRepository.saveFile(session.getModelSession(), new ByteArrayInputStream(out.toByteArray()));
    }

    private BufferedImage createPreviewImage(RedDawnSession session, List<VideoFrame> videoFrames) {
        BufferedImage previewImage = new BufferedImage(previewFrameWidth * videoFrames.size(), previewHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g = previewImage.getGraphics();
        for (int i = 0; i < videoFrames.size(); i++) {
            Image img = videoFrameRepository.loadImage(session.getModelSession(), videoFrames.get(i));
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
        double skip = (double) videoFrames.size() / (double) framesPerPreview;
        for (double i = 0; i < videoFrames.size(); i += skip) {
            results.add(videoFrames.get((int) Math.floor(i)));
        }
        return results;
    }
}
