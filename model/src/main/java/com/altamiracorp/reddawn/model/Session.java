package com.altamiracorp.reddawn.model;

import com.altamiracorp.reddawn.model.workspace.Workspace;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndex;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.source.Source;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import com.altamiracorp.reddawn.ucd.term.Term;

import java.util.List;

public abstract class Session {
    private QueryUser queryUser;

    public Session(QueryUser queryUser) {
        this.queryUser = queryUser;
    }

    abstract void save(Row row);

    abstract List<Row> findByRowKeyRange(String tableName, String keyStart, String keyEnd, QueryUser queryUser);

    abstract List<Row> findByRowStartsWith(String tableName, String rowKeyPrefix, QueryUser queryUser);

    abstract Row findByRowKey(String tableName, String rowKey, QueryUser queryUser);

    abstract void initializeTable(String tableName);

    abstract void deleteTable(String tableName);

    public void initializeTables() {
        initializeTable(Artifact.TABLE_NAME);
        initializeTable(Term.TABLE_NAME);
        initializeTable(Source.TABLE_NAME);
        initializeTable(Sentence.TABLE_NAME);
        initializeTable(ArtifactTermIndex.TABLE_NAME);
        initializeTable(Statement.TABLE_NAME);

        initializeTable(Workspace.TABLE_NAME);
    }

    public QueryUser getQueryUser() {
        return this.queryUser;
    }

    public void deleteTables() {
        deleteTable(Artifact.TABLE_NAME);
        deleteTable(Term.TABLE_NAME);
        deleteTable(Source.TABLE_NAME);
        deleteTable(Sentence.TABLE_NAME);
        deleteTable(ArtifactTermIndex.TABLE_NAME);
        deleteTable(Statement.TABLE_NAME);

        deleteTable(Workspace.TABLE_NAME);
    }
}
