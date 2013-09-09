package com.altamiracorp.lumify.vaast.model.subFrames;

import com.altamiracorp.lumify.model.*;

import java.io.InputStream;
import java.util.Collection;

public class SubFrameRepository extends Repository<SubFrame> {

    public SubFrame saveSubFrame(Session session, String rowKey, InputStream in) {
        SaveFileResults saveFileResults = session.saveFile(in);
        SubFrameRowKey subFrameRowKey = new SubFrameRowKey(rowKey);
        SubFrame subFrame = new SubFrame(subFrameRowKey);
        subFrame.getMetadata()
                .setHdfsPath(saveFileResults.getFullPath());
        this.save(session, subFrame);

        return subFrame;
    }

    public void saveSparseSubFrame (Session session, SubFrame subFrame, InputStream sparseIn) {
        SaveFileResults saveFileResults = session.saveFile(sparseIn);
        subFrame.getMetadata()
                .setSparseHdfsPath(saveFileResults.getFullPath());
        this.save(session,subFrame);
    }

    @Override
    public SubFrame fromRow(Row row) {
        SubFrame subFrame = new SubFrame(new SubFrameRowKey(row.getRowKey().toString()));
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            String columnFamilyName = columnFamily.getColumnFamilyName();
            if (columnFamilyName.equals(SubFrameMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                subFrame.addColumnFamily(new SubFrameMetadata().addColumns(columns));
            } else {
                subFrame.addColumnFamily(columnFamily);
            }
        }
        return subFrame;
    }

    public InputStream getRaw(Session session, SubFrame frame) {
        return getRaw(session,frame.getMetadata().getHdfsPath());

    }

    public InputStream getSparseRaw(Session session, SubFrame frame) {
       return getRaw(session,frame.getMetadata().getSparseHdfsPath());

    }

    private InputStream getRaw(Session session, String hdfsPath) {
        if (hdfsPath != null) {
            return session.loadFile(hdfsPath);
        }
        return null;
    }

    @Override
    public Row toRow(SubFrame subFrame) {
        return subFrame;
    }

    @Override
    public String getTableName() {
        return SubFrame.TABLE_NAME;
    }
}
