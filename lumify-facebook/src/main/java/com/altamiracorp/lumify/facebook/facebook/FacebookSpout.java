package com.altamiracorp.lumify.facebook.facebook;


import java.util.Iterator;
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
import facebook4j.auth.AccessToken;
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
    private static final String ACCESS_TOKEN = "facebook.accessToken";
    private static final String USER_PERMISSION = "facebook.userPermissions";
    private static final String LOCATION_PERMISSION = "facebook.locationPermissions";
    private static final String LATITUDE = "facebook.latitude";
    private static final String LONGITUDE = "facebook.longitude";
    private static final String DISTANCE = "facebook.distance";
    private static final String AUTHOR_UID = "author_uid";
    private static final String TAGGEED_UIDS = "tagged_uids";
    private String locationPermissions;
    private String userPermissions;
    private String latitude;
    private String longitude;
    private String distance;
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
        checkNotNull(stormConf.get(ACCESS_TOKEN), "'accessToken' config not set");
        checkNotNull(stormConf.get(USER_PERMISSION), "'userPermission' config not set");
        checkNotNull(stormConf.get(LOCATION_PERMISSION), "'locationPermission' config not set");
        checkNotNull(stormConf.get(LATITUDE), "'latitude' config not set");
        checkNotNull(stormConf.get(LONGITUDE), "'longitude' config not set");
        checkNotNull(stormConf.get(DISTANCE), "'distance' config not set");
        locationPermissions = (String) stormConf.get(LOCATION_PERMISSION);
        if (locationPermissions.contains(";")) {
            locationPermissions = locationPermissions.replace(";", ",");
        }
        userPermissions = (String) stormConf.get(USER_PERMISSION);
        if (userPermissions.contains(";")) {
            userPermissions = userPermissions.replace(";", ",");
        }
        latitude = (String) stormConf.get(LATITUDE);
        longitude = (String) stormConf.get(LONGITUDE);
        distance = (String) stormConf.get(DISTANCE);

        facebook.setOAuthAppId((String) stormConf.get(APP_ID), (String) stormConf.get(APP_SECRET));
        facebook.setOAuthPermissions(locationPermissions + userPermissions);
//        facebook.setOAuthAccessToken(new AccessToken((String) stormConf.get(ACCESS_TOKEN), null));
        facebook.setOAuthAccessToken(new AccessToken("CAACEdEose0cBABxO0ZBddq0NVdG40qXzOWrKjtY9zZB1pnUs65FUUSFZCIPH9vAPTJ25tZBZB9j4nQesWEFvF9YVw5yoxjt5eZB9qF8H6HYWcRYBUeFFSJ8kndPtF9SQwrCPQNfOPqb5u3cxgaau5z1LJZAdHEzcvlPgFc5g81QhmoiWUfkY29CCARte6bW1msZD"));
        LOGGER.info(String.format("Configuring environment for spout: %s-%d", context.getThisComponentId(), context.getThisTaskId()));
        getTuplesFromQuery();
        this.collector = collector;
    }

    public void getTuplesFromQuery() {
        String locationQuery = "SELECT " + locationPermissions + " FROM location_post WHERE distance(latitude, longitude, '" + latitude + "', '" + longitude + "') <" + distance;
        facebookArray = new JSONArray();
        try {
            JSONArray postArray = facebook.executeFQL(locationQuery);
            for (int i = 0; i < postArray.length(); i++) {
                JSONObject post = postArray.getJSONObject(i);
                String userQuery = "SELECT " + userPermissions + " FROM user WHERE uid = ";
                if (post.get(TAGGEED_UIDS) instanceof JSONObject) {
                    Iterator keys = post.getJSONObject(TAGGEED_UIDS).keys();
                    while (keys.hasNext()) {
                        String taggedQuery = userQuery + keys.next();
                        facebookArray.put(facebook.executeFQL(taggedQuery));
                    }
                }
                String authorQuery = userQuery + post.get(AUTHOR_UID);
                facebookArray.put(post);
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
                collector.emit(new Values(facebookArray.get(i)));
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


