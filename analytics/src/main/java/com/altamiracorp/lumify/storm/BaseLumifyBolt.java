package com.altamiracorp.lumify.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import com.google.inject.Guice;
import com.google.inject.Injector;
import kafka.javaapi.producer.Producer;
import kafka.javaapi.producer.ProducerData;
import kafka.producer.ProducerConfig;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public abstract class BaseLumifyBolt extends BaseRichBolt {
    private OutputCollector collector;
    private Producer<String, String> kafkaProducer;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        final Injector injector = Guice.createInjector(StormBootstrap.create(stormConf));
        injector.injectMembers(this);

        Properties props = new Properties();
        props.put("zk.connect", stormConf.get("zookeeperServerNames"));
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        ProducerConfig config = new ProducerConfig(props);
        kafkaProducer = new Producer<String, String>(config);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
    }

    @Override
    public final void execute(Tuple input) {
        try {
            safeExecute(input);
        } catch (Exception e) {
            getCollector().reportError(e);
            getCollector().fail(input);
        }
    }

    protected abstract void safeExecute(Tuple input) throws Exception;

    protected void pushOnQueue(String queueName, String value) {
        ProducerData<String, String> data = new ProducerData<String, String>(queueName, value);
        kafkaProducer.send(data);
    }

    protected InputStream openFile(String fileName) throws Exception {
        return new FileInputStream(fileName); // TODO change to use hdfs or file system depending on format
    }

    public OutputCollector getCollector() {
        return collector;
    }
}
