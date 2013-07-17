package com.altamiracorp.reddawn.ucd.statement;

import com.altamiracorp.reddawn.model.*;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

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

    public List<Statement> findBySourceAndTargetRowKey(Session session, String sourceEntityRowKey, String targetEntityRowKey) {
        List<Statement> statements = findByRowStartsWith(session, sourceEntityRowKey);
        ArrayList<Statement> results = new ArrayList<Statement>();

        for (Statement statement : statements) {
            StatementRowKey rowKey = statement.getRowKey();
            if (rowKey.getSubjectRowKey().equals(sourceEntityRowKey) && rowKey.getObjectRowKey().equals(targetEntityRowKey)) {
                results.add(statement);
            }
        }

        return results;
    }

    public HashMap <String, HashSet<String>> findRelationshipDirection (List <String> rowKeyPrefixes, Session session){
        List <Row> rows = findByRowStartsWithList(rowKeyPrefixes, session);
        HashMap <String, HashSet<String>> relationshipMap = new HashMap<String, HashSet<String>>();

        for (String rowKey : rowKeyPrefixes){
            relationshipMap.put(rowKey, new HashSet<String>());
        }

        for (Row row : rows){
            StatementRowKey statementRowKey = ((Statement)row).getRowKey();
            if (rowKeyPrefixes.contains(statementRowKey.getObjectRowKey())){
                relationshipMap.get(statementRowKey.getSubjectRowKey()).add(statementRowKey.getObjectRowKey());
            }
        }

        return relationshipMap;
    }
}
