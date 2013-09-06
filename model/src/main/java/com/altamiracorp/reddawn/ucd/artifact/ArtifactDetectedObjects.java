package com.altamiracorp.reddawn.ucd.artifact;

import com.altamiracorp.reddawn.model.Column;
import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.RowKeyHelper;
import com.altamiracorp.reddawn.model.Value;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ArtifactDetectedObjects extends ColumnFamily {

    public static final String NAME = "atc:Artifact_Detected_Objects";

    public ArtifactDetectedObjects() {
        super(NAME);
    }

    public String addDetectedObject(String concept, String model, String x1, String y1, String x2, String y2) {
        String columnName = RowKeyHelper.buildMinor(concept, model, x1, y1, x2, y2);
        JSONObject data = getInfoJson(concept, model, x1, y1, x2, y2, columnName);
        this.set(columnName, data);
        return columnName;
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject result = new JSONObject();
            JSONArray detectedObjects = new JSONArray();

            for (Column column : getColumns()) {
                column.getValue();
                detectedObjects.put(column.getValue().toJson(column.getValue()));
            }

            result.put("detectedObjects", detectedObjects);
            return result;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject getInfoJson(String concept, String model, String x1, String y1, String x2, String y2, String rowKey) {
        try {
            JSONObject obj = new JSONObject();
            JSONObject infoJson = new JSONObject();
            infoJson.put("concept", concept);
            infoJson.put("model", model);
            infoJson.put("_rowKey", rowKey);
            JSONObject coordsJson = new JSONObject();
            coordsJson.put("x1", x1);
            coordsJson.put("y1", y1);
            coordsJson.put("x2", x2);
            coordsJson.put("y2", y2);
            infoJson.put("coords", coordsJson);
            obj.put("info", infoJson);
            return obj;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
