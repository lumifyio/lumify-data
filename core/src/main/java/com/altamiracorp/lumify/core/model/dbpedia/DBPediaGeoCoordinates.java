package com.altamiracorp.lumify.core.model.dbpedia;

import com.altamiracorp.bigtable.model.ColumnFamily;

public class DBPediaGeoCoordinates extends ColumnFamily {
    public static final String NAME = "GeoCoordinates";

    public DBPediaGeoCoordinates() {
        super(NAME);
    }

}
