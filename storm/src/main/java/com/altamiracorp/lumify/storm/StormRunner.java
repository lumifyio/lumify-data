package com.altamiracorp.lumify.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import com.altamiracorp.lumify.cmdline.CommandLineBase;
import com.altamiracorp.lumify.storm.contentTypeSorter.ContentTypeSorterBolt;
import com.altamiracorp.lumify.storm.document.DocumentBolt;
import com.altamiracorp.lumify.storm.termExtraction.TermExtractionBolt;
import com.altamiracorp.lumify.storm.textHighlighting.TextHighlightingBolt;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.kafka.KafkaConfig;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;

import java.util.Map;

public class StormRunner extends CommandLineBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(StormRunner.class);
    public static final String LOCAL_CONFIG_KEY = "local";
    public static final String TOPOLOGY_NAME = "lumify";
    private boolean isDone;

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new StormRunner(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    public StormRunner() {
        initFramework = true;
    }

    @Override
    protected Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption(
                OptionBuilder
                        .withLongOpt("datadir")
                        .withDescription("Location of the data directory")
                        .hasArg()
                        .create()
        );

        opts.addOption(
                OptionBuilder
                        .withLongOpt("local")
                        .withDescription("Run local")
                        .create()
        );

        return opts;
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        String dataDir = cmd.getOptionValue("datadir");
        boolean isLocal = cmd.hasOption("local");

        Config conf = new Config();
        conf.put("topology.kryo.factory", "com.altamiracorp.lumify.storm.DefaultKryoFactory");
        conf.put(LOCAL_CONFIG_KEY, isLocal);
        for (Map.Entry<Object, Object> configEntry : getConfiguration().getProperties().entrySet()) {
            conf.put(configEntry.getKey().toString(), configEntry.getValue());
        }
        conf.put(BaseFileSystemSpout.DATADIR_CONFIG_NAME, dataDir);
        conf.setDebug(false);
        conf.setNumWorkers(2);

        if (isLocal) {
            TopologyConfig topologyConfig = new TopologyConfig()
                    .setUnknownSpout(new DevFileSystemSpout())
                    .setDocumentSpout(new KafkaSpout(createSpoutConfig("document")))
                    .setImageSpout(new KafkaSpout(createSpoutConfig("image")))
                    .setVideoSpout(new KafkaSpout(createSpoutConfig("video")));
            StormTopology topology = createTopology(topologyConfig);
            LocalCluster cluster = new LocalCluster();
            LOGGER.info("Submitting topology '" + TOPOLOGY_NAME + "'");
            cluster.submitTopology(TOPOLOGY_NAME, conf, topology);

            // TODO: how do we know when we are done?
            while (!isDone) {
                Utils.sleep(100);
            }
            cluster.killTopology("local");
            cluster.shutdown();
        } else {
            TopologyConfig topologyConfig = new TopologyConfig()
                    .setUnknownSpout(new HdfsFileSystemSpout("/unknown"))
                    .setDocumentSpout(new HdfsFileSystemSpout("/document"))
                    .setImageSpout(new HdfsFileSystemSpout("/image"))
                    .setVideoSpout(new HdfsFileSystemSpout("/video"));
            StormTopology topology = createTopology(topologyConfig);
            LOGGER.info("Submitting topology '" + TOPOLOGY_NAME + "'");
            StormSubmitter.submitTopology(TOPOLOGY_NAME, conf, topology);
        }

        return 0;
    }

    public StormTopology createTopology(TopologyConfig topologyConfig) {
        TopologyBuilder builder = new TopologyBuilder();
        createContentTypeSorterTopology(builder, topologyConfig);
        createVideoTopology(builder, topologyConfig);
        createImageTopology(builder, topologyConfig);
        createDocumentTopology(builder, topologyConfig);
        createTextTopology(builder);

        return builder.createTopology();
    }

    private TopologyBuilder createContentTypeSorterTopology(TopologyBuilder builder, TopologyConfig topologyConfig) {
        builder.setSpout("fileSorter", topologyConfig.getUnknownSpout(), 1);
        builder.setBolt("contentTypeSorterBolt", new ContentTypeSorterBolt(), 1)
                .shuffleGrouping("fileSorter");
        return builder;
    }

    private void createImageTopology(TopologyBuilder builder, TopologyConfig topologyConfig) {
        String queueName = "image";
        builder.setSpout(queueName, topologyConfig.getImageSpout(), 1);
        builder.setBolt("debug-" + queueName, new DebugBolt(queueName), 1)
                .shuffleGrouping(queueName);
    }

    private void createVideoTopology(TopologyBuilder builder, TopologyConfig topologyConfig) {
        String queueName = "video";
        builder.setSpout(queueName, topologyConfig.getVideoSpout(), 1);
        builder.setBolt("debug-" + queueName, new DebugBolt(queueName), 1)
                .shuffleGrouping(queueName);
    }

    private void createDocumentTopology(TopologyBuilder builder, TopologyConfig topologyConfig) {
        String queueName = "document";
        builder.setSpout(queueName, topologyConfig.getDocumentSpout(), 1);
        builder.setBolt(queueName + "-bolt", new DocumentBolt(), 1)
                .shuffleGrouping(queueName);
    }

    private void createTextTopology(TopologyBuilder builder) {
        SpoutConfig spoutConfig = createSpoutConfig("text");
        builder.setSpout("text", new KafkaSpout(spoutConfig), 1);
        builder.setBolt("textTermExtractionBolt", new TermExtractionBolt(), 1)
                .shuffleGrouping("text");
        builder.setBolt("textHighlightingBolt", new TextHighlightingBolt(), 1)
                .shuffleGrouping("textTermExtractionBolt");
    }

    private SpoutConfig createSpoutConfig(String queueName) {
        SpoutConfig spoutConfig = new SpoutConfig(
                new KafkaConfig.ZkHosts(getConfiguration().getZookeeperServerNames(), "/kafka/brokers"),
                queueName,
                "/kafka/consumers",
                queueName);
        spoutConfig.scheme = new KafkaJsonEncoder();
        return spoutConfig;
    }

    private class TopologyConfig {
        private IRichSpout unknownSpout;
        private IRichSpout imageSpout;
        private IRichSpout videoSpout;
        private IRichSpout documentSpout;

        private IRichSpout getUnknownSpout() {
            return unknownSpout;
        }

        private TopologyConfig setUnknownSpout(IRichSpout unknownSpout) {
            this.unknownSpout = unknownSpout;
            return this;
        }

        private IRichSpout getImageSpout() {
            return imageSpout;
        }

        private TopologyConfig setImageSpout(IRichSpout imageSpout) {
            this.imageSpout = imageSpout;
            return this;
        }

        private IRichSpout getVideoSpout() {
            return videoSpout;
        }

        private TopologyConfig setVideoSpout(IRichSpout videoSpout) {
            this.videoSpout = videoSpout;
            return this;
        }

        private IRichSpout getDocumentSpout() {
            return documentSpout;
        }

        private TopologyConfig setDocumentSpout(IRichSpout documentSpout) {
            this.documentSpout = documentSpout;
            return this;
        }
    }
}
