package com.altamiracorp.lumify.facebook;


import java.util.Iterator;
import java.util.Map;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import static com.altamiracorp.lumify.facebook.FacebookConstants.*;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.storm.FieldNames;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.auth.AccessToken;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;
import static com.google.common.base.Preconditions.checkNotNull;

public class FacebookStreamingSpout extends BaseRichSpout {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(FacebookStreamingSpout.class);
    private Facebook facebook = new FacebookFactory().getInstance();
    private String locationPermissions;
    private String userPermissions;
    private String latitude;
    private String longitude;
    private String distance;
    private JSONArray facebookPostArray;
    private JSONArray facebookUserArray;
    private int i;
    private int j;
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
        facebook.setOAuthAccessToken(new AccessToken((String) stormConf.get(ACCESS_TOKEN), null));

        LOGGER.info("Configuring environment for spout: %s-%d", context.getThisComponentId(), context.getThisTaskId());
        getTuplesFromQuery();
        this.collector = collector;
    }

    public void getTuplesFromQuery() {
        String locationQuery = "SELECT " + locationPermissions + " FROM location_post WHERE distance(latitude, longitude, '" + latitude + "', '" + longitude + "') <" + distance;
        facebookPostArray = new JSONArray();
        facebookUserArray = new JSONArray();
        try {
            JSONArray postArray = facebook.executeFQL(locationQuery);
            for (int i = 0; i < postArray.length(); i++) {
                JSONObject post = postArray.getJSONObject(i);
                String userQuery = "SELECT " + userPermissions + " FROM user WHERE uid = ";
                if (post.get(TAGGED_UIDS) instanceof JSONObject) {
                    Iterator keys = post.getJSONObject(TAGGED_UIDS).keys();
                    while (keys.hasNext()) {
                        String taggedQuery = userQuery + keys.next();
                        facebookUserArray.put(facebook.executeFQL(taggedQuery));
                    }
                }
                String authorQuery = userQuery + post.get(AUTHOR_UID);
                facebookPostArray.put(post);
                facebookUserArray.put(facebook.executeFQL(authorQuery));
            }
        } catch (FacebookException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String createJsonObject (JSONObject object) {
        return object.toString();
    }

    public String createJsonObject (JSONArray array) {
        try {
            if (array.length() > 0) {
                return array.get(0).toString();
            }
        } catch (JSONException e) {
            LOGGER.error("Facebook user object is empty");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void nextTuple() {
        try {
            if (i < facebookPostArray.length()) {
                String object;
                if (facebookPostArray.get(i) instanceof JSONObject){
                    object = createJsonObject(facebookPostArray.getJSONObject(i));
                    collector.emit(new Values(object));
                }
                i++;
            } else if (j < facebookUserArray.length()) {
                String object;
                if (facebookUserArray.get(j) instanceof JSONArray){
                    object = createJsonObject(facebookUserArray.getJSONArray(j));
                    if (object != null) {
                        collector.emit(new Values(object));
                    }
                }
                j++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getFacebookPostArray() {
        return facebookPostArray;
    }
    public JSONArray getFacebookUserArray() {
        return facebookUserArray;
    }
}


