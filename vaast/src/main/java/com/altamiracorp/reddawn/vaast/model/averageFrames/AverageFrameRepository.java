package com.altamiracorp.reddawn.vaast.model.averageFrames;

import com.altamiracorp.reddawn.model.*;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrame;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrameDetectedObjects;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrameMetadata;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrameRowKey;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;


public class AverageFrameRepository extends Repository<AverageFrame>{

    public AverageFrame saveAverageFrame(Session session, String rowKey, InputStream in) {
        SaveFileResults saveFileResults = session.saveFile(in);
        AverageFrameRowKey averageFrameRowKey = new AverageFrameRowKey(rowKey);
        AverageFrame averageFrame = new AverageFrame(averageFrameRowKey);
        averageFrame.getMetadata()
                .setHdfsPath(saveFileResults.getFullPath());
        this.save(session, averageFrame);

        return averageFrame;
    }

    @Override
    public AverageFrame fromRow(Row row) {
        AverageFrame averageFrame = new AverageFrame(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            String columnFamilyName = columnFamily.getColumnFamilyName();
            if (columnFamilyName.equals(AverageFrameMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                averageFrame.addColumnFamily(new AverageFrameMetadata().addColumns(columns));
            } else {
                averageFrame.addColumnFamily(columnFamily);
            }
        }
        return averageFrame;
    }

    public InputStream getRaw(Session session, AverageFrame frame) {
        String hdfsPath = frame.getMetadata().getHdfsPath();
        if (hdfsPath != null) {
            return session.loadFile(hdfsPath);
        }
        return null;
    }

    @Override
    public Row toRow(AverageFrame averageFrame) {
        return averageFrame;
    }

    @Override
    public String getTableName() {
        return AverageFrame.TABLE_NAME;
    }
}
