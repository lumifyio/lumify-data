package com.altamiracorp.lumify.storm;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import com.google.common.collect.Maps;

public abstract class BaseFileSystemSpout extends BaseRichSpout {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseFileSystemSpout.class);
    public static final String DATADIR_CONFIG_NAME = "datadir";
    private SpoutOutputCollector collector;
    private Map<String, String> workingFiles;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(FieldNames.FILE_NAME));
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        LOGGER.info(String.format("Configuring environment for spout: %s-%d", context.getThisComponentId(), context.getThisTaskId()));
        this.collector = collector;
        workingFiles = Maps.newHashMap();
    }

    protected SpoutOutputCollector getCollector() {
        return collector;
    }

    protected boolean isInWorkingSet(String fileName) {
        return workingFiles.containsKey(fileName);
    }

    protected String getPathFromMessageId(Object msgId) {
        return workingFiles.get(msgId);
    }

    protected void emit(String path) {
        workingFiles.put(path, path);

        LOGGER.info("Emitting value: " + path);
        collector.emit(new Values(path), path);
    }

    @Override
    public final void ack(Object msgId) {
        try {
            safeAck(msgId);
        } catch (Exception ex) {
            collector.reportError(ex);
        }
    }

    protected void safeAck(Object msgId) throws Exception {
        workingFiles.remove(msgId);
        super.ack(msgId);
    }

    @Override
    public void fail(Object msgId) {
        String path = workingFiles.remove(msgId);

        super.fail(msgId);
        emit(path); // TODO: should we retry or move the file in a failed directory.
    }
}
