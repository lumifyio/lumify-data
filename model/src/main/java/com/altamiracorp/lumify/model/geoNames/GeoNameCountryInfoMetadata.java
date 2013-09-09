package com.altamiracorp.lumify.model.geoNames;

import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.Value;

public class GeoNameCountryInfoMetadata extends ColumnFamily {
    public static final String NAME = "Metadata";
    private static final String TITLE_COLUMN = "title";

    public GeoNameCountryInfoMetadata() {
        super(NAME);
    }

    public String getTitle() {
        return Value.toString(get(TITLE_COLUMN));
    }

    public GeoNameCountryInfoMetadata setTitle(String title) {
        set(TITLE_COLUMN, title);
        return this;
    }
}
