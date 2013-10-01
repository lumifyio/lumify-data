package com.altamiracorp.lumify.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.user.SystemUser;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.ucd.artifact.ArtifactType;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import kafka.javaapi.producer.Producer;
import kafka.javaapi.producer.ProducerData;
import kafka.producer.ProducerConfig;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public abstract class BaseLumifyBolt extends BaseRichBolt {
    private OutputCollector collector;
    private Producer<String, JSONObject> kafkaProducer;
    private ArtifactRepository artifactRepository;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        final Injector injector = Guice.createInjector(StormBootstrap.create(stormConf));
        injector.injectMembers(this);

        Properties props = new Properties();
        props.put("zk.connect", stormConf.get("zookeeperServerNames"));
        props.put("serializer.class", KafkaJsonEncoder.class.getName());
        ProducerConfig config = new ProducerConfig(props);
        kafkaProducer = new Producer<String, JSONObject>(config);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }

    @Override
    public final void execute(Tuple input) {
        try {
            safeExecute(input);
        } catch (Exception e) {
            getCollector().reportError(e);
            getCollector().fail(input);
        }
    }

    protected abstract void safeExecute(Tuple input) throws Exception;

    protected void pushOnQueue(String queueName, JSONObject json) {
        ProducerData<String, JSONObject> data = new ProducerData<String, JSONObject>(queueName, json);
        kafkaProducer.send(data);
    }

    protected InputStream openFile(String fileName) throws Exception {
        return new FileInputStream(fileName); // TODO change to use hdfs or file system depending on format
    }

    protected long getFileSize(String fileName) {
        return new File(fileName).length(); // TODO change to use hdfs or file system depending on format
    }

    public OutputCollector getCollector() {
        return collector;
    }

    protected GraphVertex addArtifact(long rawSize, InputStream rawInputStream, String text, String classUri, ArtifactType artifactType) throws IOException {
        Artifact artifact = artifactRepository.createArtifactFromInputStream(rawSize, rawInputStream, getUser());
        artifact.getContent().setDocExtractedText(text.getBytes());
        GraphVertex artifactVertex = artifactRepository.saveToGraph(artifact, getUser());
        artifactVertex.setProperty(PropertyName.TYPE, artifactType.toString());
        artifactVertex.setProperty(PropertyName.SUBTYPE, classUri);
        return artifactVertex;
    }

    private User getUser() {
        return new SystemUser();
    }

    @Inject
    public void setArtifactRepository(ArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }
}
