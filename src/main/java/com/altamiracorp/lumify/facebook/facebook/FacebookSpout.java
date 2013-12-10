package com.altamiracorp.lumify.facebook.facebook;


import java.util.Map;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.altamiracorp.lumify.storm.FieldNames;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public class FacebookSpout extends BaseRichSpout {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookSpout.class);
    private Facebook facebook = new FacebookFactory().getInstance();
    private static final String APP_ID = "facebook.appId";
    private static final String APP_SECRET = "facebook.appSecret";
    private JSONArray facebookArray;
    private int i;
    private SpoutOutputCollector collector;


    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(FieldNames.FILE_NAME));
    }

    @Override
    public void open(Map stormConf, TopologyContext context, SpoutOutputCollector collector) {
        checkNotNull(stormConf.get(APP_ID), "'appId' config not set");
        checkNotNull(stormConf.get(APP_SECRET), "'appSecret' config not set");
//        checkNotNull(stormConf.get(QUERY), "'clientSecret' config not set");

        facebook.setOAuthAppId((String) stormConf.get(APP_ID), (String) stormConf.get(APP_SECRET));
        LOGGER.info(String.format("Configuring environment for spout: %s-%d", context.getThisComponentId(), context.getThisTaskId()));
        getTuplesFromQuery();
        this.collector = collector;
    }

    public void getTuplesFromQuery() {
        String locationInfo = "author_uid, timestamp, coords, message, tagged_uids";
        String locationQuery = "SELECT " + locationInfo + " FROM location_post WHERE distance(latitude, longitude, '38.0335370', '-78.5102940') < 1000";

        try {
            JSONArray postArray = facebook.executeFQL(locationQuery);
            facebookArray = postArray;
            for (int i = 0; i < postArray.length(); i++) {
                JSONObject post = postArray.getJSONObject(i);
                String userInfo = "birthday, contact_email, current_location, friend_count, first_name, last_name, middle_name, url, sex, username";
                String userQuery = "SELECT " + userInfo + "FROM user WHERE uid = ";
                for (int j = 0; i < post.getJSONArray("tagged_uids").length(); j++) {
                    String taggedQuery = userQuery + post.getJSONArray("tagged_uids").getJSONObject(j).toString();
                    facebookArray.put(facebook.executeFQL(taggedQuery));
                }
                String authorQuery = userQuery + post.get("author_uid");
                facebookArray.put(facebook.executeFQL(authorQuery));
            }
        } catch (FacebookException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void nextTuple() {
        try {
            if (i < facebookArray.length()) {
                collector.emit(new Values(facebookArray.getJSONObject(i)));
                LOGGER.debug("recieved facebook tuple");
                i++;
            } else {
                LOGGER.debug("no more objects to process");
            }
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


}


