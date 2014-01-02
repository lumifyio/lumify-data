package com.altamiracorp.lumify.core.model.audit;

import com.altamiracorp.bigtable.model.ColumnFamily;
import com.altamiracorp.bigtable.model.Value;
import org.json.JSONException;
import org.json.JSONObject;

public class AuditRelationship extends ColumnFamily{
    public static final String NAME = "relationship";
    public static final String SOURCE_TYPE = "sourceType";
    public static final String SOURCE_TITLE = "sourceTitle";
    public static final String SOURCE_SUBTYPE = "sourceSubtype";
    public static final String SOURCE_ID = "sourceId";
    public static final String DEST_TYPE = "destType";
    public static final String DEST_TITLE = "destTitle";
    public static final String DEST_ID = "destId";
    public static final String DEST_SUBTYPE = "destSubtype";
    public static final String LABEL = "label";

    public AuditRelationship () {
        super (NAME);
    }

    public String getSourceType() {
        return Value.toString(get(SOURCE_TYPE));
    }

    public AuditRelationship setSourceType (Object sourceType) {
        set (SOURCE_TYPE, sourceType);
        return this;
    }

    public String getSourceTitle() {
        return Value.toString(get(SOURCE_TITLE));
    }

    public AuditRelationship setSourceTitle (Object sourceTitle) {
        set (SOURCE_TITLE, sourceTitle);
        return this;
    }

    public String getSourceId() {
        return Value.toString(get(SOURCE_ID));
    }

    public AuditRelationship setSourceId (String sourceId) {
        set (SOURCE_ID, sourceId);
        return this;
    }

    public String getDestType() {
        return Value.toString(get(DEST_TYPE));
    }

    public AuditRelationship setDestType (Object destType) {
        set (DEST_TYPE, destType);
        return this;
    }

    public String getDestTitle() {
        return Value.toString(get(DEST_TITLE));
    }

    public AuditRelationship setDestTitle (Object destTitle) {
        set (DEST_TITLE, destTitle);
        return this;
    }

    public String getDestId() {
        return Value.toString(get(DEST_ID));
    }

    public AuditRelationship setDestId (String destId) {
        set (DEST_ID, destId);
        return this;
    }

    public String getLabel() {
        return Value.toString(get(LABEL));
    }

    public AuditRelationship setLabel (String label) {
        set (LABEL, label);
        return this;
    }

    public String getDestSubtype() {
        return Value.toString(get(DEST_SUBTYPE));
    }

    public AuditRelationship setDestSubtype (Object destSubtype) {
        set (DEST_SUBTYPE, destSubtype);
        return this;
    }

    public String getSourceSubtype() {
        return Value.toString(get(SOURCE_SUBTYPE));
    }

    public AuditRelationship setSourceSubtype (Object source) {
        set (SOURCE_SUBTYPE, source);
        return this;
    }

    @Override
    public JSONObject toJson () {
        try {
            JSONObject json = new JSONObject();
            json.put(SOURCE_ID, this.getSourceId());
            json.put(SOURCE_TITLE, this.getSourceTitle());
            json.put(SOURCE_TYPE, this.getSourceType());
            json.put(SOURCE_SUBTYPE, this.getSourceSubtype());
            json.put(DEST_ID, this.getDestId());
            json.put(DEST_TITLE, this.getDestTitle());
            json.put(DEST_TYPE, this.getDestType());
            json.put(DEST_SUBTYPE, this.getDestSubtype());
            json.put(LABEL, this.getLabel());
            return json;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
