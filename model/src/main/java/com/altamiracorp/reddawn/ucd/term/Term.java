package com.altamiracorp.reddawn.ucd.term;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Term extends Row<TermRowKey> {
    public static final String TABLE_NAME = "Term";

    public Term(RowKey rowKey) {
        super(TABLE_NAME, new TermRowKey(rowKey.toString()));
    }

    public Term(String sign, String modelKey, String conceptLabel) {
        super(TABLE_NAME, new TermRowKey(sign, modelKey, conceptLabel));
    }

    public List<TermMention> getTermMentions() {
        ArrayList<TermMention> termMentions = new ArrayList<TermMention>();
        for (ColumnFamily columnFamily : getColumnFamilies()) {
            if (columnFamily instanceof TermMention) {
                termMentions.add((TermMention) columnFamily);
            }
        }
        return termMentions;
    }

    public Term addTermMention(TermMention termMention) {
        this.addColumnFamily(termMention);
        return this;
    }

    public JSONObject toJson(ArtifactRowKey artifactKey) {
        try {
            JSONObject termJson = new JSONObject();
            termJson.put("key", new JSONObject(getRowKey().toJson()));

            JSONArray metadataJson = new JSONArray();
            for (TermMention termMention : getTermMentions()) {
                if (artifactKey.equals(termMention.getArtifactKey())) {
                    metadataJson.put(termMention.toJson());
                }
            }
            termJson.put("metadata", metadataJson);
            return termJson;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
