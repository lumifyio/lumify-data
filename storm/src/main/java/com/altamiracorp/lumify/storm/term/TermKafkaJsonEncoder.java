package com.altamiracorp.lumify.storm.term;

import backtype.storm.tuple.Fields;
import com.altamiracorp.lumify.storm.KafkaJsonEncoder;

public class TermKafkaJsonEncoder extends KafkaJsonEncoder {
    @Override
    public Fields getOutputFields() {
        return new Fields("json", "artifactGraphVertexId");
    }
}
