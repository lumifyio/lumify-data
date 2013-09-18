package com.altamiracorp.lumify.model.geoNames;

import com.altamiracorp.lumify.model.*;

import java.util.Collection;

public class GeoNameAdmin1CodeRepository extends Repository<GeoNameAdmin1Code> {
    @Override
    public GeoNameAdmin1Code fromRow(Row row) {
        GeoNameAdmin1Code geoNameAdmin1Code = new GeoNameAdmin1Code(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(GeoNameAdmin1CodeMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                geoNameAdmin1Code.addColumnFamily(new GeoNameAdmin1CodeMetadata().addColumns(columns));
            } else {
                geoNameAdmin1Code.addColumnFamily(columnFamily);
            }
        }
        return geoNameAdmin1Code;
    }

    @Override
    public Row toRow(GeoNameAdmin1Code geoName) {
        return geoName;
    }

    @Override
    public String getTableName() {
        return GeoNameAdmin1Code.TABLE_NAME;
    }

    public GeoNameAdmin1Code findByCountryAndAdmin1Code(ModelSession session, String countryCode, String admin1Code) {
        return findByRowKey(session, new GeoNameAdmin1CodeRowKey(countryCode, admin1Code).toString());
    }
}
