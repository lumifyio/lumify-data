package com.altamiracorp.reddawn.search;

public class ArtifactSearchResult {
    private String rowKey;
    private String subject;

    public ArtifactSearchResult(String rowKey, String subject) {
        this.rowKey = rowKey;
        this.subject = subject;
    }

    public String getRowKey() {
        return rowKey;
    }

    public String getSubject() {
        return subject;
    }
}
