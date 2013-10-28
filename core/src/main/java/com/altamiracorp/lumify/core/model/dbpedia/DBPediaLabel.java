package com.altamiracorp.lumify.core.model.dbpedia;

import com.altamiracorp.bigtable.model.ColumnFamily;
import com.altamiracorp.bigtable.model.Value;

public class DBPediaLabel extends ColumnFamily {
    public static final String NAME = "Label";
    public static final String LABEL_COLUMN = "Label";

    public DBPediaLabel() {
        super(NAME);
    }

    public String getLabel() {
        return Value.toString(get(LABEL_COLUMN));
    }

    public DBPediaLabel setLabel(String label) {
        set(LABEL_COLUMN, label);
        return this;
    }

}
