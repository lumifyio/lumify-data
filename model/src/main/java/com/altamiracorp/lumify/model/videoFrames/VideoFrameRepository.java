package com.altamiracorp.lumify.model.videoFrames;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.ModelSession;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.Row;
import com.altamiracorp.lumify.model.SaveFileResults;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;
import com.google.inject.Inject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class VideoFrameRepository extends Repository<VideoFrame> {
    private VideoFrameBuilder videoFrameBuilder = new VideoFrameBuilder();

    @Inject
    public VideoFrameRepository(final ModelSession modelSession) {
        super(modelSession);
    }

    @Override
    public VideoFrame fromRow(Row row) {
        return videoFrameBuilder.fromRow(row);
    }

    @Override
    public Row toRow(VideoFrame videoFrame) {
        return videoFrame;
    }

    @Override
    public String getTableName() {
        return videoFrameBuilder.getTableName();
    }

    public void saveVideoFrame(ModelSession session, ArtifactRowKey artifactRowKey, InputStream in, long frameStartTime, User user) {
        SaveFileResults saveFileResults = session.saveFile(in, user);
        VideoFrameRowKey videoFrameRowKey = new VideoFrameRowKey(artifactRowKey.toString(), frameStartTime);
        VideoFrame videoFrame = new VideoFrame(videoFrameRowKey);
        videoFrame.getMetadata()
                .setHdfsPath(saveFileResults.getFullPath());
        save(videoFrame, user);
    }

    public List<VideoFrame> findAllByArtifactRowKey(String rowKey, User user) {
        return findByRowStartsWith(rowKey, user);
    }

    public BufferedImage loadImage(VideoFrame videoFrame, User user) {
        InputStream in = getModelSession().loadFile(videoFrame.getMetadata().getHdfsPath(), user);
        try {
            return ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeException("Could not load image: " + videoFrame.getRowKey(), e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException("Could not close InputStream", e);
            }
        }
    }
}