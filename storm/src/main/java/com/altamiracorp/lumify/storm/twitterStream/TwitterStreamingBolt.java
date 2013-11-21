package com.altamiracorp.lumify.storm.twitterStream;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.storm.BaseArtifactProcessingBolt;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.altamiracorp.lumify.storm.FieldNames;
import com.altamiracorp.lumify.storm.StormBootstrap;
import com.altamiracorp.lumify.storm.file.FileMetadata;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.regex.Matcher;


public class TwitterStreamingBolt extends BaseArtifactProcessingBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterStreamingBolt.class);

    private OutputCollector collector;
    private Injector injector;

    private String twitterRepository;


    protected String getThreadPrefix() {
        return "twitterStreamBoltWorker";
    }

    protected ServiceLoader getServiceLoader() {
        return ServiceLoader.load(TwitterTextWorker.class);
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector){
        super.prepare(stormConf, context, collector);
        this.collector = collector;
        injector = Guice.createInjector(StormBootstrap.create(stormConf));
        injector.injectMembers(this);

        try {
            mkdir("/lumify/data/temp");
        } catch (IOException e){
            collector.reportError(e);
        }

    }
    @Override
    public void safeExecute(Tuple tuple) throws Exception {
        super.execute(tuple);

        String tweet = tuple.getStringByField("tweet");
        JSONObject json = new JSONObject(tweet);
        //if an actual tweet, process it
        if (json.has("text")) {
            //create an artifact for the tweet and write it to accumulo
            ArtifactRowKey build = ArtifactRowKey.build(json.toString().getBytes());
            String rowKey = build.toString();
            FileWriter tweetFile = new FileWriter("/lumify/artifacts/text/" + rowKey);
            try {
                tweetFile.write(json.toString());
                LOGGER.info("Creating tweet file: %s and writing to accumulo", tweetFile);
            } catch (IOException e) {
                LOGGER.info("Tweet file creation failed for file: %s", tweetFile);
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } finally {
                tweetFile.flush();
                tweetFile.close();
            }
            //create the graph vertex for the artifact
            GraphVertex graphVertex = processFile(tuple);
            graphVertex.setProperty(PropertyName.ROW_KEY, rowKey);
            graphVertex.setProperty(PropertyName.TEXT_HDFS_PATH, "/lumify/artifacts/text" + rowKey);
            //pass the artifact to the twitter bolt
            onAfterGraphVertexCreated(graphVertex);
            getCollector().ack(tuple);
        }


        collector.ack(tuple);
    }

    @Override
    public GraphVertex processFile(Tuple tuple) throws Exception {
        String tweet = tuple.getStringByField("tweet");
        JSONObject json = new JSONObject(tweet);
//        FileMetadata fileMetadata = getFileMetadata(tuple);

//        fileMetadata.setMimeType("text/plain");
        LOGGER.info("Processing tweet: %s", json.getString("id"));

        ArtifactExtractedInfo artifactExtractedInfo = new ArtifactExtractedInfo();
        artifactExtractedInfo.setMimeType("text/plain");
        GraphVertex graphVertex = saveArtifact(artifactExtractedInfo);
        if (json.has("id")) {
            artifactExtractedInfo.setTitle(json.getString("id"));
            //set FieldsNames.FILE_NAME.
            graphVertex.setProperty(PropertyName.TITLE, json.getString("id"));
        }
        if (json.has("text")) {
            artifactExtractedInfo.setText(json.getString("text"));
        }
        if (json.has("coordinates")) {
            graphVertex.setProperty(PropertyName.GEO_LOCATION, json.getString("coordinates"));
        }
        if (json.has("created_at")) {
            graphVertex.setProperty(PropertyName.PUBLISHED_DATE, json.getString("created_at"));
        } else {
            graphVertex.setProperty(PropertyName.PUBLISHED_DATE, new Date());
        }

        return graphVertex;
    }

    protected File getPrimaryFileFromArchive(File archiveTempDir) {
        throw new RuntimeException("Not implemented for class " + getClass());
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(FieldNames.FILE_NAME));
    }
}
