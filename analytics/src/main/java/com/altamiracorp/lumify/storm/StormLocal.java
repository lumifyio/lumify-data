package com.altamiracorp.lumify.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import com.altamiracorp.lumify.cmdline.CommandLineBase;
import com.altamiracorp.lumify.contentTypeExtraction.ContentTypeSorterBolt;
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

import java.io.File;
import java.util.Map;

public class StormLocal extends CommandLineBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(StormLocal.class.getName());
    public static final String FILE_CONTENT_TYPE_SORTER_ID = "fileContentTypeExtraction";
    private boolean isDone;

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new StormLocal(), args);
        if (res != 0) {
            System.exit(res);
        }
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

        return opts;
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        File dataDir = new File(cmd.getOptionValue("datadir"));

        Config conf = new Config();
        conf.put("topology.kryo.factory", "com.altamiracorp.lumify.storm.DefaultKryoFactory");
        for (Map.Entry<Object, Object> configEntry : getConfiguration().getProperties().entrySet()) {
            conf.put(configEntry.getKey().toString(), configEntry.getValue());
        }
        conf.put(DevFileSystemSpout.DATADIR_CONFIG_NAME, dataDir.getAbsolutePath());
        conf.setDebug(false);
        conf.setNumWorkers(2);

        LocalCluster cluster = new LocalCluster();
        StormTopology topology = createTopology();
        cluster.submitTopology("local", conf, topology);
        // TODO: how do we know when we are done?
        while (!isDone) {
            Utils.sleep(100);
        }
        cluster.killTopology("local");
        cluster.shutdown();

        return 0;
    }

    public StormTopology createTopology() {
        TopologyBuilder builder = new TopologyBuilder();
        createContentTypeSorterTopology(builder);
        createVideoTopology(builder);
        createImageTopology(builder);
        createDocumentTopology(builder);

        return builder.createTopology();
    }

    private TopologyBuilder createContentTypeSorterTopology(TopologyBuilder builder) {
        builder.setSpout(FILE_CONTENT_TYPE_SORTER_ID, new DevFileSystemSpout(), 1);
        builder.setBolt("contentTypeSorterBolt", new ContentTypeSorterBolt(), 10)
                .shuffleGrouping(FILE_CONTENT_TYPE_SORTER_ID);
        return builder;
    }

    private void createImageTopology(TopologyBuilder builder) {
        String queueName = "image";
        SpoutConfig spoutConfig = createSpoutConfig(queueName);
        builder.setSpout(queueName, new KafkaSpout(spoutConfig), 10);
        builder.setBolt("debug-" + queueName, new DebugBolt(queueName), 10)
                .shuffleGrouping(queueName);
    }

    private void createVideoTopology(TopologyBuilder builder) {
        String queueName = "video";
        SpoutConfig spoutConfig = createSpoutConfig(queueName);
        builder.setSpout(queueName, new KafkaSpout(spoutConfig), 10);
        builder.setBolt("debug-" + queueName, new DebugBolt(queueName), 10)
                .shuffleGrouping(queueName);
    }

    private void createDocumentTopology(TopologyBuilder builder) {
        String queueName = "document";
        SpoutConfig spoutConfig = createSpoutConfig(queueName);
        builder.setSpout(queueName + "-spout", new KafkaSpout(spoutConfig), 10);
        builder.setBolt(queueName + "-bolt", new DocumentBolt(), 10)
                .shuffleGrouping(queueName + "-spout");
    }

    private SpoutConfig createSpoutConfig(String queueName) {
        SpoutConfig spoutConfig = new SpoutConfig(
                new KafkaConfig.ZkHosts(getConfiguration().getZookeeperServerNames(), "/brokers"),
                queueName,
                "/consumers",
                queueName);
        spoutConfig.scheme = new KafkaJsonEncoder();
        return spoutConfig;
    }
}
