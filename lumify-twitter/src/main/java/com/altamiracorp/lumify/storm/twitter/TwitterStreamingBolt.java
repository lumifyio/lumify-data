package com.altamiracorp.lumify.storm.twitter;

import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.core.model.artifact.ArtifactType;
import com.altamiracorp.lumify.core.model.graph.GraphGeoLocation;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.search.SearchProvider;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TwitterStreamingBolt extends BaseLumifyBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterStreamingBolt.class);
    private SearchProvider searchProvider;

    @Override
    public void safeExecute(Tuple tuple) throws Exception {
        JSONObject json = getJsonFromTuple(tuple);

        //if an actual tweet, process it
        if (json.has("text")) {
            ArtifactRowKey build = ArtifactRowKey.build(json.toString().getBytes());
            String rowKey = build.toString();

            String text = json.getString("text");
            ArtifactExtractedInfo artifactExtractedInfo = new ArtifactExtractedInfo();
            artifactExtractedInfo.setText(text);
            artifactExtractedInfo.setRaw(json.toString().getBytes());

            artifactExtractedInfo.setMimeType("text/plain");
            artifactExtractedInfo.setRowKey(rowKey);
            artifactExtractedInfo.setArtifactType(ArtifactType.DOCUMENT.toString());
            String user = json.getJSONObject("user").getString("screen_name");
            String title = "tweet from : " + user;
            artifactExtractedInfo.setTitle(title);

            // Write to accumulo and create graph vertex for artifact
            GraphVertex graphVertex = saveArtifact(artifactExtractedInfo);
            LOGGER.info("Saving tweet to accumulo and as graph vertex: " + graphVertex.getId());

            graphVertex.setProperty(PropertyName.TITLE, title);
            if (json.has("created_at")) {
                final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
                SimpleDateFormat sf = new SimpleDateFormat(TWITTER);
                sf.setLenient(true);

                Date date = sf.parse(json.getString("created_at"));
                artifactExtractedInfo.setDate(date);
                graphVertex.setProperty(PropertyName.PUBLISHED_DATE, date.getTime());
            } else {
                graphVertex.setProperty(PropertyName.PUBLISHED_DATE, new Date().getTime());
            }
            if (json.has("coordinates") && !json.get("coordinates").equals(JSONObject.NULL)) {
                JSONArray coordinates = json.getJSONObject("coordinates").getJSONArray("coordinates");
                graphVertex.setProperty(PropertyName.GEO_LOCATION, new GraphGeoLocation(coordinates.getDouble(1), coordinates.getDouble(0)));
            }
            graphVertex.setProperty(PropertyName.ROW_KEY, rowKey);
            graphVertex.setProperty(PropertyName.SOURCE, json.getString("source"));
            graphRepository.commit();

            // Indexing
            InputStream in = new ByteArrayInputStream(text.getBytes());
            searchProvider.add(graphVertex, in);
        }

        getCollector().ack(tuple);
    }

    @Inject
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

}
