package com.altamiracorp.lumify.storm.structuredData;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;

import java.util.Map;

public class StructuredDataEntityExtractorBolt extends BaseLumifyBolt {
    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void safeExecute(Tuple input) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
