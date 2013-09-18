package com.altamiracorp.lumify.model.videoFrames;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.Column;
import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.ModelSession;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.Row;
import com.altamiracorp.lumify.model.SaveFileResults;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;
import com.google.inject.Inject;

public class VideoFrameRepository extends Repository<VideoFrame> {
    @Inject
    public VideoFrameRepository(final ModelSession modelSession) {
        super(modelSession);
    }

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
        save(videoFrame, user);
    }

    public List<VideoFrame> findAllByArtifactRowKey(String rowKey, User user) {
        return findByRowStartsWith(rowKey, user);
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