package com.altamiracorp.lumify.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DebugBolt extends BaseRichBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugBolt.class.getName());
    private final String prefix;
    private OutputCollector collector;

    public DebugBolt(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append(": ");
        for (int i = 0; i < input.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(input.getValue(i));
        }
        LOGGER.info(sb.toString());

        collector.ack(input);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }
}