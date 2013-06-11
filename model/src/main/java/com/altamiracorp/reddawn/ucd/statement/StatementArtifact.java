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

    public StatementArtifact(String columnFamilyName) {
        super(columnFamilyName);
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

    public String getAuthor() {
        return Value.toString(get(AUTHOR));
    }

    public Long getDate() {
        return Value.toLong(get(DATE));
    }

    public String getExtractorId() {
        return Value.toString(get(EXTRACTOR_ID));
    }

    public String getSecurityMarking() {
        return Value.toString(get(SECURITY_MARKING));
    }

    public String getSentence() {
        return Value.toString(get(SENTENCE));
    }
    
    
//ArtifactKey
//Author
//Date
//ExtractorId
//SecurityMarking
//Sentence

}
