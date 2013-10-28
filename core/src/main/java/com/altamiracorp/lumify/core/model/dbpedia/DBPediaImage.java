package com.altamiracorp.lumify.core.model.dbpedia;

import com.altamiracorp.bigtable.model.ColumnFamily;

public class DBPediaImage extends ColumnFamily {
    public static final String NAME = "Image";

    public DBPediaImage() {
        super(NAME);
    }

}
