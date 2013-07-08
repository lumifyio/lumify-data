package com.altamiracorp.reddawn.ucd.statement;

import com.altamiracorp.reddawn.model.*;

import java.util.Collection;

public class StatementRepository extends Repository<Statement> {
    @Override
    public Statement fromRow(Row row) {
        Statement statement = new Statement(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (isRowAStatementArtifact(columnFamily)) {
                Collection<Column> columns = columnFamily.getColumns();
                statement.addColumnFamily(new StatementArtifact(columnFamily.getColumnFamilyName()).addColumns(columns));
            } else {
                statement.addColumnFamily(columnFamily);
            }
        }
        return statement;
    }

    private boolean isRowAStatementArtifact(ColumnFamily columnFamily) {
        return columnFamily.get(StatementArtifact.ARTIFACT_KEY) != null;
    }

    @Override
    public Row toRow(Statement statement) {
        return statement;
    }

    @Override
    public String getTableName() {
        return Statement.TABLE_NAME;
    }

    public void save(Session session, Statement statement) {
        super.save(session, statement);
    }
}
