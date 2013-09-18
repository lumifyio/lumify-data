package com.altamiracorp.lumify.model.geoNames;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.Column;
import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.Row;

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

    public GeoNameCountryInfo findByCountryCode(String countryCode, User user) {
        return findByRowKey(new GeoNameCountryInfoRowKey(countryCode).toString(), user);
    }
}
