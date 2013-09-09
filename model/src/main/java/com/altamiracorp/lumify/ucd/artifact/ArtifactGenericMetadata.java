package com.altamiracorp.lumify.ucd.artifact;

import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.Value;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ArtifactGenericMetadata extends ColumnFamily {
    private static final DateFormat DF = new SimpleDateFormat("ddHHmm'Z' MMM yy");

    public static final String NAME = "Generic_Metadata";
    public static final String AUTHOR = "author";
    public static final String CHARSET = "charset";
    public static final String CATEGORY = "category";
    public static final String DOCUMENT_DTG = "document_dtg";
    public static final String DOCUMENT_TYPE = "document_type";
    public static final String EXTRACTED_TEXT_HDFS_PATH = "extracted_text_hdfs_path";
    public static final String EXTERNAL_URL = "external_url";
    public static final String FILE_EXTENSION = "file_extension";
    public static final String FILE_NAME = "file_name";
    public static final String FILE_SIZE = "file_size";
    public static final String FILE_TIMESTAMP = "file_timestamp";
    public static final String HDFS_FILE_PATH = "hdfs_file_path";
    public static final String LANGUAGE = "language";
    public static final String LOAD_TIMESTAMP = "load_timestamp";
    public static final String LOAD_TYPE = "load_type";
    public static final String MIME_TYPE = "mime_type";
    public static final String MAPPING_JSON = "atc:mapping_json";
    public static final String SOURCE = "source";
    public static final String SOURCE_SUBTYPE = "source_subtype";
    public static final String SOURCE_TYPE = "source_type";
    public static final String SUBJECT = "subject";
    public static final String MP4_HDFS_FILE_PATH = "atc:mp4_hdfs_file_path";
    public static final String WEBM_HDFS_FILE_PATH = "atc:webm_hdfs_file_path";
    public static final String VIDEO_PREVIEW_IMAGE_HDFS_FILE_PATH = "atc:video_preview_image_hdfs_file_path";
    public static final String POSTER_FRAME_HDFS_FILE_PATH = "atc:poster_frame_hdfs_file_path";
    public static final String AUDIO_HDFS_FILE_PATH = "atc:audio_hdfs_file_path";
    public static final String GRAPH_VERTEX_ID = "atc:graph_vertex_id";

    public ArtifactGenericMetadata() {
        super(NAME);
    }

    public String getAuthor() {
        return Value.toString(get(AUTHOR));
    }

    public ArtifactGenericMetadata setAuthor(String author) {
        set(AUTHOR, author);
        return this;
    }

    public String getCharset() {
        return Value.toString(get(CHARSET));
    }

    public ArtifactGenericMetadata setCharset(String charset) {
        set(CHARSET, charset);
        return this;
    }

    public String getCategory() {
        return Value.toString(get(CATEGORY));
    }

    public ArtifactGenericMetadata setCategory(String category) {
        set(CATEGORY, category);
        return this;
    }

    public String getDocumentDtg() {
        return Value.toString(get(DOCUMENT_DTG));
    }

    public Date getDocumentDtgDate() {
        String documentDtg = getDocumentDtg();
        if (documentDtg == null) {
            return null;
        }
        try {
            return DF.parse(documentDtg);
        } catch (ParseException e) {
            throw new RuntimeException("Could not parse document dtg date: " + documentDtg, e);
        }
    }

    public ArtifactGenericMetadata setDocumentDtg(String documentDtg) {
        set(DOCUMENT_DTG, documentDtg);
        return this;
    }

    public ArtifactGenericMetadata setDocumentDtg(Date documentDtg) {
        set(DOCUMENT_DTG, DF.format(documentDtg).toUpperCase());
        return this;
    }

    public String getDocumentType() {
        return Value.toString(get(DOCUMENT_TYPE));
    }

    public ArtifactGenericMetadata setDocumentType(String documentType) {
        set(DOCUMENT_TYPE, documentType);
        return this;
    }

    public String getExtractedTextHdfsPath() {
        return Value.toString(get(EXTRACTED_TEXT_HDFS_PATH));
    }

    public ArtifactGenericMetadata setExtractedTextHdfsPath(String extractedTextHdfsPath) {
        set(EXTRACTED_TEXT_HDFS_PATH, extractedTextHdfsPath);
        return this;
    }

    public String getExternalUrl() {
        return Value.toString(get(EXTERNAL_URL));
    }

    public ArtifactGenericMetadata setExternalUrl(String externalUrl) {
        set(EXTERNAL_URL, externalUrl);
        return this;
    }

    public String getFileExtension() {
        return Value.toString(get(FILE_EXTENSION));
    }

    public ArtifactGenericMetadata setFileExtension(String fileExtension) {
        set(FILE_EXTENSION, fileExtension);
        return this;
    }

    public String getFileName() {
        return Value.toString(get(FILE_NAME));
    }

    public ArtifactGenericMetadata setFileName(String fileName) {
        set(FILE_NAME, fileName);
        return this;
    }

    public Long getFileSize() {
        return Value.toLong(get(FILE_SIZE));
    }

    public ArtifactGenericMetadata setFileSize(Long fileSize) {
        set(FILE_SIZE, fileSize);
        return this;
    }

    public Long getFileTimestamp() {
        return Value.toLong(get(FILE_TIMESTAMP));
    }

    public ArtifactGenericMetadata setFileTimestamp(Long fileTimestamp) {
        set(FILE_TIMESTAMP, fileTimestamp);
        return this;
    }

    public Date getFileTimestampDate() {
        Long l = getFileTimestamp();
        if (l == null) {
            return null;
        }
        return new Date(l);
    }

    public String getHdfsFilePath() {
        return Value.toString(get(HDFS_FILE_PATH));
    }

    public ArtifactGenericMetadata setHdfsFilePath(String hdfsFilePath) {
        set(HDFS_FILE_PATH, hdfsFilePath);
        return this;
    }

    public String getMp4HdfsFilePath() {
        return Value.toString(get(MP4_HDFS_FILE_PATH));
    }

    public ArtifactGenericMetadata setMp4HdfsFilePath(String mp4HdfsFilePath) {
        set(MP4_HDFS_FILE_PATH, mp4HdfsFilePath);
        return this;
    }

    public String getWebmHdfsFilePath() {
        return Value.toString(get(WEBM_HDFS_FILE_PATH));
    }

    public ArtifactGenericMetadata setWebmHdfsFilePath(String webmHdfsFilePath) {
        set(WEBM_HDFS_FILE_PATH, webmHdfsFilePath);
        return this;
    }

    public String getVideoPreviewImageHdfsFilePath() {
        return Value.toString(get(VIDEO_PREVIEW_IMAGE_HDFS_FILE_PATH));
    }

    public ArtifactGenericMetadata setVideoPreviewImageHdfsFilePath(String previewImageHdfsFilePath) {
        set(VIDEO_PREVIEW_IMAGE_HDFS_FILE_PATH, previewImageHdfsFilePath);
        return this;
    }

    public String getPosterFrameHdfsFilePath() {
        return Value.toString(get(POSTER_FRAME_HDFS_FILE_PATH));
    }

    public ArtifactGenericMetadata setPosterFrameHdfsFilePath(String posterFrameHdfsFilePath) {
        set(POSTER_FRAME_HDFS_FILE_PATH, posterFrameHdfsFilePath);
        return this;
    }

    public String getAudioHdfsFilePath() {
        return Value.toString(get(AUDIO_HDFS_FILE_PATH));
    }

    public ArtifactGenericMetadata setAudioHdfsFilePath(String audioFileHdfsPath) {
        set(AUDIO_HDFS_FILE_PATH, audioFileHdfsPath);
        return this;
    }

    public String getLanguage() {
        return Value.toString(get(LANGUAGE));
    }

    public ArtifactGenericMetadata setLanguage(String language) {
        set(LANGUAGE, language);
        return this;
    }

    public Long getLoadTimestamp() {
        return Value.toLong(get(LOAD_TIMESTAMP));
    }

    public ArtifactGenericMetadata setLoadTimestamp(Long loadTimestamp) {
        set(LOAD_TIMESTAMP, loadTimestamp);
        return this;
    }

    public Date getLoadTimestampDate() {
        Long l = getLoadTimestamp();
        if (l == null) {
            return null;
        }
        return new Date(l);
    }

    public String getLoadType() {
        return Value.toString(get(LOAD_TYPE));
    }

    public ArtifactGenericMetadata setLoadType(String loadType) {
        set(LOAD_TYPE, loadType);
        return this;
    }

    public String getMimeType() {
        return Value.toString(get(MIME_TYPE));
    }

    public ArtifactGenericMetadata setMimeType(String mimeType) {
        set(MIME_TYPE, mimeType);
        return this;
    }

    public JSONObject getMappingJson() {
        return Value.toJson(get(MAPPING_JSON));
    }

    public ArtifactGenericMetadata setMappingJson(JSONObject mappingJson) {
        set(MAPPING_JSON, mappingJson);
        return this;
    }

    /**
     * Source Row Id
     *
     * @return Source Row Id
     */
    public String getSource() {
        return Value.toString(get(SOURCE));
    }

    /**
     * Source Row Id
     *
     * @param source The source row id.
     * @return this
     */
    public ArtifactGenericMetadata setSource(String source) {
        set(SOURCE, source);
        return this;
    }

    public String getSourceSubtype() {
        return Value.toString(get(SOURCE_SUBTYPE));
    }

    public ArtifactGenericMetadata setSourceSubtype(String sourceSubtype) {
        set(SOURCE_SUBTYPE, sourceSubtype);
        return this;
    }

    public String getSourceType() {
        return Value.toString(get(SOURCE_TYPE));
    }

    public ArtifactGenericMetadata setSourceType(String sourceType) {
        set(SOURCE_TYPE, sourceType);
        return this;
    }

    public String getSubject() {
        return Value.toString(get(SUBJECT));
    }

    public ArtifactGenericMetadata setSubject(String subject) {
        set(SUBJECT, subject);
        return this;
    }

    public String getGraphVertexId() {
        return Value.toString(get(GRAPH_VERTEX_ID));
    }

    public ArtifactGenericMetadata setGraphVertexId(String vertexId) {
        set(GRAPH_VERTEX_ID, vertexId);
        return this;
    }
}
