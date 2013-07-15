package com.altamiracorp.reddawn.ucd.statement;

import com.altamiracorp.reddawn.model.RowKey;
import com.altamiracorp.reddawn.model.RowKeyHelper;
import com.altamiracorp.reddawn.ucd.predicate.PredicateRowKey;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import org.json.JSONException;
import org.json.JSONObject;

public class StatementRowKey extends RowKey {
    public StatementRowKey(String rowKey) {
        super(rowKey);
    }

    public StatementRowKey(TermRowKey subjectRowKey, PredicateRowKey predicateRowKey, TermRowKey objectRowKey) {
        super(RowKeyHelper.buildMajor(subjectRowKey.toString(), predicateRowKey.toString(), objectRowKey.toString()).toString());
    }

    public StatementRowKey(String subjectRowKey, String predicateLabel, String objectRowKey) {
        super(RowKeyHelper.buildMajor(subjectRowKey, predicateLabel, objectRowKey).toString());
    }

    public String getSubjectRowKey() {
        return RowKeyHelper.splitOnMajorFieldSeperator(this.toString())[0];
    }

    public String getPredicateLabel() {
        return RowKeyHelper.splitOnMajorFieldSeperator(this.toString())[1];
    }

    public String getObjectRowKey() {
        return RowKeyHelper.splitOnMajorFieldSeperator(this.toString())[2];
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject json = super.toJson();
            json.put("subjectRowKey", new TermRowKey(getSubjectRowKey()).toJson());
            json.put("predicateLabel", new PredicateRowKey(getPredicateLabel()).toJson());
            json.put("objectRowKey", new TermRowKey(getObjectRowKey()).toJson());
            return json;
        } catch (JSONException e) {
            throw new RuntimeException("cannot get rowkey JSON", e);
        }
    }
}
