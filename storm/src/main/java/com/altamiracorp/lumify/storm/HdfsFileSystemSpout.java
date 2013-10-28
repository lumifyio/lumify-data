package com.altamiracorp.lumify.storm;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.utils.Utils;

import com.altamiracorp.lumify.core.config.ConfigurationHelper;

public class HdfsFileSystemSpout extends BaseFileSystemSpout {
    private static final Logger LOGGER = LoggerFactory.getLogger(HdfsFileSystemSpout.class);

    private final String subDir;
    private FileSystem hdfsFileSystem;
    private String readPath;

    public HdfsFileSystemSpout(String subDir) {
        this.subDir = subDir;
    }

    @Override
    public void open(Map stormConf, TopologyContext context, SpoutOutputCollector collector) {
        super.open(stormConf, context, collector);

        String rootDataPath = (String) stormConf.get(BaseFileSystemSpout.DATADIR_CONFIG_NAME);
        checkNotNull(rootDataPath, BaseFileSystemSpout.DATADIR_CONFIG_NAME + " is a required configuration parameter");

        readPath = rootDataPath + subDir;
        Configuration conf = ConfigurationHelper.createHadoopConfigurationFromMap(stormConf);

        try {
            String hdfsRootDir = (String) stormConf.get(com.altamiracorp.lumify.core.config.Configuration.HADOOP_URL);
            LOGGER.info("opening hdfs file system " + hdfsRootDir);
            hdfsFileSystem = FileSystem.get(new URI(hdfsRootDir), conf, "hadoop");
            if (!hdfsFileSystem.exists(new Path(readPath))) {
                LOGGER.info("making hdfs directory " + readPath + " on " + hdfsRootDir);
                hdfsFileSystem.mkdirs(new Path(readPath));
            }
        } catch (Exception e) {
            collector.reportError(e);
        }
    }

    @Override
    public void nextTuple() {
        try {
            Path path = new Path(readPath);
            if (!processPath(path)) {
                Utils.sleep(1000);
            }
        } catch (IOException e) {
            getCollector().reportError(e);
        }
    }

    @Override
    public void safeAck(Object msgId) throws Exception {
        String path = getPathFromMessageId(msgId);
        checkNotNull(path);

        Path ackedMessagePath = new Path(path);
        if (hdfsFileSystem.exists(ackedMessagePath)) {
            if( hdfsFileSystem.delete(ackedMessagePath, false) ) {
                LOGGER.debug("Deleted message path: " + ackedMessagePath);
            } else {
                LOGGER.debug("Could not delete message path: " + ackedMessagePath);
            }
        }

        super.safeAck(msgId);
    }

    // TODO: we could speed this up by caching the list of files to work on instead of reading all of them each time
    private boolean processPath(Path path) throws IOException {
        checkState(hdfsFileSystem != null, "hdfsFileSystem is not initialized");

        FileStatus[] files = hdfsFileSystem.listStatus(path);
        for (FileStatus file : files) {
            Path filePath = file.getPath();
            if (file.isDir()) {
                if (processPath(filePath)) {
                    return true;
                }
            }

            if (isInWorkingSet(filePath.toString())) {
                continue;
            }

            emit(filePath.toString());
            return true;
        }
        return false;
    }
}
