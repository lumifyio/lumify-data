package com.altamiracorp.lumify.storm;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.utils.Utils;
import com.altamiracorp.lumify.config.ConfigurationHelper;
import com.altamiracorp.lumify.model.AccumuloSession;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class HdfsFileSystemSpout extends BaseFileSystemSpout {
    private static final Logger LOGGER = LoggerFactory.getLogger(HdfsFileSystemSpout.class.getName());
    private final String subDir;

    private FileSystem hdfsFileSystem;
    private String rootDataPath;
    private String importPath;

    public HdfsFileSystemSpout(String subDir) {
        this.subDir = subDir;
    }

    @Override
    public void open(Map stormConf, TopologyContext context, SpoutOutputCollector collector) {
        super.open(stormConf, context, collector);

        this.rootDataPath = (String) stormConf.get(BaseFileSystemSpout.DATADIR_CONFIG_NAME);
        checkNotNull(this.rootDataPath, BaseFileSystemSpout.DATADIR_CONFIG_NAME + " is a required configuration parameter");
        this.importPath = rootDataPath + this.subDir;
        Configuration conf = ConfigurationHelper.createHadoopConfigurationFromMap(stormConf);
        try {
            String hdfsRootDir = (String) stormConf.get(AccumuloSession.HADOOP_URL);
            hdfsFileSystem = FileSystem.get(new URI(hdfsRootDir), conf, "hadoop");
            mkdirs(new Path(this.importPath));
        } catch (Exception e) {
            collector.reportError(e);
        }
    }

    private void mkdirs(Path path) throws IOException {
        if (!hdfsFileSystem.exists(path)) {
            hdfsFileSystem.mkdirs(path);
        }
    }

    @Override
    public void nextTuple() {
        try {
            Path path = new Path(importPath);
            if (!processPath(path)) {
                Utils.sleep(10 * 1000);
            }
        } catch (IOException e) {
            getCollector().reportError(e);
        }
    }

    @Override
    public void safeAck(Object msgId) throws Exception {
        String path = getPathFromMessageId(msgId);
        checkNotNull(path, "path was null");
        if (hdfsFileSystem.exists(new Path(path))) {
            hdfsFileSystem.delete(new Path(path), false);
        }
        super.safeAck(msgId);
    }

    // TODO: we could speed this up by caching the list of files to work on instead of reading all of them each time
    private boolean processPath(Path path) throws IOException {
        checkNotNull(hdfsFileSystem, "hdfsFileSystem is not initialized");
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
