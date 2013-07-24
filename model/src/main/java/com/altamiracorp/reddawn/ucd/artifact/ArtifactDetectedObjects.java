package com.altamiracorp.reddawn.ucd.artifact;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.RowKeyHelper;

public class ArtifactDetectedObjects extends ColumnFamily {

    public static final String NAME = "atc:Artifact_Detected_Objects";


    public ArtifactDetectedObjects() {
        super(NAME);
    }

    public void addDetectedObject(String concept, String model, String[] coords) {
        String coordKey = RowKeyHelper.buildMinor(coords);
        String columnName = RowKeyHelper.buildMinor(concept,model,coordKey);
        this.set(columnName,"");
    }

}
