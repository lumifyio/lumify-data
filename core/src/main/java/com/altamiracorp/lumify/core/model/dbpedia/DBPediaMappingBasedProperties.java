package com.altamiracorp.lumify.core.model.dbpedia;

import com.altamiracorp.bigtable.model.ColumnFamily;

public class DBPediaMappingBasedProperties extends ColumnFamily {
    public static final String NAME = "MappingBasedProperties";

    public DBPediaMappingBasedProperties() {
        super(NAME);
    }

}
