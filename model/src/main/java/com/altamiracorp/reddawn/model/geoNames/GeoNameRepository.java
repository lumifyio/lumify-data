package com.altamiracorp.reddawn.model.geoNames;

import com.altamiracorp.reddawn.model.*;
import com.altamiracorp.reddawn.model.workspace.Workspace;
import com.altamiracorp.reddawn.model.workspace.WorkspaceContent;

import java.util.Collection;

public class GeoNameRepository extends Repository<GeoName> {
    @Override
    public GeoName fromRow(Row row) {
        GeoName artifact = new GeoName(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(GeoNameMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                artifact.addColumnFamily(new GeoNameMetadata().addColumns(columns));
            } else {
                artifact.addColumnFamily(columnFamily);
            }
        }
        return artifact;
    }

    @Override
    public Row toRow(GeoName geoName) {
        return geoName;
    }

    @Override
    public String getTableName() {
        return GeoName.TABLE_NAME;
    }
}
