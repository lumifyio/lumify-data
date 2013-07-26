package com.altamiracorp.reddawn.ucd.artifact;

import com.altamiracorp.reddawn.model.Column;
import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.RowKeyHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ArtifactDetectedObjects extends ColumnFamily {

    public static final String NAME = "atc:Artifact_Detected_Objects";


    public ArtifactDetectedObjects() {
        super(NAME);
    }

    public void addDetectedObject(String concept, String model, int x1, int y1, int x2, int y2) {
        String columnName = RowKeyHelper.buildMinor(concept, model, Integer.toString(x1), Integer.toString(y1), Integer.toString(x2), Integer.toString(y2));
        this.set(columnName, "");
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject result = new JSONObject();
            JSONArray detectedObjects = new JSONArray();

            for (Column column : getColumns()) {
                JSONObject columnJson = new JSONObject();
                String[] parts = RowKeyHelper.splitOnMinorFieldSeperator(column.getName());
                columnJson.put("concept", parts[0]);
                columnJson.put("model", parts[1]);
                JSONObject coordsJson = new JSONObject();
                coordsJson.put("x1", parts[2]);
                coordsJson.put("y1", parts[3]);
                coordsJson.put("x2", parts[4]);
                coordsJson.put("y2", parts[5]);
                columnJson.put("coords", coordsJson);
                detectedObjects.put(columnJson);
            }

            result.put("detectedObjects", detectedObjects);
            return result;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
