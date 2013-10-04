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

import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class HdfsFileSystemSpout extends BaseFileSystemSpout {
    private FileSystem hdfsFileSystem;
    private String rootDataPath;

    @Override
    public void open(Map stormConf, TopologyContext context, SpoutOutputCollector collector) {
        this.rootDataPath = (String) stormConf.get(BaseFileSystemSpout.DATADIR_CONFIG_NAME);
        Configuration conf = ConfigurationHelper.createHadoopConfigurationFromMap(stormConf);
        try {
            String hdfsRootDir = (String) stormConf.get(AccumuloSession.HADOOP_URL);
            hdfsFileSystem = FileSystem.get(new URI(hdfsRootDir), conf, "hadoop");
        } catch (Exception e) {
            collector.reportError(e);
        }
    }

    @Override
    public void nextTuple() {
        try {
            Path path = new Path(rootDataPath);
            if (!processPath(path)) {
                Utils.sleep(100);
            }
        } catch (IOException e) {
            getCollector().reportError(e);
        }
    }

    // TODO: we could speed this up by caching the list of files to work on instead of reading all of them each time
    private boolean processPath(Path path) throws IOException {
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
