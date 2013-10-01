package com.altamiracorp.lumify.storm;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import com.altamiracorp.lumify.FileImporter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.util.*;

public class DevFileSystemSpout extends BaseRichSpout {
    public static final String FILE_NAME_FIELD_NAME = "fileName";
    private final File dataDir;
    private Queue<File> files;
    private SpoutOutputCollector collector;
    private HashMap<String, File> workingFiles;
    private TopologyContext topologyContext;

    public DevFileSystemSpout(File dataDir) {
        this.dataDir = dataDir;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(FILE_NAME_FIELD_NAME));
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector collector) {
        this.topologyContext = topologyContext;
        this.collector = collector;
        this.files = new LinkedList<File>();
        this.workingFiles = new HashMap<String, File>();

        IOFileFilter fileFilter = new WildcardFileFilter("*");
        IOFileFilter directoryFilter = TrueFileFilter.INSTANCE;
        Iterator<File> fileIterator = FileUtils.iterateFiles(this.dataDir, fileFilter, directoryFilter);

        while (fileIterator.hasNext()) {
            try {
                File f = fileIterator.next();
                if (f.isFile() && !isSupportingFile(f)) {
                    this.files.add(f);
                }
            } catch (Exception ex) {
                collector.reportError(ex);
            }
        }
    }

    private boolean isSupportingFile(File f) {
        if (f.getName().endsWith(FileImporter.MAPPING_JSON_FILE_NAME_SUFFIX)) {
            return true;
        }
        if (f.getName().endsWith(FileImporter.YOUTUBE_CC_FILE_NAME_SUFFIX)) {
            return true;
        }
        if (f.getName().endsWith((FileImporter.SRT_CC_FILE_NAME_SUFFIX))) {
            return true;
        }
        return false;
    }

    @Override
    public void nextTuple() {
        if (this.files.size() == 0) {
            Utils.sleep(100);
            return;
        }

        File f = files.remove();
        this.workingFiles.put(f.getAbsolutePath(), f);
        this.collector.emit(new Values(f.getAbsolutePath()), f.getAbsolutePath());
    }

    @Override
    public void ack(Object msgId) {
        messageComplete((String) msgId);
        super.ack(msgId);
    }

    @Override
    public void fail(Object msgId) {
        messageComplete((String) msgId);
        super.fail(msgId);
    }

    private void messageComplete(String msgId) {
        this.workingFiles.remove(msgId);
    }

    private boolean isDone() {
        if (this.files == null || this.workingFiles == null) {
            return false;
        }
        return this.files.size() == 0 && this.workingFiles.size() == 0;
    }
}
