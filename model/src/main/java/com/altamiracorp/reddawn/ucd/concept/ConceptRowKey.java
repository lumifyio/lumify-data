package com.altamiracorp.reddawn.ucd.concept;

import com.altamiracorp.reddawn.model.RowKey;
import com.altamiracorp.reddawn.model.RowKeyHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class ConceptRowKey extends RowKey {
    public ConceptRowKey(String rowKey) {
        super(rowKey);
    }

    public ConceptRowKey(String modelKey, String conceptLabel) {
        super(RowKeyHelper.buildMinor(modelKey, conceptLabel));
    }

    public String getModelKey() {
        return RowKeyHelper.splitOnMinorFieldSeperator(this.toString())[0];
    }

    public String getConceptLabel() {
        return RowKeyHelper.splitOnMinorFieldSeperator(this.toString())[1];
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject json = super.toJson();
            json.put("modelKey", getModelKey());
            json.put("conceptLabel", getConceptLabel());
            return json;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
