package com.altamiracorp.lumify.storm;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Map;

public abstract class BaseFileSystemSpout extends BaseRichSpout implements BaseFileSystemSpoutMXBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseFileSystemSpout.class);
    public static final String DATADIR_CONFIG_NAME = "datadir";
    private SpoutOutputCollector collector;
    private Map<String, String> workingFiles;
    private int totalProcessed;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(FieldNames.FILE_NAME));
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        LOGGER.info(String.format("Configuring environment for spout: %s-%d", context.getThisComponentId(), context.getThisTaskId()));
        this.collector = collector;
        workingFiles = Maps.newHashMap();

        try {
            registerJmxBean();
        } catch (Exception ex) {
            LOGGER.error("Could not register JMX bean", ex);
        }
    }

    protected void registerJmxBean() throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
        for (int suffix = 0; ; suffix++) {
            ObjectName beanName = new ObjectName("com.altamiracorp.lumify.storm.spout:type=" + getClass().getName() + "-" + suffix);
            if (beanServer.isRegistered(beanName)) {
                continue;
            }
            beanServer.registerMBean(this, beanName);
            break;
        }
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
            totalProcessed++;
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

    @Override
    public int getWorkingCount() {
        return workingFiles.size();
    }

    @Override
    public int getTotalProcessedCount() {
        return totalProcessed;
    }
}
