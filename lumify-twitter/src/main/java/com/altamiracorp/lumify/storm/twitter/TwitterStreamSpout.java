package com.altamiracorp.lumify.storm.twitter;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.altamiracorp.lumify.storm.FieldNames;
import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.google.common.base.Preconditions.checkNotNull;

public class TwitterStreamSpout extends BaseRichSpout {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterStreamSpout.class);
    private SpoutOutputCollector collector;
    private Client hbc;
    private BlockingQueue<String> tweetsToProcess = new LinkedBlockingQueue<String>();

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(FieldNames.FILE_NAME));
    }

    @Override
    public void open(Map stormConf, TopologyContext context, SpoutOutputCollector collector) {
        checkNotNull(stormConf.get("consumerKey"), "'consumerKey' config not set");
        checkNotNull(stormConf.get("consumerSecret"), "'consumerSecret' config not set");
        checkNotNull(stormConf.get("token"), "'token' config not set");
        checkNotNull(stormConf.get("tokenSecret"), "'tokenSecret' config not set");

        LOGGER.info(String.format("Configuring environment for spout: %s-%d", context.getThisComponentId(), context.getThisTaskId()));
        this.collector = collector;

        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
        List<String> terms = Lists.newArrayList("twitter");
        endpoint.trackTerms(terms);
        Authentication hosebirdAuth = new OAuth1((String) stormConf.get("consumerKey"),
                (String) stormConf.get("consumerSecret"),
                (String) stormConf.get("token"),
                (String) stormConf.get("tokenSecret"));

        ClientBuilder builder = new ClientBuilder()
                .name("twitter-spout")
                .hosts(hosebirdHosts)
                .endpoint(endpoint)
                .authentication(hosebirdAuth)
                .processor(new StringDelimitedProcessor(tweetsToProcess));

        hbc = builder.build();
        hbc.connect();
    }

    @Override
    public void nextTuple() {
        try {
            String tweet = tweetsToProcess.take();
            LOGGER.debug("received tweet to process: " + tweet);
            collector.emit(new Values(tweet));
        } catch (InterruptedException e) {
            collector.reportError(e);
        }
    }


}


