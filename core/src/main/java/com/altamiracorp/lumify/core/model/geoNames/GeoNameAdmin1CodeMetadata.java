package com.altamiracorp.lumify.core.model.geoNames;

import com.altamiracorp.lumify.core.model.ColumnFamily;
import com.altamiracorp.lumify.core.model.Value;

public class GeoNameAdmin1CodeMetadata extends ColumnFamily {
    public static final String NAME = "Metadata";
    private static final String TITLE_COLUMN = "title";

    public GeoNameAdmin1CodeMetadata() {
        super(NAME);
    }

    public String getTitle() {
        return Value.toString(get(TITLE_COLUMN));
    }

    public GeoNameAdmin1CodeMetadata setTitle(String title) {
        set(TITLE_COLUMN, title);
        return this;
    }
}
