package com.altamiracorp.lumify.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import com.altamiracorp.lumify.cmdline.CommandLineBase;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(StormRunner.class.getName());
    public static final String FILE_CONTENT_TYPE_SORTER_ID = "fileContentTypeExtraction";
    public static final String LOCAL_CONFIG_KEY = "local";
    private boolean isDone;

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new StormRunner(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    public StormRunner() {
        initFramework = false;
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
            StormTopology topology = createTopology(new DevFileSystemSpout());
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("local", conf, topology);

            // TODO: how do we know when we are done?
            while (!isDone) {
                Utils.sleep(100);
            }
            cluster.killTopology("local");
            cluster.shutdown();
        } else {
            StormTopology topology = createTopology(new HdfsFileSystemSpout());
            StormSubmitter.submitTopology("lumify", conf, topology);
        }

        return 0;
    }

    public StormTopology createTopology(IRichSpout fileSpout) {
        TopologyBuilder builder = new TopologyBuilder();
        createContentTypeSorterTopology(builder, fileSpout);
        createVideoTopology(builder);
        createImageTopology(builder);
        createDocumentTopology(builder);
        createTextTopology(builder);

        return builder.createTopology();
    }

    private TopologyBuilder createContentTypeSorterTopology(TopologyBuilder builder, IRichSpout fileSpout) {
        builder.setSpout(FILE_CONTENT_TYPE_SORTER_ID, fileSpout, 1);
        builder.setBolt("contentTypeSorterBolt", new ContentTypeSorterBolt(), 1)
                .shuffleGrouping(FILE_CONTENT_TYPE_SORTER_ID);
        return builder;
    }

    private void createImageTopology(TopologyBuilder builder) {
        String queueName = "image";
        SpoutConfig spoutConfig = createSpoutConfig(queueName);
        builder.setSpout(queueName, new KafkaSpout(spoutConfig), 1);
        builder.setBolt("debug-" + queueName, new DebugBolt(queueName), 1)
                .shuffleGrouping(queueName);
    }

    private void createVideoTopology(TopologyBuilder builder) {
        String queueName = "video";
        SpoutConfig spoutConfig = createSpoutConfig(queueName);
        builder.setSpout(queueName, new KafkaSpout(spoutConfig), 1);
        builder.setBolt("debug-" + queueName, new DebugBolt(queueName), 1)
                .shuffleGrouping(queueName);
    }

    private void createDocumentTopology(TopologyBuilder builder) {
        String queueName = "document";
        SpoutConfig spoutConfig = createSpoutConfig(queueName);
        builder.setSpout(queueName + "-spout", new KafkaSpout(spoutConfig), 1);
        builder.setBolt(queueName + "-bolt", new DocumentBolt(), 1)
                .shuffleGrouping(queueName + "-spout");
    }

    private void createTextTopology(TopologyBuilder builder) {
        SpoutConfig spoutConfig = createSpoutConfig("text");
        builder.setSpout("textSpout", new KafkaSpout(spoutConfig), 1);
        builder.setBolt("textExtractionBolt", new TextExtractionBolt(), 1)
                .shuffleGrouping("textSpout");
        builder.setBolt("textHighlightingBolt", new TextHighlightingBolt(), 1)
                .shuffleGrouping("textExtractionBolt");
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
}
