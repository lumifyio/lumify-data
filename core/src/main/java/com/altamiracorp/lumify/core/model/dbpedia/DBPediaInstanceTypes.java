package com.altamiracorp.lumify.core.model.dbpedia;

import com.altamiracorp.bigtable.model.ColumnFamily;

public class DBPediaInstanceTypes extends ColumnFamily {
    public static final String NAME = "InstanceTypes";

    public DBPediaInstanceTypes() {
        super(NAME);
    }

}
