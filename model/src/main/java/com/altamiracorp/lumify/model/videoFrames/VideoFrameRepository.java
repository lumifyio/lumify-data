package com.altamiracorp.lumify.model.videoFrames;

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

    public void saveVideoFrame(Session session, ArtifactRowKey artifactRowKey, InputStream in, long frameStartTime) {
        SaveFileResults saveFileResults = session.saveFile(in);
        VideoFrameRowKey videoFrameRowKey = new VideoFrameRowKey(artifactRowKey.toString(), frameStartTime);
        VideoFrame videoFrame = new VideoFrame(videoFrameRowKey);
        videoFrame.getMetadata()
                .setHdfsPath(saveFileResults.getFullPath());
        this.save(session, videoFrame);
    }

    public List<VideoFrame> findAllByArtifactRowKey(Session session, String rowKey) {
        return this.findByRowStartsWith(session, rowKey);
    }

    public BufferedImage loadImage(Session session, VideoFrame videoFrame) {
        InputStream in = session.loadFile(videoFrame.getMetadata().getHdfsPath());
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