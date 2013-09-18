package com.altamiracorp.lumify.model.videoFrames;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.*;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public class VideoFrameRepository extends Repository<VideoFrame> {
    @Override
    public VideoFrame fromRow(Row row) {
        VideoFrame videoFrame = new VideoFrame(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            String columnFamilyName = columnFamily.getColumnFamilyName();
            if (columnFamilyName.equals(VideoFrameMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                videoFrame.addColumnFamily(new VideoFrameMetadata().addColumns(columns));
            } else if (columnFamilyName.equals(VideoFrameDetectedObjects.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                videoFrame.addColumnFamily(new VideoFrameDetectedObjects().addColumns(columns));
            } else {
                videoFrame.addColumnFamily(columnFamily);
            }
        }
        return videoFrame;
    }

    @Override
    public Row toRow(VideoFrame videoFrame) {
        return videoFrame;
    }

    @Override
    public String getTableName() {
        return VideoFrame.TABLE_NAME;
    }

    public void saveVideoFrame(ModelSession session, ArtifactRowKey artifactRowKey, InputStream in, long frameStartTime, User user) {
        SaveFileResults saveFileResults = session.saveFile(in, user);
        VideoFrameRowKey videoFrameRowKey = new VideoFrameRowKey(artifactRowKey.toString(), frameStartTime);
        VideoFrame videoFrame = new VideoFrame(videoFrameRowKey);
        videoFrame.getMetadata()
                .setHdfsPath(saveFileResults.getFullPath());
        this.save(videoFrame, user);
    }

    public List<VideoFrame> findAllByArtifactRowKey(String rowKey, User user) {
        return this.findByRowStartsWith(rowKey, user);
    }

    public BufferedImage loadImage(ModelSession session, VideoFrame videoFrame, User user) {
        InputStream in = session.loadFile(videoFrame.getMetadata().getHdfsPath(), user);
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