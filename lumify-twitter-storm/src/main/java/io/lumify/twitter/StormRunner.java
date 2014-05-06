package io.lumify.twitter;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.TopologyBuilder;
import io.lumify.storm.StormRunnerBase;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class StormRunner extends StormRunnerBase {
    private static final String TOPOLOGY_NAME = "lumify-twitter";
    private static final String OPT_FILE_NAME = "filename";
    private String fileName;

    public static void main(String[] args) throws Exception {
        int res = new StormRunner().run(args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption(
                OptionBuilder
                        .withLongOpt(OPT_FILE_NAME)
                        .withDescription("Name of file.")
                        .hasArg()
                        .withArgName("fileName")
                        .create("f")
        );

        return opts;
    }

    @Override
    protected String getTopologyName() {
        return TOPOLOGY_NAME;
    }

    @Override
    protected void beforeCreateTopology(CommandLine cmd, Config conf) throws Exception {
        super.beforeCreateTopology(cmd, conf);

        if (cmd.hasOption(OPT_FILE_NAME)) {
            fileName = cmd.getOptionValue(OPT_FILE_NAME);
        }
    }

    public StormTopology createTopology(int parallelismHint) {
        TopologyBuilder builder = new TopologyBuilder();
        createTweetProcessingTopology(builder, parallelismHint);
        return builder.createTopology();
    }

    private void createTweetProcessingTopology(TopologyBuilder builder, int parallelismHint) {
        IRichSpout spout;

        if (fileName != null) {
            spout = new TweetFileSpout(fileName);
        } else {
            spout = new TwitterStreamSpout();
        }

        String name = "lumify-twitter";
        builder.setSpout(name + "-spout", spout, 1)
                .setMaxTaskParallelism(1);
        builder.setBolt(name + "-bolt", new TweetProcessorBolt(), parallelismHint)
                .shuffleGrouping(name + "-spout");
    }
}