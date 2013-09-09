package com.altamiracorp.lumify.ucd.artifact;

import com.altamiracorp.lumify.model.ColumnFamily;

public class ArtifactExtractedText extends ColumnFamily {

    public static final String NAME = "atc:Extracted_Text";

    public ArtifactExtractedText() {
        super(NAME);
    }

    public void addExtractedText (String extractor, String extractedText) {
        this.set(extractor,extractedText);
    }

}
