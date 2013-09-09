package com.altamiracorp.lumify.model.geoNames;

import com.altamiracorp.lumify.model.*;

import java.util.Collection;

public class GeoNamePostalCodeRepository extends Repository<GeoNamePostalCode>{

    @Override
    public GeoNamePostalCode fromRow(Row row) {
        GeoNamePostalCode geoNamePostalCode = new GeoNamePostalCode(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(GeoNamePostalCodeMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                geoNamePostalCode.addColumnFamily(new GeoNamePostalCodeMetadata().addColumns(columns));
            } else {
                geoNamePostalCode.addColumnFamily(columnFamily);
            }
        }
        return geoNamePostalCode;
    }

    @Override
    public Row toRow(GeoNamePostalCode geoNamePostalCode) {
        return geoNamePostalCode;
    }

    @Override
    public String getTableName() {
        return GeoNamePostalCode.TABLE_NAME;
    }

    public GeoNamePostalCode findByCountryAndPostalCode (Session session, String countryCode, String postalCode) {
        return this.findByRowKey(session,new GeoNamePostalCodeRowKey(countryCode,postalCode).toString());
    }

    public GeoNamePostalCode findByUSZipCode (Session session, String zipCode) {
        return findByCountryAndPostalCode(session,"US",zipCode);
    }
}
