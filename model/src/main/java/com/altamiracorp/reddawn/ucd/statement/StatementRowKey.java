package com.altamiracorp.reddawn.ucd.statement;

import com.altamiracorp.reddawn.model.RowKey;
import com.altamiracorp.reddawn.model.RowKeyHelper;
import com.altamiracorp.reddawn.ucd.predicate.PredicateRowKey;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;

public class StatementRowKey extends RowKey {
    public StatementRowKey(String rowKey) {
        super(rowKey);
    }

    public StatementRowKey(TermRowKey subjectRowKey, PredicateRowKey knows, TermRowKey objectRowKey) {
        super(RowKeyHelper.buildMajor(subjectRowKey.toString(), knows.toString(), objectRowKey.toString()).toString());
    }
}
