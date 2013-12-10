package com.altamiracorp.lumify.facebook.facebook;



import java.io.InputStream;
import com.altamiracorp.lumify.core.model.search.SearchProvider;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.google.common.base.Joiner;
import com.google.inject.Inject;

public class FacebookBolt extends BaseLumifyBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookBolt.class);
    private static final Joiner FILEPATH_JOINER = Joiner.on('/');
    private String dataDir;
    private SearchProvider searchProvider;

    @Override
    public void safeExecute(Tuple input) throws Exception {
        JSONObject json = getJsonFromTuple(input);
        String tupleName = calculateTupleType(json);
    }

    private String calculateTupleType(JSONObject jsonObject) throws Exception {
        String name;
        if (jsonObject.has("author_uid")) {
            name = jsonObject.getString("author_uid");
            InputStream in = openFile(name);
            LOGGER.info("Facebook tuple is a post. Writing: %s to accumulo", name);
            processPost(jsonObject);
        } else {
            name = jsonObject.getString("username");
            LOGGER.info("Facebook tuple is a user. Creating entity for: %s", name);
            processUser(jsonObject);
        }
        return name;
    }

    private void processUser(JSONObject user) {
        //create entity for each user with the properties if one doesn't already exist

        //add properties to the entities
    }

    private void processPost(JSONObject post) {
        //write artifact with extracted info to accumulo

        //create entity for the artifact

        //create entities for each of the ids tagged or author and the relationships

    }

    @Inject
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }
}