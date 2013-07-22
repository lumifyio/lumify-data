package com.altamiracorp.reddawn.ucd.object;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;

import java.util.ArrayList;
import java.util.List;

public class UcdObject extends Row<UcdObjectRowKey> {
    public static final String TABLE_NAME = "Object";

    public UcdObject(RowKey rowKey) {
        super(TABLE_NAME, new UcdObjectRowKey(rowKey.toString()));
    }

    public UcdObject(String rowKey) {
        super(TABLE_NAME, new UcdObjectRowKey(rowKey));
    }

    public UcdObject() {
        super(TABLE_NAME);
    }

    public UcdObject(String sign, String modelKey, String conceptLabel) {
        this(new UcdObjectRowKey(sign, modelKey, conceptLabel));
    }

    public UcdObjectObjectStatement getUcdObjectObjectStatement() {
        UcdObjectObjectStatement ucdObjectObjectStatement = get(UcdObjectObjectStatement.NAME);
        if (ucdObjectObjectStatement == null) {
            addColumnFamily(new UcdObjectObjectStatement());
        }
        return get(UcdObjectObjectStatement.NAME);
    }

    public UcdObject addObjectStatement(UcdObjectObjectStatement objectStatement) {
        this.addColumnFamily(objectStatement);
        return this;
    }
}
