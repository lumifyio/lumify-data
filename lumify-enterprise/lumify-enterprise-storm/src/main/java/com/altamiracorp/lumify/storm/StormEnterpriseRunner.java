package com.altamiracorp.lumify.storm;

import backtype.storm.Config;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;
import com.altamiracorp.lumify.core.config.ConfigurationHelper;
import com.altamiracorp.lumify.core.model.workQueue.WorkQueueRepository;
import com.altamiracorp.lumify.storm.video.VideoBolt;
import com.altamiracorp.lumify.storm.video.VideoPreviewBolt;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.jvnet.inflector.Noun;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class StormEnterpriseRunner extends StormRunnerBase {
    private static final String ROOT_DATA_DIR = "/lumify/data";
    private static final String UNKNOWN_DATA_DIR = "unknown";

    private static final String CMD_OPT_DATADIR = "datadir";
    private static final String TOPOLOGY_NAME = "lumify-enterprise";

    public static void main(String[] args) throws Exception {
        int res = new StormEnterpriseRunner().run(args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected Options getOptions() {
        Options opts = super.getOptions();

        opts.addOption(
                OptionBuilder
                        .withLongOpt(CMD_OPT_DATADIR)
                        .withDescription("Location of the data directory")
                        .hasArg()
                        .create()
        );

        return opts;
    }

    @Override
    protected void beforeCreateTopology(CommandLine cmd, Config conf) throws Exception {
        super.beforeCreateTopology(cmd, conf);

        String dataDir = cmd.getOptionValue(CMD_OPT_DATADIR);

        if (isLocal()) {
            copyDataFilesToHdfs(conf, dataDir);
        }
    }

    @Override
    protected Config createConfig(CommandLine cmd) {
        Config conf = super.createConfig(cmd);
        conf.put(BaseFileSystemSpout.DATADIR_CONFIG_NAME, ROOT_DATA_DIR);
        return conf;
    }

    @Override
    protected String getTopologyName() {
        return TOPOLOGY_NAME;
    }

    private void copyDataFilesToHdfs(Config stormConf, String dataDir) throws URISyntaxException, IOException, InterruptedException {
        LOGGER.debug("Copying files to HDFS");
        File dataDirFile = new File(dataDir);
        String hdfsRootDir = (String) stormConf.get(com.altamiracorp.lumify.core.config.Configuration.HADOOP_URL);
        Configuration conf = ConfigurationHelper.createHadoopConfigurationFromMap(stormConf);

        FileSystem hdfsFileSystem = FileSystem.get(new URI(hdfsRootDir), conf, "hadoop");
        Path unknownDataDir = new Path(ROOT_DATA_DIR, UNKNOWN_DATA_DIR);

        int totalFileCount = dataDirFile.listFiles().length;
        int fileCount = 0;
        for (File f : dataDirFile.listFiles()) {
            Path srcPath = new Path(f.getAbsolutePath());
            if (srcPath.getName().startsWith(".") || f.length() == 0) {
                continue;
            }

            Path dstPath = new Path(unknownDataDir, srcPath.getName());
            LOGGER.debug("Copying file (%d/%d): %s -> %s", fileCount + 1, totalFileCount, srcPath, dstPath);
            hdfsFileSystem.copyFromLocalFile(false, true, srcPath, dstPath);
            fileCount++;
        }

        LOGGER.debug(String.format("Copied %d %s from %s to HDFS: %s", fileCount, Noun.pluralOf("file", fileCount), dataDirFile, unknownDataDir));
    }

    public StormTopology createTopology(int parallelismHint) {
        TopologyBuilder builder = new TopologyBuilder();
        createVideoTopology(builder, parallelismHint);
        createProcessedVideoTopology(builder, parallelismHint);

        return builder.createTopology();
    }

    private void createVideoTopology(TopologyBuilder builder, int parallelismHint) {
        String name = "video";
        builder.setSpout(name + "-spout", new HdfsFileSystemSpout("/video"), 1)
                .setMaxTaskParallelism(1);
        builder.setBolt(name + "-bolt", new VideoBolt(), parallelismHint)
                .shuffleGrouping(name + "-spout");
    }

    private void createProcessedVideoTopology(TopologyBuilder builder, int parallelismHint) {
        String name = "processedVideo";
        builder.setSpout(name + "-spout", createWorkQueueRepositorySpout(WorkQueueRepository.PROCESSED_VIDEO_QUEUE_NAME), 1)
                .setMaxTaskParallelism(1);
        builder.setBolt(name + "-bolt", new VideoPreviewBolt(), parallelismHint)
                .shuffleGrouping(name + "-spout");
    }
}
