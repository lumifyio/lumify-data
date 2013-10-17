package com.altamiracorp.lumify.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.config.ConfigurationHelper;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.ontology.VertexType;
import com.altamiracorp.lumify.core.user.SystemUser;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.AccumuloSession;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.ucd.artifact.ArtifactType;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.Map;

public abstract class BaseLumifyBolt extends BaseRichBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseLumifyBolt.class.getName());

    private OutputCollector collector;
    protected ArtifactRepository artifactRepository;
    private FileSystem hdfsFileSystem;
    protected GraphRepository graphRepository;
    private Injector injector;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        injector = Guice.createInjector(StormBootstrap.create(stormConf));
        injector.injectMembers(this);

        Configuration conf = ConfigurationHelper.createHadoopConfigurationFromMap(stormConf);
        try {
            String hdfsRootDir = (String) stormConf.get(AccumuloSession.HADOOP_URL);
            hdfsFileSystem = FileSystem.get(new URI(hdfsRootDir), conf, "hadoop");
        } catch (Exception e) {
            collector.reportError(e);
        }
    }

    protected JSONObject getJsonFromTuple(Tuple input) throws Exception {
        String str = input.getString(0);
        try {
            return new JSONObject(str);
        } catch (Exception ex) {
            throw new RuntimeException("Invalid input format. Expected JSON got.\n" + str, ex);
        }
    }

    public Injector getInjector() {
        return injector;
    }

    protected <T> T inject(T obj) {
        getInjector().injectMembers(obj);
        return obj;
    }

    protected FileSystem getHdfsFileSystem() {
        return hdfsFileSystem;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("json"));
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
        LOGGER.info("moving file " + sourceFileName + " -> " + destFileName);
        Path sourcePath = new Path(sourceFileName);
        Path destPath = new Path(destFileName);
        getHdfsFileSystem().rename(sourcePath, destPath);
    }

    public OutputCollector getCollector() {
        return collector;
    }

    protected GraphVertex saveArtifact(ArtifactExtractedInfo artifactExtractedInfo) throws IOException {
        Artifact artifact = saveArtifactModel(artifactExtractedInfo);
        GraphVertex artifactVertex = saveArtifactGraphVertex(artifactExtractedInfo, artifact);
        return artifactVertex;
    }

    private GraphVertex saveArtifactGraphVertex(ArtifactExtractedInfo artifactExtractedInfo, Artifact artifact) {
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
        artifactVertex.setProperty(PropertyName.SUBTYPE, ArtifactType.DOCUMENT.toString());
        artifactVertex.setProperty(PropertyName.TITLE, artifactExtractedInfo.getTitle());
        if (artifactExtractedInfo.getRawHdfsPath() != null) {
            artifactVertex.setProperty(PropertyName.RAW_HDFS_PATH, artifactExtractedInfo.getRawHdfsPath());
        }
        if (artifactExtractedInfo.getTextHdfsPath() != null) {
            artifactVertex.setProperty(PropertyName.TEXT_HDFS_PATH, artifactExtractedInfo.getTextHdfsPath());
            artifactVertex.setProperty(PropertyName.HIGHLIGHTED_TEXT_HDFS_PATH, artifactExtractedInfo.getTextHdfsPath());
        }
        if (artifactExtractedInfo.getDetectedObjects() != null) {
            artifactVertex.setProperty(PropertyName.DETECTED_OBJECTS, artifactExtractedInfo.getDetectedObjects());
        }
        String vertexId = this.graphRepository.save(artifactVertex, getUser());
        this.graphRepository.commit();

        if (!vertexId.equals(oldGraphVertexId)) {
            artifact.getMetadata().setGraphVertexId(vertexId);
            artifactRepository.save(artifact, getUser());
        }
        return artifactVertex;
    }

    private Artifact saveArtifactModel(ArtifactExtractedInfo artifactExtractedInfo) {
        Artifact artifact = artifactRepository.findByRowKey(artifactExtractedInfo.getRowKey(), getUser());
        if (artifact == null) {
            artifact = new Artifact(artifactExtractedInfo.getRowKey());
            artifact.getMetadata().setCreateDate(new Date());
        }
        if (artifactExtractedInfo.getRaw() != null) {
            artifact.getMetadata().setRaw(artifactExtractedInfo.getRaw());
        }
        if (artifactExtractedInfo.getVideoTranscript() != null) {
            artifact.getMetadata().setVideoTranscript(artifactExtractedInfo.getVideoTranscript());
            // TODO should we combine text like this? If the text ends up on HDFS the text here is technically invalid
            if (artifactExtractedInfo.getText() == null) {
                artifactExtractedInfo.setText(artifactExtractedInfo.getVideoTranscript().toString());
            } else {
                artifactExtractedInfo.setText(artifactExtractedInfo.getText() + "\n\n" + artifactExtractedInfo.getVideoTranscript().toString());
            }
        }
        if (artifactExtractedInfo.getText() != null) {
            artifact.getMetadata().setText(artifactExtractedInfo.getText());
            if (artifact.getMetadata().getHighlightedText() == null) {
                artifact.getMetadata().setHighlightedText(artifactExtractedInfo.getText());
            }
        }
        if (artifactExtractedInfo.getMappingJson() != null){
            artifact.getMetadata().setMappingJson(artifactExtractedInfo.getMappingJson());
        }

        artifactRepository.save(artifact, getUser());
        return artifact;
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
