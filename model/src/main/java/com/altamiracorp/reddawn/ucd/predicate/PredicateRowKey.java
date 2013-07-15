package com.altamiracorp.reddawn.ucd.predicate;

import com.altamiracorp.reddawn.model.RowKey;
import com.altamiracorp.reddawn.model.RowKeyHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class PredicateRowKey extends RowKey {
    public PredicateRowKey(String rowKey) {
        super(rowKey);
    }

    public PredicateRowKey(String modelKey, String predicateLabel) {
        super(RowKeyHelper.buildMinor(modelKey, predicateLabel).toString());
    }

    public String getModelKey() {
        return RowKeyHelper.splitOnMinorFieldSeperator(this.toString())[0];
    }

    public String getPredicateLabel() {
        return RowKeyHelper.splitOnMinorFieldSeperator(this.toString())[1];
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject json = super.toJson();
            json.put("modelKey", getModelKey());
            json.put("predicateLabel", getPredicateLabel());
            return json;
        } catch (JSONException e) {
            throw new RuntimeException("Could not parse rowkey", e);
        }
    }
}
