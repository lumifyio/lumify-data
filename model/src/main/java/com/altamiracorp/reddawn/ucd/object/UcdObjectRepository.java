package com.altamiracorp.reddawn.ucd.object;

import com.altamiracorp.reddawn.model.Column;
import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Repository;
import com.altamiracorp.reddawn.model.Row;

import java.util.Collection;


public class UcdObjectRepository extends Repository<UcdObject> {
    @Override
    public Row toRow(UcdObject ucdObject) {
        return ucdObject;
    }

    @Override
    public String getTableName() {
        return UcdObject.TABLE_NAME;
    }

    @Override
    public UcdObject fromRow(Row row) {
        UcdObject ucdObject = new UcdObject(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(UcdObjectObjectStatement.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                ucdObject.addColumnFamily(new UcdObjectObjectStatement().addColumns(columns));
            } else {
                ucdObject.addColumnFamily(columnFamily);
            }
        }
        return ucdObject;
    }
}
