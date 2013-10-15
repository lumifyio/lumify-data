package com.altamiracorp.lumify.core.ingest;

import com.altamiracorp.lumify.core.ingest.video.VideoTranscript;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArtifactExtractedInfo {
    private static final String ROW_KEY = "rowKey";
    private static final String TEXT = "text";
    private static final String TITLE = "title";
    private static final String DATE = "date";
    private static final String TEXT_HDFS_PATH = "textHdfsPath";
    private static final String ONTOLOGY_CLASS_URI = "ontologyClassUri";
    private static final String RAW_HDFS_PATH = "rawHdfsPath";
    private static final String RAW = "raw";
    private static final String TEXT_ROW_KEY = "textRowKey";
    private static final String MP4_HDFS_PATH = "mp4HdfsPath";
    private static final String WEBM_HDFS_PATH = "webmHdfsPath";
    private static final String DETECTED_OBJECTS = "detectedObjects";
    private static final String VIDEO_TRANSCRIPT = "videoTranscript";
    private static final String AUDIO_HDFS_PATH = "audioHdfsPath";
    private static final String POSTER_FRAME_HDFS_PATH = "posterFrameHdfsPath";
    private static final String VIDEO_DURATION = "videoDuration";
    private static final String VIDEO_FRAMES = "videoFrames";
    private HashMap<String, Object> properties = new HashMap<String, Object>();

    public void mergeFrom(ArtifactExtractedInfo artifactExtractedInfo) {
        if (artifactExtractedInfo == null) {
            return;
        }
        for (Map.Entry<String, Object> prop : artifactExtractedInfo.properties.entrySet()) {
            if (prop.getKey().equals(VIDEO_TRANSCRIPT)) {
                this.properties.put(prop.getKey(), VideoTranscript.merge(getVideoTranscript(), (VideoTranscript) prop.getValue()));
            } else {
                this.properties.put(prop.getKey(), prop.getValue());
            }
        }
    }

    public void setRowKey(String rowKey) {
        properties.put(ROW_KEY, rowKey);
    }

    public String getRowKey() {
        return (String) properties.get(ROW_KEY);
    }

    public String getText() {
        return (String) properties.get(TEXT);
    }

    public void setText(String text) {
        properties.put(TEXT, text);
    }

    public void setTitle(String title) {
        properties.put(TITLE, title);
    }

    public String getTitle() {
        return (String) properties.get(TITLE);
    }

    public void setDate(Date date) {
        properties.put(DATE, date);
    }

    public void set(String key, Object val) {
        properties.put(key, val);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Object> prop : properties.entrySet()) {
            if ("raw".equals(prop.getKey())) {
                continue;
            }
            json.put(prop.getKey(), prop.getValue());
        }
        return json;
    }

    public void setTextHdfsPath(String textHdfsPath) {
        set(TEXT_HDFS_PATH, textHdfsPath);
    }

    public String getTextHdfsPath() {
        return (String) properties.get(TEXT_HDFS_PATH);
    }

    public void setOntologyClassUri(String ontologyClassUri) {
        set(ONTOLOGY_CLASS_URI, ontologyClassUri);
    }

    public String getOntologyClassUri() {
        return (String) properties.get(ONTOLOGY_CLASS_URI);
    }

    public void setRawHdfsPath(String rawHdfsPath) {
        set(RAW_HDFS_PATH, rawHdfsPath);
    }

    public String getRawHdfsPath() {
        return (String) properties.get(RAW_HDFS_PATH);
    }

    public void setRaw(byte[] raw) {
        set(RAW, raw);
    }

    public byte[] getRaw() {
        return (byte[]) properties.get(RAW);
    }

    public void setTextRowKey(String textRowKey) {
        properties.put(TEXT_ROW_KEY, textRowKey);
    }

    public String getTextRowKey() {
        return (String) properties.get(TEXT_ROW_KEY);
    }

    public void setMp4HdfsFilePath(String mp4HdfsFilePath) {
        properties.put(MP4_HDFS_PATH, mp4HdfsFilePath);
    }

    public String getMp4HdfsFilePath() {
        return (String) properties.get(MP4_HDFS_PATH);
    }

    public void setWebMHdfsFilePath(String webMHdfsFilePath) {
        properties.put(WEBM_HDFS_PATH, webMHdfsFilePath);
    }

    public String getWebMHdfsFilePath() {
        return (String) properties.get(WEBM_HDFS_PATH);
    }

    public void setAudioHdfsPath(String audioHdfsPath) {
        properties.put(AUDIO_HDFS_PATH, audioHdfsPath);
    }

    public String getAudioHdfsPath() {
        return (String) properties.get(AUDIO_HDFS_PATH);
    }

    public void setDetectedObjects(String detectedObjectsJsonString) {
        set(DETECTED_OBJECTS, detectedObjectsJsonString);
    }

    public String getDetectedObjects() {
        return (String) properties.get(DETECTED_OBJECTS);
    }

    public void setVideoTranscript(VideoTranscript videoTranscript) {
        set(VIDEO_TRANSCRIPT, videoTranscript);
    }

    public VideoTranscript getVideoTranscript() {
        return (VideoTranscript) properties.get(VIDEO_TRANSCRIPT);
    }

    public void setPosterFrameHdfsPath(String posterFrameHdfsPath) {
        set(POSTER_FRAME_HDFS_PATH, posterFrameHdfsPath);
    }

    public String getPosterFrameHdfsPath() {
        return (String) properties.get(POSTER_FRAME_HDFS_PATH);
    }

    public void setVideoDuration(long videoDuration) {
        set(VIDEO_DURATION, videoDuration);
    }

    public long getVideoDuration() {
        return (Long) properties.get(VIDEO_DURATION);
    }

    public void setVideoFrames(List<VideoFrame> videoFrames) {
        set(VIDEO_FRAMES, videoFrames);
    }

    public List<VideoFrame> getVideoFrames() {
        return (List<VideoFrame>) properties.get(VIDEO_FRAMES);
    }

    public static class VideoFrame {
        private final String hdfsPath;
        private final long frameStartTime;

        public VideoFrame(String hdfsPath, long frameStartTime) {
            this.hdfsPath = hdfsPath;
            this.frameStartTime = frameStartTime;
        }

        public String getHdfsPath() {
            return hdfsPath;
        }

        public long getFrameStartTime() {
            return frameStartTime;
        }
    }
}
