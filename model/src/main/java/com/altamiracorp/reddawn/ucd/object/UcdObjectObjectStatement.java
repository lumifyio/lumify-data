package com.altamiracorp.reddawn.ucd.object;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.ucd.statement.Statement;

public class UcdObjectObjectStatement extends ColumnFamily {
    public static final String NAME = "ObjectStatement";

    public UcdObjectObjectStatement() {
        super(NAME);
    }

    public void addStatement(Statement statement) {
        set(statement.getRowKey().toString(), statement.getRowKey().toString());
    }
}
