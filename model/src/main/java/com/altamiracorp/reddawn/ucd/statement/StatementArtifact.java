package com.altamiracorp.reddawn.ucd.statement;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.RowKeyHelper;
import com.altamiracorp.reddawn.model.Value;

public class StatementArtifact extends ColumnFamily {
    public static final String ARTIFACT_KEY = "artifactKey";
    public static final String AUTHOR = "author";
    public static final String DATE = "date";
    public static final String EXTRACTOR_ID = "extractorId";
    public static final String SECURITY_MARKING = "securityMarking";
    public static final String SENTENCE = "sentence";
    public static final String SENTENCE_TEXT = "atc:sentence_text";
    public static final String ARTIFACT_SUBJECT = "atc:artifact_subject";

    public StatementArtifact(String columnFamilyName) {
        super(columnFamilyName);
    }

    public StatementArtifact() {
        super(null);
    }

    @Override
    public String getColumnFamilyName() {
        if (super.getColumnFamilyName() == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(getArtifactKey());
            sb.append(getAuthor());
            sb.append(getDate());
            sb.append(getExtractorId());
            sb.append(getSecurityMarking());
            sb.append(getSentence());
            return RowKeyHelper.buildSHA256KeyString(sb.toString().getBytes());
        }
        return super.getColumnFamilyName();
    }

    public String getArtifactKey() {
        return Value.toString(get(ARTIFACT_KEY));
    }

    public StatementArtifact setArtifactKey(String artifactKey) {
        set(ARTIFACT_KEY, artifactKey);
        return this;
    }

    public String getAuthor() {
        return Value.toString(get(AUTHOR));
    }

    public StatementArtifact setAuthor(String author) {
        set(AUTHOR, author);
        return this;
    }

    public Long getDate() {
        return Value.toLong(get(DATE));
    }

    public StatementArtifact setDate(Long date) {
        set(DATE, date);
        return this;
    }

    public String getExtractorId() {
        return Value.toString(get(EXTRACTOR_ID));
    }

    public StatementArtifact setExtractorId(String extractorId) {
        set(EXTRACTOR_ID, extractorId);
        return this;
    }

    public String getSecurityMarking() {
        return Value.toString(get(SECURITY_MARKING));
    }

    public StatementArtifact setSecurityMarking(String securityMarking) {
        set(SECURITY_MARKING, securityMarking);
        return this;
    }

    public String getSentence() {
        return Value.toString(get(SENTENCE));
    }

    public StatementArtifact setSentence(String sentence) {
        set(SENTENCE, sentence);
        return this;
    }

    public String getSentenceText() {
        return Value.toString(get(SENTENCE_TEXT));
    }

    public StatementArtifact setSentenceText(String sentenceText) {
        set(SENTENCE_TEXT, sentenceText);
        return this;
    }

    public String getArtifactSubject() {
        return Value.toString(get(ARTIFACT_SUBJECT));
    }

    public StatementArtifact setArtifactSubject(String artifactSubject) {
        set(ARTIFACT_SUBJECT, artifactSubject);
        return this;
    }
}
