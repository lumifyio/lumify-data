package com.altamiracorp.lumify.storm.twitter;

import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import com.altamiracorp.lumify.storm.StormRunnerBase;

public class StormRunner extends StormRunnerBase {
    private static final String TOPOLOGY_NAME = "lumify-twitter";

    public static void main(String[] args) throws Exception {
        int res = new StormRunner().run(args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected String getTopologyName() {
        return TOPOLOGY_NAME;
    }

    @Override
    public StormTopology createTopology() {
        TopologyBuilder builder = new TopologyBuilder();
        createTwitterStreamTopology(builder);
        return builder.createTopology();
    }

    private void createTwitterStreamTopology(TopologyBuilder builder) {
        String spoutName = "twitterStreamSpout";
        builder.setSpout(spoutName, new TwitterStreamSpout(), 1);
        builder.setBolt(spoutName + "-bolt", new TwitterStreamingBolt(), 1)
                .shuffleGrouping(spoutName);
    }
}