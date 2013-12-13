package com.altamiracorp.lumify.facebook.facebook;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.SystemUser;
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
    private JSONArray facebookPostArray;
    private JSONArray facebookUserArray;
    private int i;
    private int j;
    private SpoutOutputCollector collector;
    protected ArtifactRepository artifactRepository;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(FieldNames.FILE_NAME));
    }

    protected InputStream getInputStream(GraphVertex graphVertex) throws Exception {
        checkNotNull(graphVertex, "graphVertex cannot be null");

        InputStream textIn;
        String artifactRowKey = (String) graphVertex.getProperty(PropertyName.ROW_KEY);
        Artifact artifact = artifactRepository.findByRowKey(artifactRowKey, new SystemUser().getModelUserContext());
        String text = artifact.getMetadata().getText();
        if (text == null) {
            text = "";
        }
        textIn = new ByteArrayInputStream(text.getBytes());
        return textIn;
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
        facebook.setOAuthAccessToken(new AccessToken("CAACEdEose0cBAOwgUetxwBfNXRciSlmcgC0XlIvHzXX7O95Y0CMDY7CatPAmU8g6CvCMtzr5rUolem3v4qRIYcM9fuzdU5HyVFC6zjvJOSF7rW2iWCWh2M36p5265t0gF8mthneJp2vI18OPwS7T0VWKiCn5MYAbN4xjPZC6b9ZCWZBtjGaY7XUjqmqH9UZD"));
        LOGGER.info(String.format("Configuring environment for spout: %s-%d", context.getThisComponentId(), context.getThisTaskId()));
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
                if (post.get(TAGGEED_UIDS) instanceof JSONObject) {
                    Iterator keys = post.getJSONObject(TAGGEED_UIDS).keys();
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public String createJsonObject (JSONObject object) {
        return object.toString();
    }

    public String createJsonObject (JSONArray array) {
        try {
            return array.get(0).toString();
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    @Override
    public void nextTuple() {
        try {
            if (i < facebookPostArray.length()) {
//                org.json.JSONObject object;
                String object;
                if (facebookPostArray.get(i) instanceof JSONObject){
                    object = createJsonObject(facebookPostArray.getJSONObject(i));
                    collector.emit(new Values(object));
                }
                LOGGER.debug("recieved facebook post tuple");
                i++;
            } else if (j < facebookUserArray.length()) {
                String object;
                if (facebookUserArray.get(j) instanceof JSONObject){
                    object = createJsonObject(facebookUserArray.getJSONArray(j));
                    collector.emit(new Values(object));
                }
                LOGGER.debug("recieved facebook user tuple");
                j++;
            } else {
                LOGGER.debug("no more objects to process");
            }
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}


