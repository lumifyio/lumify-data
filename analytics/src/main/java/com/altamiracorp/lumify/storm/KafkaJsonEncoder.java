package com.altamiracorp.lumify.storm;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import kafka.message.Message;
import kafka.serializer.Encoder;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class KafkaJsonEncoder implements Encoder<JSONObject>, Scheme {
    @Override
    public Message toMessage(JSONObject json) {
        return new Message(json.toString().getBytes());
    }

    @Override
    public List<Object> deserialize(byte[] ser) {
        ArrayList<Object> results = new ArrayList<Object>();
        results.add(new String(ser));
        return results;
    }

    @Override
    public Fields getOutputFields() {
        return new Fields("json");
    }
}
