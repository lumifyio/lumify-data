package com.altamiracorp.reddawn.ucd.term;

import com.altamiracorp.reddawn.model.RowKey;
import com.altamiracorp.reddawn.model.RowKeyHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class TermRowKey extends RowKey {
    public TermRowKey(String rowKey) {
        super(rowKey);
    }

    public TermRowKey(String sign, String modelKey, String conceptLabel) {
        super(RowKeyHelper.buildMinor(sign.toLowerCase(), modelKey, conceptLabel));
    }

    public String getSign() {
        return RowKeyHelper.splitOnMinorFieldSeperator(this.toString())[0];
    }

    public String getModelKey() {
        return RowKeyHelper.splitOnMinorFieldSeperator(this.toString())[1];
    }

    public String getConceptLabel() {
        return RowKeyHelper.splitOnMinorFieldSeperator(this.toString())[2];
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject json = super.toJson();
            json.put("sign", getSign());
            json.put("modelKey", getModelKey());
            json.put("conceptLabel", getConceptLabel());
            return json;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
