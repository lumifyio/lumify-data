package com.altamiracorp.lumify.model.geoNames;

import com.altamiracorp.lumify.model.RowKey;

public class GeoNameAdmin1CodeRowKey extends RowKey {
    public GeoNameAdmin1CodeRowKey(String rowKey) {
        super(rowKey);
    }

    public GeoNameAdmin1CodeRowKey(String countryCode, String admin1Code) {
        this(countryCode + "." + admin1Code);
    }
}
