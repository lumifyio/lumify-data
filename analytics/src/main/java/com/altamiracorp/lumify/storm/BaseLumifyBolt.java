package com.altamiracorp.lumify.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.user.SystemUser;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.AccumuloSession;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.ontology.VertexType;
import com.altamiracorp.lumify.textExtraction.ArtifactExtractedInfo;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import kafka.javaapi.producer.Producer;
import kafka.javaapi.producer.ProducerData;
import kafka.producer.ProducerConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

public abstract class BaseLumifyBolt extends BaseRichBolt {
    private OutputCollector collector;
    private Producer<String, JSONObject> kafkaProducer;
    protected ArtifactRepository artifactRepository;
    private FileSystem hdfsFileSystem;
    protected GraphRepository graphRepository;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        final Injector injector = Guice.createInjector(StormBootstrap.create(stormConf));
        injector.injectMembers(this);

        Configuration conf = createHadoopConfiguration(stormConf);
        try {
            String hdfsRootDir = (String) stormConf.get(AccumuloSession.HADOOP_URL);
            hdfsFileSystem = FileSystem.get(new URI(hdfsRootDir), conf, "hadoop");
        } catch (Exception e) {
            collector.reportError(e);
        }

        Properties props = new Properties();
        props.put("zk.connect", stormConf.get("zookeeperServerNames") + "/kafka"); // TODO what happens if zookeeperServerNames has multiple names
        props.put("serializer.class", KafkaJsonEncoder.class.getName());
        ProducerConfig config = new ProducerConfig(props);
        kafkaProducer = new Producer<String, JSONObject>(config);
    }

    protected Configuration createHadoopConfiguration(Map stormConf) {
        Configuration configuration = new Configuration();
        for (Object entrySetObject : stormConf.entrySet()) {
            Map.Entry entrySet = (Map.Entry) entrySetObject;
            configuration.set("" + entrySet.getKey(), "" + entrySet.getValue());
        }
        return configuration;
    }

    protected FileSystem getHdfsFileSystem() {
        return hdfsFileSystem;
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
        // TODO probably a better way to handle this
        try {
            return new FileInputStream(fileName);
        } catch (Exception ex) {
            return getHdfsFileSystem().open(new Path(fileName));
        }
    }

    protected long getFileSize(String fileName) throws IOException {
        // TODO probably a better way to handle this
        try {
            return new File(fileName).length();
        } catch (Exception ex) {
            return getHdfsFileSystem().getStatus(new Path(fileName)).getUsed();
        }
    }

    protected void mkdir(String pathString) throws IOException {
        Path path = new Path(pathString);
        if (!getHdfsFileSystem().exists(path)) {
            getHdfsFileSystem().mkdirs(path);
        }
    }

    protected void moveFile(String sourceFileName, String destFileName) throws IOException {
        Path sourcePath = new Path(sourceFileName);
        Path destPath = new Path(destFileName);
        getHdfsFileSystem().copyFromLocalFile(false, sourcePath, destPath);
    }

    public OutputCollector getCollector() {
        return collector;
    }

    protected GraphVertex addArtifact(ArtifactExtractedInfo artifactExtractedInfo) throws IOException {
        Artifact artifact = artifactRepository.findByRowKey(artifactExtractedInfo.getRowKey(), getUser());
        if (artifact == null) {
            artifact = new Artifact(artifactExtractedInfo.getRowKey());
            artifact.getMetadata().setCreateDate(new Date());
        }
        if (artifactExtractedInfo.getRaw() != null) {
            artifact.getMetadata().setRaw(artifactExtractedInfo.getRaw());
        }
        if (artifactExtractedInfo.getText() != null) {
            artifact.getMetadata().setText(artifactExtractedInfo.getText());
        }

        artifactRepository.save(artifact, getUser());

        GraphVertex artifactVertex = null;
        String oldGraphVertexId = artifact.getMetadata().getGraphVertexId();
        if (oldGraphVertexId != null) {
            artifactVertex = this.graphRepository.findVertex(oldGraphVertexId, getUser());
        }
        if (artifactVertex == null) {
            artifactVertex = new InMemoryGraphVertex();
        }

        artifactVertex.setProperty(PropertyName.ROW_KEY.toString(), artifact.getRowKey().toString());
        artifactVertex.setProperty(PropertyName.TYPE, VertexType.ARTIFACT.toString());
        artifactVertex.setProperty(PropertyName.SUBTYPE, artifactExtractedInfo.getOntologyClassUri());
        artifactVertex.setProperty(PropertyName.TITLE, artifactExtractedInfo.getTitle());
        if (artifactExtractedInfo.getRawHdfsPath() != null) {
            artifactVertex.setProperty(PropertyName.RAW_HDFS_PATH, artifactExtractedInfo.getRawHdfsPath());
        }
        if (artifactExtractedInfo.getTextHdfsPath() != null) {
            artifactVertex.setProperty(PropertyName.TEXT_HDFS_PATH, artifactExtractedInfo.getTextHdfsPath());
        }
        String vertexId = this.graphRepository.save(artifactVertex, getUser());
        this.graphRepository.commit();

        if (!vertexId.equals(oldGraphVertexId)) {
            artifact.getMetadata().setGraphVertexId(vertexId);
            artifactRepository.save(artifact, getUser());
        }

        return artifactVertex;
    }

    protected User getUser() {
        return new SystemUser();
    }

    @Inject
    public void setArtifactRepository(ArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }

    @Inject
    public void setGraphRepository(GraphRepository graphRepository) {
        this.graphRepository = graphRepository;
    }
}
