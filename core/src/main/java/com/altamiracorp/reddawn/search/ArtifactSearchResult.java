package com.altamiracorp.reddawn.search;

import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;

import java.util.Date;

public class ArtifactSearchResult {
    private final String source;
    private final String rowKey;
    private final String subject;
    private final Date publishedDate;
    private final ArtifactType artifactType;
    private final String graphNodeId;

    public ArtifactSearchResult(String rowKey, String subject, Date publishedDate, String source, ArtifactType artifactType, String graphNodeId) {
        this.rowKey = rowKey;
        this.subject = subject;
        this.publishedDate = publishedDate;
        this.source = source;
        this.artifactType = artifactType;
        this.graphNodeId = graphNodeId;
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

    public String getGraphNodeId() {
        return graphNodeId;
    }

    @Override
    public String toString() {
        return "rowKey: " + getRowKey() + ", subject: " + getSubject() + ", publishedDate: " + getPublishedDate() + ", source: " + getSource() + ", graphNodeId: " + getGraphNodeId();
    }

    public ArtifactType getType() {
        return this.artifactType;
    }
}
