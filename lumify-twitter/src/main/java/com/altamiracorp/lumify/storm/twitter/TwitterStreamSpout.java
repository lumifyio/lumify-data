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

import java.lang.String;
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
    private static final String SEARCH_TERMS    = "twitter";
    private static final List<String> TERMS     = Lists.newArrayList(SEARCH_TERMS);
    private static final String CONSUMER_KEY    = "twitter.consumerKey";
    private static final String CONSUMER_SECRET = "twitter.consumerSecret";
    private static final String TOKEN           = "twitter.token";
    private static final String TOKEN_SECRET    = "twitter.tokenSecret";

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(FieldNames.FILE_NAME));
    }

    @Override
    public void open(Map stormConf, TopologyContext context, SpoutOutputCollector collector) {
        checkNotNull(stormConf.get(CONSUMER_KEY), "'consumerKey' config not set");
        checkNotNull(stormConf.get(CONSUMER_SECRET), "'consumerSecret' config not set");
        checkNotNull(stormConf.get(TOKEN), "'token' config not set");
        checkNotNull(stormConf.get(TOKEN_SECRET), "'tokenSecret' config not set");

        LOGGER.info(String.format("Configuring environment for spout: %s-%d", context.getThisComponentId(), context.getThisTaskId()));
        this.collector = collector;

        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
        endpoint.trackTerms(TERMS);
        Authentication hosebirdAuth = new OAuth1((String) stormConf.get(CONSUMER_KEY),
                (String) stormConf.get(CONSUMER_SECRET),
                (String) stormConf.get(TOKEN),
                (String) stormConf.get(TOKEN_SECRET));

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


