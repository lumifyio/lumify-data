package com.altamiracorp.reddawn.model.geoNames;

import com.altamiracorp.reddawn.model.RowKey;
import com.altamiracorp.reddawn.model.RowKeyHelper;

public class GeoNameRowKey extends RowKey {
    public GeoNameRowKey(String rowKey) {
        super(rowKey);
    }

    public GeoNameRowKey(String name, String id) {
        super(RowKeyHelper.buildMinor(name.toLowerCase(), id));
    }
}
