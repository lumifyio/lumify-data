package com.altamiracorp.lumify.storm;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseFileSystemSpout extends BaseRichSpout {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseFileSystemSpout.class);
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

    protected boolean isInWorkingSet(String fileName) {
        return this.workingFiles.containsKey(fileName);
    }

    protected String getPathFromMessageId(Object msgId) {
        return this.workingFiles.get(msgId);
    }

    protected void emit(String path) {
        this.workingFiles.put(path, path);
        LOGGER.info("emitting value (" + getClass().getName() + "): " + path);
        getCollector().emit(new Values(path), path);
    }

    @Override
    public final void ack(Object msgId) {
        try {
            safeAck(msgId);
        } catch (Exception ex) {
            getCollector().reportError(ex);
        }
    }

    protected void safeAck(Object msgId) throws Exception {
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
