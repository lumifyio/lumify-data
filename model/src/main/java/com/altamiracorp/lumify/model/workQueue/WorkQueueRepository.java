package com.altamiracorp.lumify.model.workQueue;

import org.json.JSONObject;

public abstract class WorkQueueRepository {
    public void pushArtifactHighlight(String artifactGraphVertexId) {
        JSONObject artifactHighlightJson = new JSONObject();
        artifactHighlightJson.put("graphVertexId", artifactGraphVertexId);
        pushOnQueue("artifactHighlight", artifactHighlightJson);
    }

    public void pushText(String artifactGraphVertexId) {
        JSONObject textQueueDataJson = new JSONObject();
        textQueueDataJson.put("graphVertexId", artifactGraphVertexId);
        pushOnQueue("text", textQueueDataJson);
    }

    public void pushProcessedVideo(String artifactRowKey) {
        JSONObject json = new JSONObject();
        json.put("artifactRowKey", artifactRowKey);
        pushOnQueue("processedVideo", json);
    }

    protected abstract void pushOnQueue(String queueName, JSONObject json, String... extra);
}
