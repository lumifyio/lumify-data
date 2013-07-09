package com.altamiracorp.reddawn.ucd.sentence;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Value;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SentenceMetadata extends ColumnFamily {
    public static final String NAME = "Metadata";
    public static final String AUTHOR = "author";
    public static final String CONTENT_HASH = "contentHash";
    public static final String DATE = "date";
    public static final String EXTRACTOR_ID = "extractorId";
    public static final String SECURITY_MARKING = "securityMarking";
    public static final String ARTIFACT_SUBJECT = "atc:artifactSubject";

    public SentenceMetadata() {
        super(NAME);
    }

    public String getAuthor() {
        return Value.toString(get(AUTHOR));
    }

    public SentenceMetadata setAuthor(String author) {
        set(AUTHOR, author);
        return this;
    }

    public byte[] getContentHash() {
        return Value.toBytes(get(CONTENT_HASH));
    }

    public SentenceMetadata setContentHash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] md5 = digest.digest(text.getBytes());
            set(CONTENT_HASH, md5);
            return this;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getDate() {
        return Value.toLong(get(DATE));
    }

    public SentenceMetadata setDate(Long date) {
        set(DATE, date);
        return this;
    }

    public String getExtractorId() {
        return Value.toString(get(EXTRACTOR_ID));
    }

    public SentenceMetadata setExtractorId(String extractorId) {
        set(EXTRACTOR_ID, extractorId);
        return this;
    }

    public String getSecurityMarking() {
        return Value.toString(get(SECURITY_MARKING));
    }

    public SentenceMetadata setSecurityMarking(String securityMarking) {
        set(SECURITY_MARKING, securityMarking);
        return this;
    }

    public String getArtifactSubject() {
        Value artifactSubject = get(ARTIFACT_SUBJECT);
        return artifactSubject != null ? Value.toString(artifactSubject) : "No Artifact Subject Found";
    }

    public SentenceMetadata setArtifactSubject(String artifactSubject) {
        set(ARTIFACT_SUBJECT, artifactSubject);
        return this;
    }
}
