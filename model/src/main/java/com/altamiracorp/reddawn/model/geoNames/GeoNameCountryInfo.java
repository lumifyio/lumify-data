package com.altamiracorp.reddawn.model.geoNames;

import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;

public class GeoNameCountryInfo extends Row<GeoNameCountryInfoRowKey> {
    public static final String TABLE_NAME = "atc_GeoNameCountryInfo";

    public GeoNameCountryInfo(GeoNameCountryInfoRowKey rowKey) {
        super(TABLE_NAME, rowKey);
    }

    public GeoNameCountryInfo(RowKey rowKey) {
        super(TABLE_NAME, new GeoNameCountryInfoRowKey(rowKey.toString()));
    }

    public GeoNameCountryInfoMetadata getMetadata() {
        GeoNameCountryInfoMetadata metadata = get(GeoNameCountryInfoMetadata.NAME);
        if (metadata == null) {
            addColumnFamily(new GeoNameCountryInfoMetadata());
        }
        return get(GeoNameCountryInfoMetadata.NAME);
    }
}
