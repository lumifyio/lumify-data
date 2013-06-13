package com.altamiracorp.reddawn.ucd.source;

import com.altamiracorp.reddawn.model.Column;
import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Repository;
import com.altamiracorp.reddawn.model.Row;

import java.util.Collection;

public class SourceRepository extends Repository<Source> {
    @Override
    public Source fromRow(Row row) {
        Source source = new Source(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(SourceMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                source.addColumnFamily(new SourceMetadata().addColumns(columns));
            } else {
                source.addColumnFamily(columnFamily);
            }
        }
        return source;
    }

    @Override
    public Row toRow(Source source) {
        return source;
    }

    @Override
    public String getTableName() {
        return Source.TABLE_NAME;
    }
}
