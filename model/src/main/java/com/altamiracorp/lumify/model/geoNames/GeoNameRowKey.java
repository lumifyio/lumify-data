package com.altamiracorp.lumify.model.geoNames;

import com.altamiracorp.lumify.model.RowKey;
import com.altamiracorp.lumify.core.util.RowKeyHelper;

public class GeoNameRowKey extends RowKey {
    public GeoNameRowKey(String rowKey) {
        super(rowKey);
    }

    public GeoNameRowKey(String name, String id) {
        super(RowKeyHelper.buildMinor(name.toLowerCase(), id));
    }
}
