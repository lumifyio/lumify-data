package com.altamiracorp.lumify.cmdline;

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
import com.altamiracorp.lumify.storm.DevFileSystemSpout;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class StormLocal extends CommandLineBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(StormLocal.class.getName());
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
        conf.setDebug(false);
        conf.setNumWorkers(2);

        DevFileSystemSpout devFileSystemSpout = new DevFileSystemSpout(dataDir);

        LocalCluster cluster = new LocalCluster();
        StormTopology topology = createTopology(devFileSystemSpout);
        cluster.submitTopology("local", conf, topology);
        while (!isDone) {
            Utils.sleep(100);
        }
        cluster.killTopology("local");
        cluster.shutdown();

        return 0;
    }

    public StormTopology createTopology(DevFileSystemSpout devFileSystemSpout) {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("files", devFileSystemSpout, 10);
        builder.setBolt("print", new PrintBolt(), 10)
                .shuffleGrouping("files");
        return builder.createTopology();
    }

    private static class PrintBolt extends BaseRichBolt {
        private OutputCollector collector;

        @Override
        public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
            this.collector = collector;
        }

        @Override
        public void execute(Tuple input) {
            LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + input.getStringByField(DevFileSystemSpout.FILE_NAME_FIELD_NAME));
            collector.ack(input);
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer) {
        }
    }
}
