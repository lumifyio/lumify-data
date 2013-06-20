package com.altamiracorp.reddawn.model.geoNames;

import com.altamiracorp.reddawn.model.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    public GeoName findBestMatch(Session session, String name) {
        List<GeoName> matches = this.findByRowStartsWith(session, name.toLowerCase() + RowKeyHelper.MINOR_FIELD_SEPARATOR);
        if (matches.size() == 0) {
            return null;
        }
        Collections.sort(matches, new GeoNamePopulationComparator());
        return matches.get(matches.size() - 1);
    }
}
