package com.altamiracorp.lumify.facebook;

import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import com.altamiracorp.lumify.storm.StormRunnerBase;

public class StormRunner extends StormRunnerBase {
    private static final String TOPOLOGY_NAME = "lumify-facebook";

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

    public StormTopology createTopology() {
        TopologyBuilder builder = new TopologyBuilder();
        createFacebookTopology(builder);
        return builder.createTopology();
    }

    private void createFacebookTopology(TopologyBuilder builder) {
        String spoutName = "facebookSpout";
        builder.setSpout(spoutName, new FacebookSpout(), 1);
        builder.setBolt(spoutName + "-bolt", new FacebookBolt(), 1)
                .shuffleGrouping(spoutName);
    }
}