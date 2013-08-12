package com.altamiracorp.reddawn.ucd.term;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.GeoLocation;
import com.altamiracorp.reddawn.model.RowKeyHelper;
import com.altamiracorp.reddawn.model.Value;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class TermMention extends ColumnFamily {
    public static final String ARTIFACT_KEY = "artifactKey";
    public static final String ARTIFACT_KEY_SIGN = "artifactKey_sign";
    public static final String AUTHOR = "author";
    public static final String GEO_LOCATION = "geoLocation";
    public static final String MENTION = "mention";
    public static final String PROVENANCE_ID = "provenanceID";
    public static final String SECURITY_MARKING = "securityMarking";
    public static final String DATE = "date";
    public static final String ARTIFACT_TYPE = "atc:artifactType";
    public static final String ARTIFACT_SUBJECT = "atc:artifactSubject";
    public static final String SENTENCE_TEXT = "atc:sentenceText";
    public static final String SENTENCE_OFFSET = "atc:sentenceOffset";
    public static final String GRAPH_NODE_ID = "atc:graphNodeId";
    public static final String RESOLVED_GRAPH_NODE_ID = "atc:resolvedGraphNodeId";
    public static final String RESOLVED_SIGN = "atc:resolvedSign";

    public TermMention() {
        super(null);
    }

    public TermMention(String columnFamilyName) {
        super(columnFamilyName);
    }

    @Override
    public String getColumnFamilyName() {
        if (super.getColumnFamilyName() == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(getArtifactKey());
            sb.append(getMention());
            // TODO what else should be part of the hash?
            return RowKeyHelper.buildSHA256KeyString(sb.toString().getBytes());
        }
        return super.getColumnFamilyName();
    }

    public String getArtifactKey() {
        return Value.toString(get(ARTIFACT_KEY));
    }

    public TermMention setArtifactKey(String artifactKey) {
        set(ARTIFACT_KEY, artifactKey);
        return this;
    }

    public String getArtifactSubject() {
        return Value.toString(get(ARTIFACT_SUBJECT));
    }

    public TermMention setArtifactSubject(String artifactSubject) {
        set(ARTIFACT_SUBJECT, artifactSubject);
        return this;
    }

    public String getArtifactType() {
        return Value.toString(get(ARTIFACT_TYPE));
    }

    public TermMention setArtifactType(String artifactType) {
        set(ARTIFACT_TYPE, artifactType);
        return this;
    }

    public String getSentenceText() {
        return Value.toString(get(SENTENCE_TEXT));
    }

    public TermMention setSentenceText(String sentenceText) {
        set(SENTENCE_TEXT, sentenceText);
        return this;
    }

    public Long getSentenceTokenOffset() {
        return Value.toLong(get(SENTENCE_OFFSET));
    }

    public TermMention setSentenceTokenOffset(Long sentenceOffset) {
        set(SENTENCE_OFFSET, sentenceOffset);
        return this;
    }

    public String getArtifactKeySign() {
        return Value.toString(get(ARTIFACT_KEY_SIGN));
    }

    public TermMention setArtifactKeySign(String artifactKeySign) {
        set(ARTIFACT_KEY_SIGN, artifactKeySign);
        return this;
    }

    public String getAuthor() {
        return Value.toString(get(AUTHOR));
    }

    public TermMention setAuthor(String author) {
        set(AUTHOR, author);
        return this;
    }

    public String getResolvedGraphNodeId() {
        return Value.toString(get(RESOLVED_GRAPH_NODE_ID));
    }

    public TermMention setResolvedGraphNodeId(String resolvedGraphNodeId) {
        set(RESOLVED_GRAPH_NODE_ID, resolvedGraphNodeId);
        return this;
    }

    public String getResolvedSign() {
        return Value.toString(get(RESOLVED_SIGN));
    }

    public TermMention setResolvedSign(String resolvedSign) {
        set(RESOLVED_SIGN, resolvedSign);
        return this;
    }

    public String getGeoLocation() {
        return Value.toString(get(GEO_LOCATION));
    }

    public Double getLatitude() {
        return GeoLocation.getLatitude(getGeoLocation());
    }

    public Double getLongitude() {
        return GeoLocation.getLongitude(getGeoLocation());
    }

    public TermMention setGeoLocation(String geoLocation) {
        set(GEO_LOCATION, geoLocation);
        return this;
    }

    public TermMention setGeoLocation(Double lat, Double lon) {
        return setGeoLocation(GeoLocation.getGeoLocation(lat, lon));
    }

    public String getMention() {
        return Value.toString(get(MENTION));
    }

    public TermMention setMention(String mention) {
        set(MENTION, mention);
        return this;
    }

    public TermMention setMention(JSONObject json) {
        setMention(json.toString());
        return this;
    }

    public JSONObject getMentionJSON() {
        try {
            String mention = getMention();
            if (mention == null) {
                return null;
            }
            return new JSONObject(mention);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getMentionStart() {
        JSONObject mentionJson = getMentionJSON();
        if (mentionJson == null) {
            return null;
        }
        try {
            return mentionJson.getLong("start");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public TermMention setMentionStart(Long start) {
        try {
            JSONObject mentionJson = getMentionJSON();
            if (mentionJson == null) {
                mentionJson = new JSONObject();
            }
            if (start != null) {
                mentionJson.put("start", start.longValue());
            } else {
                mentionJson.remove("start");
            }
            setMention(mentionJson);
            return this;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getMentionEnd() {
        JSONObject mentionJson = getMentionJSON();
        if (mentionJson == null) {
            return null;
        }
        try {
            return mentionJson.getLong("end");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public TermMention setMentionEnd(Long end) {
        try {
            JSONObject mentionJson = getMentionJSON();
            if (mentionJson == null) {
                mentionJson = new JSONObject();
            }
            if (end != null) {
                mentionJson.put("end", end.longValue());
            } else {
                mentionJson.remove("start");
            }
            setMention(mentionJson);
            return this;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getProvenanceId() {
        return Value.toString(get(PROVENANCE_ID));
    }

    public TermMention setProvenanceId(String provenanceId) {
        set(PROVENANCE_ID, provenanceId);
        return this;
    }

    public String getSecurityMarking() {
        return Value.toString(get(SECURITY_MARKING));
    }

    public TermMention setSecurityMarking(String securityMarking) {
        set(SECURITY_MARKING, securityMarking);
        return this;
    }

    public Long getDate() {
        return Value.toLong(get(DATE));
    }

    public TermMention setDate(Long date) {
        set(DATE, date);
        return this;
    }

    public TermMention setDate(Date date) {
        return setDate(date.getTime());
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject json = super.toJson();

            Double latitude = getLatitude();
            if (latitude != null) {
                json.put("latitude", latitude);
            }

            Double longitude = getLongitude();
            if (longitude != null) {
                json.put("longitude", longitude);
            }

            return json;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getGraphNodeId() {
        return Value.toString(get(GRAPH_NODE_ID));
    }

    public TermMention setGraphNodeId(String graphNodeId) {
        set(GRAPH_NODE_ID, graphNodeId);
        return this;
    }
}
