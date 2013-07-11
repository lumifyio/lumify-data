package com.altamiracorp.reddawn.search;

import com.altamiracorp.reddawn.ucd.term.TermRowKey;

public class TermSearchResult {
    private final String sign;
    private final TermRowKey rowKey;
    private final String conceptLabel;

    public TermSearchResult(String rowKey, String sign, String conceptLabel) {
        this.rowKey = new TermRowKey(rowKey);
        this.sign = sign;
        this.conceptLabel = conceptLabel;
    }

    public String getSign() {
        return sign;
    }

    public String getConceptLabel() {
        return conceptLabel;
    }

    public TermRowKey getRowKey() {
        return rowKey;
    }

    @Override
    public String toString() {
        return "rowKey: " + getRowKey() + ", sign: " + getSign() + ", concept label: " + getConceptLabel();
    }

}
