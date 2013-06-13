package com.altamiracorp.reddawn.ucd.artifactTermIndex;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;

import java.util.ArrayList;
import java.util.List;

public class ArtifactTermIndex extends Row<ArtifactTermIndexRowKey> {
    public static final String TABLE_NAME = "ArtifactTermIndex";

    public ArtifactTermIndex(ArtifactTermIndexRowKey rowKey) {
        super(TABLE_NAME, rowKey);
    }

    public ArtifactTermIndex() {
        super(TABLE_NAME);
    }

    public ArtifactTermIndex(String artifactRowKey) {
        this(new ArtifactTermIndexRowKey(artifactRowKey));
    }

    public List<TermRowKey> getTermMentions() {
        ArrayList<TermRowKey> termRowKeys = new ArrayList<TermRowKey>();
        for (ColumnFamily columnFamily : getColumnFamilies()) {
            if (columnFamily instanceof ArtifactTermIndexTerm) {
                ArtifactTermIndexTerm artifactTermIndexTerm = (ArtifactTermIndexTerm) columnFamily;
                termRowKeys.add(new TermRowKey(artifactTermIndexTerm.getColumnFamilyName()));
            }
        }
        return termRowKeys;
    }

    public void addTermMention(TermRowKey termRowKey, TermMention termMention) {
        ArtifactTermIndexTerm artifactTermIndexTerm = get(termRowKey.toString());
        if (artifactTermIndexTerm == null) {
            addColumnFamily(new ArtifactTermIndexTerm(termRowKey));
            artifactTermIndexTerm = get(termRowKey.toString());
        }
        artifactTermIndexTerm.set(termMention.getColumnFamilyName(), "");
    }
}
