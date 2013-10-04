package com.altamiracorp.lumify.storm;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.altamiracorp.lumify.FileImporter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseFileSystemSpout extends BaseRichSpout {
    public static final String DATADIR_CONFIG_NAME = "datadir";
    private SpoutOutputCollector collector;
    private HashMap<String, String> workingFiles;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(FieldNames.FILE_NAME));
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
        this.workingFiles = new HashMap<String, String>();
    }

    protected SpoutOutputCollector getCollector() {
        return collector;
    }

    protected boolean isSupportingFile(File f) {
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

    protected boolean isInWorkingSet(String fileName) {
        return this.workingFiles.containsKey(fileName);
    }

    protected void emit(String path) {
        this.workingFiles.put(path, path);
        getCollector().emit(new Values(path), path);
    }

    @Override
    public void ack(Object msgId) {
        this.workingFiles.remove(msgId);
        super.ack(msgId);
    }

    @Override
    public void fail(Object msgId) {
        String path = this.workingFiles.remove(msgId);
        super.fail(msgId);
        emit(path); // TODO: should we retry or move the file in a failed directory.
    }
}
