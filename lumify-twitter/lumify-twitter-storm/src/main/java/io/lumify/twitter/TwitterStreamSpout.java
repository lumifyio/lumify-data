package io.lumify.twitter;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
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
import io.lumify.core.util.LumifyLogger;
import io.lumify.core.util.LumifyLoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public class TwitterStreamSpout extends BaseRichSpout {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(TwitterStreamSpout.class);
    private SpoutOutputCollector collector;
    private final BlockingQueue<String> tweetsToProcess = new LinkedBlockingQueue<String>();

    private static final Pattern TWEET_ID_PATTERN = Pattern.compile("\"id_str\"\\s*:\\s*\"(\\d+)\"");

    private static final String QUERY = "twitter.query";
    private static final String CONSUMER_KEY = "twitter.consumerKey";
    private static final String CONSUMER_SECRET = "twitter.consumerSecret";
    private static final String TOKEN = "twitter.token";
    private static final String TOKEN_SECRET = "twitter.tokenSecret";

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(TweetFileSpout.JSON_OUTPUT_FIELD));
    }

    @Override
    public void open(Map stormConf, TopologyContext context, SpoutOutputCollector collector) {
        String consumerKey = (String) stormConf.get(CONSUMER_KEY);
        String consumerSecret = (String) stormConf.get(CONSUMER_SECRET);
        String token = (String) stormConf.get(TOKEN);
        String tokenSecret = (String) stormConf.get(TOKEN_SECRET);
        String query = (String) stormConf.get(QUERY);

        checkNotNull(consumerKey, "'consumerKey' config not set");
        checkNotNull(consumerSecret, "'consumerSecret' config not set");
        checkNotNull(token, "'token' config not set");
        checkNotNull(tokenSecret, "'tokenSecret' config not set");

        LOGGER.info("Configuring environment for spout: %s-%d", context.getThisComponentId(), context.getThisTaskId());
        this.collector = collector;

        connect(
                consumerKey,
                consumerSecret,
                token,
                tokenSecret,
                query);
    }

    private void connect(String consumerKey, String consumerSecret, String token, String tokenSecret, String query) {
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
        ArrayList<String> terms = getTermsFromQuery(query);
        endpoint.trackTerms(terms);
        Authentication hosebirdAuth = new OAuth1(
                consumerKey,
                consumerSecret,
                token,
                tokenSecret);

        ClientBuilder builder = new ClientBuilder()
                .name("twitter-spout")
                .hosts(hosebirdHosts)
                .endpoint(endpoint)
                .authentication(hosebirdAuth)
                .processor(new StringDelimitedProcessor(tweetsToProcess));

        Client hbc = builder.build();
        hbc.connect();
    }

    private ArrayList<String> getTermsFromQuery(String query) {
        return Lists.newArrayList(query.split(";"));
    }

    @Override
    public void nextTuple() {
        try {
            String tweet = tweetsToProcess.take();
            Matcher m = TWEET_ID_PATTERN.matcher(tweet);
            if (m.find()) {
                String tweetId = m.group(1);
                LOGGER.debug("received tweet to process: %s", tweetId);
                collector.emit(new Values(tweet), tweetId);
            } else {
                LOGGER.warn("Could not parse tweet id from: %s", tweet);
            }
        } catch (InterruptedException e) {
            collector.reportError(e);
        }
    }
}