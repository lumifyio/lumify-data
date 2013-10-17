package com.altamiracorp.lumify.core.model.geoNames;

import com.altamiracorp.lumify.core.model.Row;
import com.altamiracorp.lumify.core.model.RowKey;

public class GeoNamePostalCode extends Row<GeoNamePostalCodeRowKey> {

    public static final String TABLE_NAME = "atc_GeoNamePostalCode";

    public GeoNamePostalCode(GeoNamePostalCodeRowKey rowKey) {
        super(TABLE_NAME, rowKey);
    }

    public GeoNamePostalCode(RowKey rowKey) {
        super(TABLE_NAME, new GeoNamePostalCodeRowKey(rowKey.toString()));
    }

    public GeoNamePostalCodeMetadata getMetadata() {
        GeoNamePostalCodeMetadata metadata = get(GeoNamePostalCodeMetadata.NAME);
        if (metadata == null) {
            addColumnFamily(new GeoNamePostalCodeMetadata());
        }
        return get(GeoNamePostalCodeMetadata.NAME);
    }

}
