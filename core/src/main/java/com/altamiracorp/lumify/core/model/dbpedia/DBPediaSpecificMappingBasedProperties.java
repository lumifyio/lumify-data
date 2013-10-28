package com.altamiracorp.lumify.core.model.dbpedia;

import com.altamiracorp.bigtable.model.ColumnFamily;

public class DBPediaSpecificMappingBasedProperties extends ColumnFamily {
    public static final String NAME = "SpecificMappingBasedProperties";

    public DBPediaSpecificMappingBasedProperties() {
        super(NAME);
    }

}
