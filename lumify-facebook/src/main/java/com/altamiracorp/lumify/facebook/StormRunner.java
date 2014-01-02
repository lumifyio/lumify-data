package com.altamiracorp.lumify.facebook;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import com.altamiracorp.lumify.storm.StormRunnerBase;
import org.apache.commons.cli.CommandLine;

public class StormRunner extends StormRunnerBase {
    private static final String TOPOLOGY_NAME = "lumify-facebook";
    private Boolean startFileSpout = false;
    private Boolean startStreamingSpout = false;
    private static final String FACEBOOK_FILE_DIR = "facebook.fileProcessDirectory";
    private static final String FACEBOOK_STREAM_PERMISSIONS = "facebook.permissions";
    private String hdfsFacebookSubdir;

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
    protected void beforeCreateTopology(CommandLine cmd, Config conf) throws Exception {
        super.beforeCreateTopology(cmd, conf);
        if (conf.get(FACEBOOK_FILE_DIR) != null) {
            hdfsFacebookSubdir = (String) conf.get(FACEBOOK_FILE_DIR);
            startFileSpout = true;
        }
        if (conf.get(FACEBOOK_STREAM_PERMISSIONS) != null) {
            startStreamingSpout = true;
        }
        if (!(startFileSpout || startStreamingSpout)) {
            throw new IllegalStateException("Must have at least one Facebook spout");
        }
    }

    public StormTopology createTopology(int parallelismHint) {
        TopologyBuilder builder = new TopologyBuilder();
        if (startFileSpout) {
            createFacebookFileTopology(builder, parallelismHint);
        }
        if (startStreamingSpout) {
            createFacebookStreamingTopology(builder, parallelismHint);
        }
        return builder.createTopology();
    }

    private void createFacebookFileTopology(TopologyBuilder builder, int parallelismHint) {
//        String spoutName = "facebookFileProcessingSpout";
//        builder.setSpout(spoutName, new FacebookFileProcessingSpout(hdfsFacebookSubdir), 1);
//        builder.setBolt(spoutName + "-bolt", new FacebookBolt(), parallelismHint)
//                .shuffleGrouping(spoutName);
    }

    private void createFacebookStreamingTopology(TopologyBuilder builder, int parallelismHint) {
        String spoutName = "facebookStreamingSpout";
        builder.setSpout(spoutName, new FacebookStreamingSpout(), 1);
        builder.setBolt(spoutName + "-bolt", new FacebookBolt(), parallelismHint)
                .shuffleGrouping(spoutName);
    }
}