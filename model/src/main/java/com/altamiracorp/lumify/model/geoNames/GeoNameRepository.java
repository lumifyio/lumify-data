package com.altamiracorp.lumify.model.geoNames;

import com.altamiracorp.lumify.model.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GeoNameRepository extends Repository<GeoName> {
    @Override
    public GeoName fromRow(Row row) {
        GeoName geoName = new GeoName(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(GeoNameMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                geoName.addColumnFamily(new GeoNameMetadata().addColumns(columns));
            } else {
                geoName.addColumnFamily(columnFamily);
            }
        }
        return geoName;
    }

    @Override
    public Row toRow(GeoName geoName) {
        return geoName;
    }

    @Override
    public String getTableName() {
        return GeoName.TABLE_NAME;
    }

    public GeoName findBestMatch(ModelSession session, String name) {
        List<GeoName> matches = this.findByRowStartsWith(session, name.toLowerCase() + RowKeyHelper.MINOR_FIELD_SEPARATOR);
        if (matches.size() == 0) {
            return null;
        }
        Collections.sort(matches, new GeoNamePopulationComparator());
        return matches.get(matches.size() - 1);
    }
}
