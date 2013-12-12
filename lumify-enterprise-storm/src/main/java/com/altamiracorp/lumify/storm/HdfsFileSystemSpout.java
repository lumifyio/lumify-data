package com.altamiracorp.lumify.storm;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.utils.Utils;
import com.altamiracorp.lumify.core.config.ConfigurationHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class HdfsFileSystemSpout extends BaseFileSystemSpout {
    private static final Logger LOGGER = LoggerFactory.getLogger(HdfsFileSystemSpout.class);

    private final String subDir;
    private FileSystem hdfsFileSystem;
    private String readPath;
    private Queue<String> filesToProcess;

    public HdfsFileSystemSpout(String subDir) {
        this.subDir = subDir;
    }

    @Override
    public void open(Map stormConf, TopologyContext context, SpoutOutputCollector collector) {
        super.open(stormConf, context, collector);

        filesToProcess = new LinkedList<String>();

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
            while (filesToProcess.size() > 0) {
                checkState(hdfsFileSystem != null, "hdfsFileSystem is not initialized");

                String path = filesToProcess.remove();
                if (!isInWorkingSet(path) && hdfsFileSystem.exists(new Path(path))) {
                    LOGGER.debug("emitting path: " + path);
                    emit(path);
                    return;
                }
            }

            populateFilesToProcess();
            if (filesToProcess.size() == 0) {
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
            if (hdfsFileSystem.delete(ackedMessagePath, false)) {
                LOGGER.debug("Deleted message path: " + ackedMessagePath);
            } else {
                LOGGER.debug("Could not delete message path: " + ackedMessagePath);
            }
        }

        super.safeAck(msgId);
    }

    private void populateFilesToProcess() throws IOException {
        checkState(hdfsFileSystem != null, "hdfsFileSystem is not initialized");

        Path path = new Path(readPath);
        populateFilesToProcess(path);
    }

    private void populateFilesToProcess(Path path) throws IOException {
        checkState(hdfsFileSystem != null, "hdfsFileSystem is not initialized");

        FileStatus[] files = hdfsFileSystem.listStatus(path);
        for (FileStatus file : files) {
            Path filePath = file.getPath();
            if (file.isDirectory()) {
                populateFilesToProcess(filePath);
            }
            this.filesToProcess.add(filePath.toString());
        }
    }

    @Override
    public long getToBeProcessedCount() {
        return filesToProcess.size();
    }

    @Override
    public String getName() {
        return this.subDir;
    }
}
