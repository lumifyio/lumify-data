package com.altamiracorp.lumify.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.config.ConfigurationHelper;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRepository;
import com.altamiracorp.lumify.core.model.audit.AuditRepository;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.user.SystemUser;
import com.altamiracorp.lumify.core.user.User;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BaseLumifyBolt extends BaseRichBolt implements BaseLumifyBoltMXBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseLumifyBolt.class);

    private OutputCollector collector;
    protected ArtifactRepository artifactRepository;
    private FileSystem hdfsFileSystem;
    protected GraphRepository graphRepository;
    protected AuditRepository auditRepository;
    private Injector injector;
    private AtomicLong totalProcessedCount = new AtomicLong();
    private AtomicLong processingCount = new AtomicLong();
    private AtomicLong totalErrorCount = new AtomicLong();
    private long averageProcessingTime;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        LOGGER.info(String.format("Configuring environment for bolt: %s-%d", context.getThisComponentId(), context.getThisTaskId()));
        this.collector = collector;
        injector = Guice.createInjector(StormBootstrap.create(stormConf));
        injector.injectMembers(this);

        Configuration conf = ConfigurationHelper.createHadoopConfigurationFromMap(stormConf);
        try {
            String hdfsRootDir = (String) stormConf.get(com.altamiracorp.lumify.core.config.Configuration.HADOOP_URL);
            hdfsFileSystem = FileSystem.get(new URI(hdfsRootDir), conf, "hadoop");

            registerJmxBean();
        } catch (Exception e) {
            collector.reportError(e);
        }
    }

    protected void registerJmxBean() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName beanName = new ObjectName("com.altamiracorp.lumify.storm.bolt:type=" + getClass().getName());
        beanServer.registerMBean(this, beanName);
    }

    protected JSONObject getJsonFromTuple(Tuple input) throws Exception {
        String str = input.getString(0);
        try {
            return new JSONObject(str);
        } catch (Exception ex) {
            throw new RuntimeException("Invalid input format. Expected JSON got.\n" + str, ex);
        }
    }

    protected JSONObject tryGetJsonFromTuple(Tuple input) {
        try {
            return getJsonFromTuple(input);
        } catch (Exception ex) {
            return null;
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
        long startTime = System.currentTimeMillis();
        processingCount.getAndIncrement();
        try {
            String auditMessage;
            String graphVertexId = null;
            auditMessage = "BEGIN " + this.getClass().getName() + ": " + input;
            LOGGER.info(auditMessage);
            JSONObject json = tryGetJsonFromTuple(input);
            if (json != null) {
                graphVertexId = json.optString("graphVertexId");
                if (graphVertexId.length() == 0) {
                    graphVertexId = null;
                }
                if (graphVertexId != null) {
                    auditRepository.audit(graphVertexId, auditMessage, getUser());
                }
            }
            try {
                safeExecute(input);
            } catch (Exception e) {
                totalErrorCount.getAndIncrement();
                LOGGER.error("Error occurred during execution: " + input, e);
                getCollector().reportError(e);
                getCollector().fail(input);
            }

            auditMessage = "END " + this.getClass().getName() + ": " + input;
            LOGGER.info(auditMessage);
            if (graphVertexId != null) {
                auditRepository.audit(graphVertexId, auditMessage, getUser());
            }
        } finally {
            processingCount.getAndDecrement();
            totalProcessedCount.getAndIncrement();
            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            this.averageProcessingTime = (((totalProcessedCount.get() - 1) * this.averageProcessingTime) + processingTime) / totalProcessedCount.get();
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

    protected GraphVertex saveArtifact(ArtifactExtractedInfo artifactExtractedInfo) {
        Artifact artifact = saveArtifactModel(artifactExtractedInfo);
        GraphVertex artifactVertex = saveArtifactGraphVertex(artifactExtractedInfo, artifact);
        return artifactVertex;
    }

    private GraphVertex saveArtifactGraphVertex(ArtifactExtractedInfo artifactExtractedInfo, Artifact artifact) {
        if (artifactExtractedInfo.getUrl() != null && !artifactExtractedInfo.getUrl().isEmpty()) {
            artifactExtractedInfo.setSource(artifactExtractedInfo.getUrl());
        }

        String oldGraphVertexId = artifact.getMetadata().getGraphVertexId();
        boolean existingGraphVertex = oldGraphVertexId != null && oldGraphVertexId.length() > 0;

        GraphVertex vertex = artifactRepository.saveToGraph(artifact, artifactExtractedInfo, getUser());
        if (existingGraphVertex) {
            auditRepository.audit(vertex.getId(), "Updating Entity: " + vertex.getId(), getUser());
        } else {
            auditRepository.audit(vertex.getId(), "Entity created: " + vertex.getId(), getUser());
        }
        return vertex;
    }

    private Artifact saveArtifactModel(ArtifactExtractedInfo artifactExtractedInfo) {
        Artifact artifact = artifactRepository.findByRowKey(artifactExtractedInfo.getRowKey(), getUser().getModelUserContext());
        if (artifact == null) {
            artifact = new Artifact(artifactExtractedInfo.getRowKey());
            artifact.getMetadata().setCreateDate(new Date());
        }
        if (artifactExtractedInfo.getRaw() != null) {
            artifact.getMetadata().setRaw(artifactExtractedInfo.getRaw());
        }
        if (artifactExtractedInfo.getVideoTranscript() != null) {
            artifact.getMetadata().setVideoTranscript(artifactExtractedInfo.getVideoTranscript());
            artifact.getMetadata().setVideoDuration(Long.toString(artifactExtractedInfo.getVideoDuration()));

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
        if (artifactExtractedInfo.getMappingJson() != null) {
            artifact.getMetadata().setMappingJson(artifactExtractedInfo.getMappingJson());
        }
        if (artifactExtractedInfo.getTitle() != null) {
            artifact.getMetadata().setFileName(artifactExtractedInfo.getTitle());
        }
        if (artifactExtractedInfo.getFileExtension() != null) {
            artifact.getMetadata().setFileExtension(artifactExtractedInfo.getFileExtension());
        }
        if (artifactExtractedInfo.getMimeType() != null) {
            artifact.getMetadata().setMimeType(artifactExtractedInfo.getMimeType());
        }

        artifactRepository.save(artifact, getUser().getModelUserContext());
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

    @Inject
    public void setAuditRepository(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    protected boolean isArchive(String fileName) {
        fileName = fileName.toLowerCase();
        if (fileName.endsWith(".tar") || fileName.endsWith(".zip") || fileName.endsWith(".gz")) {
            return true;
        }
        return false;
    }

    @Override
    public long getProcessingCount() {
        return this.processingCount.get();
    }

    @Override
    public long getTotalProcessedCount() {
        return this.totalProcessedCount.get();
    }

    @Override
    public long getAverageProcessingTime() {
        return this.averageProcessingTime;
    }

    @Override
    public long getTotalErrorCount() {
        return this.totalErrorCount.get();
    }
}
