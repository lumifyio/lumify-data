package com.altamiracorp.lumify.storm.twitterStream;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.structuredData.StructuredDataExtractionWorker;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRepository;
import com.altamiracorp.lumify.core.model.artifact.ArtifactType;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.altamiracorp.lumify.storm.structuredData.MappingProperties;
import com.altamiracorp.lumify.util.LineReader;
import com.sun.xml.bind.v2.TODO;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.text.ParseException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class TwitterTextWorker
        extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterTextWorker.class.getName());
    private ArtifactRepository artifactRepository;

    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData data) throws Exception {
        LOGGER.debug("Extracting Text from Twitter [TwitterTextWorker]: " + data.getFileName());
        ArtifactExtractedInfo info = new ArtifactExtractedInfo();

        LOGGER.debug("Finished [TwitterTextWorker]: " + data.getFileName());
        return info;
    }
    //take the given artifact and create the associated entities before putting it on the highlighting queue

    //create and entity for the tweet, the user who tweeted the tweet, any users mentioned in the tweet, and any hashtags in the tweet, including the associated relatiosnhips
    public TermExtractionResult extract(GraphVertex graphVertex, User user) throws IOException, ParseException {
        checkNotNull(graphVertex);
        checkNotNull(user);
        TermExtractionResult termExtractionResult = new TermExtractionResult();
        String artifactRowKey = (String) graphVertex.getProperty(PropertyName.ROW_KEY);
        LOGGER.info(String.format("Processing graph vertex [%s] for artifact: %s", graphVertex.getId(), artifactRowKey));

        Artifact artifact = artifactRepository.findByRowKey(artifactRowKey, user.getModelUserContext());
        JSONObject json = artifact.toJson();

        if (json.has("id")) {
            graphVertex.setProperty(PropertyName.TITLE, json.getString("id"));
        }
        if (json.has("coordinates")) {
            graphVertex.setProperty(PropertyName.GEO_LOCATION, json.getString("coordinates"));
        }
        if (json.has("created_at")) {
            graphVertex.setProperty(PropertyName.TIME_STAMP, json.getString("created_at"));
        }
        if (json.has("favorite_count")) {
            graphVertex.setProperty(PropertyName.ONTOLOGY_TITLE, json.getString("favorite_count"));
        }


        return termExtractionResult;
    }

    private void processLine(TermExtractionResult termExtractionResult, int offset, List<String> columns, JSONObject mappingJson) throws ParseException {

    }
    // TODO add a queue for getting the information from an unknown user from twitter

}
