package com.altamiracorp.reddawn.ucd.statement;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;

import java.util.ArrayList;
import java.util.List;

public class Statement extends Row<StatementRowKey> {
    public static final String TABLE_NAME = "Statement";

    public Statement(StatementRowKey rowKey) {
        super(TABLE_NAME, rowKey);
    }

    public Statement(RowKey rowKey) {
        super(TABLE_NAME, new StatementRowKey(rowKey.toString()));
    }

    public List<StatementArtifact> getStatementArtifacts() {
        ArrayList<StatementArtifact> statementArtifacts = new ArrayList<StatementArtifact>();
        for (ColumnFamily columnFamily : getColumnFamilies()) {
            if (columnFamily instanceof StatementArtifact) {
                statementArtifacts.add((StatementArtifact) columnFamily);
            }
        }
        return statementArtifacts;
    }

    public Statement addStatementArtifact(StatementArtifact statementArtifact) {
        this.addColumnFamily(statementArtifact);
        return this;
    }
}
