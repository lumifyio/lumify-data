package com.altamiracorp.lumify.model.geoNames;

import com.altamiracorp.lumify.model.*;

import java.util.Collection;

public class GeoNameCountryInfoRepository extends Repository<GeoNameCountryInfo> {
    @Override
    public GeoNameCountryInfo fromRow(Row row) {
        GeoNameCountryInfo geoNameCountryInfo = new GeoNameCountryInfo(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(GeoNameAdmin1CodeMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                geoNameCountryInfo.addColumnFamily(new GeoNameCountryInfoMetadata().addColumns(columns));
            } else {
                geoNameCountryInfo.addColumnFamily(columnFamily);
            }
        }
        return geoNameCountryInfo;
    }

    @Override
    public Row toRow(GeoNameCountryInfo geoName) {
        return geoName;
    }

    @Override
    public String getTableName() {
        return GeoNameCountryInfo.TABLE_NAME;
    }

    public GeoNameCountryInfo findByCountryCode(ModelSession session, String countryCode) {
        return findByRowKey(session, new GeoNameCountryInfoRowKey(countryCode).toString());
    }
}
