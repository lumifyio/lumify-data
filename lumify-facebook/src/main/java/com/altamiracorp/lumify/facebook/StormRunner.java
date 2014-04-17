package com.altamiracorp.lumify.facebook;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import com.altamiracorp.lumify.facebook.BaseFileSystemSpout;
import com.altamiracorp.lumify.facebook.HdfsFileSystemSpout;
import com.altamiracorp.lumify.storm.StormRunnerBase;
import org.apache.commons.cli.CommandLine;

public class StormRunner extends StormRunnerBase {
    private static final String TOPOLOGY_NAME = "lumify-facebook";
    private Boolean startFileSpout = false;
    private Boolean startStreamingSpout = false;
    private static final String FACEBOOK_FILE_DIR = "facebook.fileProcessDirectory";
    private static final String FACEBOOK_STREAM_TABLES = "facebook.tables";
    private static final String HDFS_FACEBOOK_SUBDIR_PROPERTY = "facebook.hdfs.queryPath";
    private static final String HDFS_ROOT_PATH_PROPERTY = "facebook.hdfs.dataRoot";
    private static final String DEFAULT_HDFS_FACEBOOK_SUBDIR = "rawFacebook";
    private static final String DEFAULT_HDFS_DATA_ROOT = "/lumify/data";
    private String hdfsDataRoot;
    private String hdfsFacebookSubdir;
    public static final String DATADIR_CONFIG_NAME = "datadir";


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
        if (conf.get(FACEBOOK_STREAM_TABLES) != null) {
            startStreamingSpout = true;
        }
        if (conf.get(FACEBOOK_FILE_DIR) != null) {
            startFileSpout = true;
            String rootProp = (String) conf.get(HDFS_ROOT_PATH_PROPERTY);
            String subDirProp = (String) conf.get(HDFS_FACEBOOK_SUBDIR_PROPERTY);
            hdfsDataRoot = rootProp != null ? rootProp.trim() : DEFAULT_HDFS_DATA_ROOT;
            hdfsFacebookSubdir = (subDirProp != null && !subDirProp.trim().isEmpty()) ? subDirProp.trim() : DEFAULT_HDFS_FACEBOOK_SUBDIR;
            if (!hdfsFacebookSubdir.startsWith("/")) {
                hdfsFacebookSubdir = "/" + hdfsFacebookSubdir;
            }
            conf.put(DATADIR_CONFIG_NAME, hdfsDataRoot);
        }
        if (!(startFileSpout || startStreamingSpout)) {
            throw new IllegalStateException("Must have at least one Facebook spout when running Facebook Storm Topology");
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
        String spoutName = "facebookFileProcessingSpout";
        builder.setSpout(spoutName, new HdfsFileSystemSpout(hdfsFacebookSubdir), 1);
        builder.setBolt(spoutName + "-FileProcessingBolt", new FacebookFileProcessingBolt(), parallelismHint)
                .shuffleGrouping(spoutName);
        builder.setBolt(spoutName + "-BaseProcessingBolt", new FacebookBolt(), parallelismHint)
                .shuffleGrouping(spoutName + "-FileProcessingBolt");
    }

    private void createFacebookStreamingTopology(TopologyBuilder builder, int parallelismHint) {
        String spoutName = "facebookStreamingSpout";
        builder.setSpout(spoutName, new FacebookStreamingSpout(), 1);
        builder.setBolt(spoutName + "-BaseProcessingBolt", new FacebookBolt(), parallelismHint)
                .shuffleGrouping(spoutName);
    }
}