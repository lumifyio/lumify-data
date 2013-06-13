package com.altamiracorp.reddawn.model.workspace;

import com.altamiracorp.reddawn.model.*;

import java.util.Collection;

public class WorkspaceRepository extends Repository<Workspace> {
    @Override
    public Workspace fromRow(Row row) {
        Workspace artifact = new Workspace(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(WorkspaceContent.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                artifact.addColumnFamily(new WorkspaceContent().addColumns(columns));
            } else {
                artifact.addColumnFamily(columnFamily);
            }
        }
        return artifact;
    }

    @Override
    public Row toRow(Workspace workspace) {
        return workspace;
    }

    @Override
    public String getTableName() {
        return Workspace.TABLE_NAME;
    }

    public Collection<Workspace> findByUserId(Session session, String userId) {
        return findByRowStartsWith(session, userId);
    }
}
