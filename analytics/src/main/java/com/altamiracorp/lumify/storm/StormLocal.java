package com.altamiracorp.lumify.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.StormTopology;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
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
        conf.setDebug(false);
        conf.setNumWorkers(2);

        DevFileSystemSpout devFileSystemSpout = new DevFileSystemSpout(dataDir);

        LocalCluster cluster = new LocalCluster();
        StormTopology topology = createTopology(devFileSystemSpout);
        cluster.submitTopology("local", conf, topology);
        // TODO: how do we know when we are done?
        while (!isDone) {
            Utils.sleep(100);
        }
        cluster.killTopology("local");
        cluster.shutdown();

        return 0;
    }

    public StormTopology createTopology(DevFileSystemSpout devFileSystemSpout) {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(FILE_CONTENT_TYPE_SORTER_ID, devFileSystemSpout, 1);
        builder.setBolt("contentTypeSorterBolt", new ContentTypeSorterBolt(), 10)
                .shuffleGrouping(FILE_CONTENT_TYPE_SORTER_ID);

        String contentTypeName = ContentTypeSorterBolt.SimpleType.TEXT.toString().toLowerCase();
        SpoutConfig spoutConfig = new SpoutConfig(
                new KafkaConfig.ZkHosts(getConfiguration().getZookeeperServerNames(), "/brokers"),
                contentTypeName,
                "/consumers",
                contentTypeName);
        builder.setSpout(contentTypeName, new KafkaSpout(spoutConfig), 10);
        builder.setBolt("debug", new DebugBolt(contentTypeName), 10)
                .shuffleGrouping(contentTypeName);

        return builder.createTopology();
    }

    public static class DebugBolt extends BaseRichBolt {

        private final String contentTypeName;
        private OutputCollector collector;

        public DebugBolt(String contentTypeName) {
            this.contentTypeName = contentTypeName;
        }

        @Override
        public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
            this.collector = collector;
        }

        @Override
        public void execute(Tuple input) {
            LOGGER.info("debug (" + contentTypeName + "): " + new String((byte[]) input.getValue(0)));

            collector.ack(input);
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
        }
    }
}
