package com.altamiracorp.lumify.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import com.altamiracorp.lumify.core.cmdline.CommandLineBase;
import com.altamiracorp.lumify.core.config.ConfigurationHelper;
import com.altamiracorp.lumify.core.model.workQueue.WorkQueueRepository;
import com.altamiracorp.lumify.storm.contentTypeSorter.ContentTypeSorterBolt;
import com.altamiracorp.lumify.storm.document.DocumentBolt;
import com.altamiracorp.lumify.storm.image.ImageBolt;
import com.altamiracorp.lumify.storm.structuredData.StructuredDataTextExtractorBolt;
import com.altamiracorp.lumify.storm.term.extraction.TermExtractionBolt;
import com.altamiracorp.lumify.storm.video.VideoBolt;
import com.altamiracorp.lumify.storm.video.VideoPreviewBolt;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.jvnet.inflector.Noun;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class StormEnterpriseRunner extends CommandLineBase {
    private static final String ROOT_DATA_DIR = "/lumify/data";
    private static final String UNKNOWN_DATA_DIR = "unknown";

    private static final String CMD_OPT_LOCAL = "local";
    private static final String CMD_OPT_DATADIR = "datadir";
    private static final String TOPOLOGY_NAME = "lumify-enterprise";

    public static void main(String[] args) throws Exception {
        int res = new StormEnterpriseRunner().run(args);
        if (res != 0) {
            System.exit(res);
        }
    }

    public StormEnterpriseRunner() {
        initFramework = true;
    }

    @Override
    protected Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption(
                OptionBuilder
                        .withLongOpt(CMD_OPT_DATADIR)
                        .withDescription("Location of the data directory")
                        .hasArg()
                        .create()
        );

        opts.addOption(
                OptionBuilder
                        .withLongOpt(CMD_OPT_LOCAL)
                        .withDescription("Run local")
                        .create()
        );

        return opts;
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        String dataDir = cmd.getOptionValue(CMD_OPT_DATADIR);
        boolean isLocal = cmd.hasOption(CMD_OPT_LOCAL);

        Config conf = new Config();
        conf.put("topology.kryo.factory", "com.altamiracorp.lumify.storm.DefaultKryoFactory");
        for (String key : getConfiguration().getKeys()) {
            conf.put(key, getConfiguration().get(key));
        }
        conf.put(BaseFileSystemSpout.DATADIR_CONFIG_NAME, ROOT_DATA_DIR);
        conf.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, 10000);
        conf.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);
        conf.put(Config.WORKER_CHILDOPTS, " -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=1%ID% ");
        conf.setDebug(false);
        conf.setNumWorkers(2);

        if (isLocal) {
            copyDataFilesToHdfs(conf, dataDir);
        }

        StormTopology topology = createTopology();
        LOGGER.info("Created topology layout: " + topology);
        LOGGER.info(String.format("Submitting topology '%s'", TOPOLOGY_NAME));

        if (isLocal) {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology(TOPOLOGY_NAME, conf, topology);

            while (!willExit()) {
                Utils.sleep(100);
            }

            cluster.killTopology(TOPOLOGY_NAME);
            cluster.shutdown();
        } else {
            StormSubmitter.submitTopology(TOPOLOGY_NAME, conf, topology);
        }

        return 0;
    }

    private void copyDataFilesToHdfs(Config stormConf, String dataDir) throws URISyntaxException, IOException, InterruptedException {
        LOGGER.debug("Copying files to HDFS");
        File dataDirFile = new File(dataDir);
        String hdfsRootDir = (String) stormConf.get(com.altamiracorp.lumify.core.config.Configuration.HADOOP_URL);
        Configuration conf = ConfigurationHelper.createHadoopConfigurationFromMap(stormConf);

        FileSystem hdfsFileSystem = FileSystem.get(new URI(hdfsRootDir), conf, "hadoop");
        Path unknownDataDir = new Path(ROOT_DATA_DIR, UNKNOWN_DATA_DIR);

        int totalFileCount = dataDirFile.listFiles().length;
        int fileCount = 0;
        for (File f : dataDirFile.listFiles()) {
            Path srcPath = new Path(f.getAbsolutePath());
            if (srcPath.getName().startsWith(".") || f.length() == 0) {
                continue;
            }

            Path dstPath = new Path(unknownDataDir, srcPath.getName());
            LOGGER.debug("Copying file (" + (fileCount + 1) + "/" + totalFileCount + "): " + srcPath + " -> " + dstPath);
            hdfsFileSystem.copyFromLocalFile(false, true, srcPath, dstPath);
            fileCount++;
        }

        LOGGER.debug(String.format("Copied %d %s from %s to HDFS: %s", fileCount, Noun.pluralOf("file", fileCount), dataDirFile, unknownDataDir));
    }

    public StormTopology createTopology() {
        TopologyBuilder builder = new TopologyBuilder();
        createContentTypeSorterTopology(builder);
        createVideoTopology(builder);
        createImageTopology(builder);
        createDocumentTopology(builder);
        createTextTopology(builder);
        createProcessedVideoTopology(builder);
        createStructuredDataTextTopology(builder);

        return builder.createTopology();
    }

    private TopologyBuilder createContentTypeSorterTopology(TopologyBuilder builder) {
        String queueName = "contentType";
        builder.setSpout(queueName, new HdfsFileSystemSpout("/unknown"), 1);
        builder.setBolt(queueName + "-bolt", new ContentTypeSorterBolt(), 1)
                .shuffleGrouping(queueName);
        return builder;
    }

    private void createImageTopology(TopologyBuilder builder) {
        String queueName = "image";
        builder.setSpout(queueName, new HdfsFileSystemSpout("/image"), 1);
        builder.setBolt(queueName + "-bolt", new ImageBolt(), 1)
                .shuffleGrouping(queueName);
    }

    private void createVideoTopology(TopologyBuilder builder) {
        String queueName = "video";
        builder.setSpout(queueName, new HdfsFileSystemSpout("/video"), 1);
        builder.setBolt(queueName + "-bolt", new VideoBolt(), 1)
                .shuffleGrouping(queueName);
    }

    private void createDocumentTopology(TopologyBuilder builder) {
        String queueName = "document";
        builder.setSpout(queueName, new HdfsFileSystemSpout("/document"), 1);
        builder.setBolt(queueName + "-bolt", new DocumentBolt(), 1)
                .shuffleGrouping(queueName);
    }

    private void createStructuredDataTextTopology(TopologyBuilder builder) {
        String queueName = "structuredData";
        builder.setSpout(queueName, new HdfsFileSystemSpout("/structuredData"), 1);
        builder.setBolt(queueName + "-bolt", new StructuredDataTextExtractorBolt(), 1)
                .shuffleGrouping(queueName);
    }

    private void createTextTopology(TopologyBuilder builder) {
        builder.setSpout("text", new LumifyKafkaSpout(getConfiguration(), WorkQueueRepository.TEXT_QUEUE_NAME), 1);
        builder.setBolt("textTermExtractionBolt", new TermExtractionBolt(), 1)
                .shuffleGrouping("text");
    }

    private void createProcessedVideoTopology(TopologyBuilder builder) {
        builder.setSpout("processedVideoSpout", new LumifyKafkaSpout(getConfiguration(), WorkQueueRepository.PROCESSED_VIDEO_QUEUE_NAME), 1);
        builder.setBolt("processedVideoBolt", new VideoPreviewBolt(), 1)
                .shuffleGrouping("processedVideoSpout");
    }
}
