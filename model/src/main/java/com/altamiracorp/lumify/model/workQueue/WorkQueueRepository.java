package com.altamiracorp.lumify.model.workQueue;

import org.json.JSONObject;

public abstract class WorkQueueRepository {
    public static final String ARTIFACT_HIGHLIGHT_QUEUE_NAME = "artifactHighlight";
    public static final String TEXT_QUEUE_NAME = "text";
    public static final String PROCESSED_VIDEO_QUEUE_NAME = "processedVideo";
    public static final String STRUCTURED_DATA_ENTITY_QUEUE_NAME = "structuredDataEntity";

    public void pushArtifactHighlight(String artifactGraphVertexId) {
        JSONObject artifactHighlightJson = new JSONObject();
        artifactHighlightJson.put("graphVertexId", artifactGraphVertexId);
        pushOnQueue(ARTIFACT_HIGHLIGHT_QUEUE_NAME, artifactHighlightJson);
    }

    public void pushText(String artifactGraphVertexId) {
        JSONObject textQueueDataJson = new JSONObject();
        textQueueDataJson.put("graphVertexId", artifactGraphVertexId);
        pushOnQueue(TEXT_QUEUE_NAME, textQueueDataJson);
    }

    public void pushProcessedVideo(String artifactRowKey) {
        JSONObject json = new JSONObject();
        json.put("artifactRowKey", artifactRowKey);
        pushOnQueue(PROCESSED_VIDEO_QUEUE_NAME, json);
    }

    public void pushStructuredDataEntity (String artifactGraphVertexId) {
        JSONObject structuredDataEntityJson = new JSONObject();
        structuredDataEntityJson.put("graphVertexId", artifactGraphVertexId);
        pushOnQueue(STRUCTURED_DATA_ENTITY_QUEUE_NAME, structuredDataEntityJson);
    }

    protected abstract void pushOnQueue(String queueName, JSONObject json, String... extra);
}
