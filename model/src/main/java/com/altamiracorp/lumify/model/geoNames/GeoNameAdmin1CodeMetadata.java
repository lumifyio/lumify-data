package com.altamiracorp.lumify.model.geoNames;

import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.Value;

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
