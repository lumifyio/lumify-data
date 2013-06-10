package com.altamiracorp.reddawn.ucd.artifactTermIndex;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;

public class ArtifactTermIndexTerm extends ColumnFamily {
    public ArtifactTermIndexTerm(TermRowKey termRowKey) {
        super(termRowKey.toString());
    }
}
