package com.altamiracorp.lumify.storm;

import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.textExtraction.ArtifactExtractedInfo;
import com.altamiracorp.lumify.textExtraction.TikaTextExtractor;
import com.altamiracorp.lumify.ucd.artifact.ArtifactType;
import com.google.inject.Inject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class DocumentBolt extends BaseLumifyBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentBolt.class.getName());
    private TikaTextExtractor tikaTextExtractor;

    @Override
    protected void safeExecute(Tuple input) throws Exception {
        JSONObject json = new JSONObject(input.getString(0));
        String fileName = json.getString("fileName");
        String mimeType = json.getString("mimeType");

        LOGGER.info("processing: " + fileName + " (mimeType: " + mimeType + ")");

        ArtifactExtractedInfo extractedInfo;
        InputStream in = openFile(fileName);
        try {
            extractedInfo = tikaTextExtractor.extract(fileName, in, mimeType);
        } finally {
            in.close();
        }

        GraphVertex graphVertex;
        in = openFile(fileName);
        try {
            String classUri = "http://altamiracorp.com/lumify#document";
            long rawSize = getFileSize(fileName);
            graphVertex = addArtifact(rawSize, in, extractedInfo.getText(), classUri, ArtifactType.DOCUMENT);
        } finally {
            in.close();
        }

        getCollector().ack(input);
    }

    @Inject
    public void setTikaTextExtractor(TikaTextExtractor tikaTextExtractor) {
        this.tikaTextExtractor = tikaTextExtractor;
    }
}
