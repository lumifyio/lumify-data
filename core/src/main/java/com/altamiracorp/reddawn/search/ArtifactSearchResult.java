package com.altamiracorp.reddawn.search;

import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;

import java.util.Date;

public class ArtifactSearchResult {
    private final String source;
    private final String rowKey;
    private final String subject;
    private final Date publishedDate;
    private final ArtifactType artifactType;

    public ArtifactSearchResult(String rowKey, String subject, Date publishedDate, String source, ArtifactType artifactType) {
        this.rowKey = rowKey;
        this.subject = subject;
        this.publishedDate = publishedDate;
        this.source = source;
        this.artifactType = artifactType;
    }

    public String getRowKey() {
        return rowKey;
    }

    public String getSubject() {
        return subject;
    }

    public Date getPublishedDate() {
        return this.publishedDate;
    }

    public String getSource() {
        return this.source;
    }

    @Override
    public String toString() {
        return "rowKey: " + getRowKey() + ", subject: " + getSubject() + ", publishedDate: " + getPublishedDate() + ", source: " + getSource();
    }

    public ArtifactType getType() {
        return this.artifactType;
    }
}
