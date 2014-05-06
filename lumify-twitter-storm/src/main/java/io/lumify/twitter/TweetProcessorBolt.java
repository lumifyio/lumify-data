package io.lumify.twitter;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import io.lumify.core.bootstrap.InjectHelper;
import io.lumify.core.bootstrap.LumifyBootstrap;
import io.lumify.core.model.ontology.OntologyLumifyProperties;
import io.lumify.core.model.properties.LumifyProperties;
import io.lumify.core.model.properties.RawLumifyProperties;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.model.workQueue.WorkQueueRepository;
import io.lumify.core.user.User;
import org.json.JSONObject;
import org.securegraph.*;
import org.securegraph.property.StreamingPropertyValue;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TweetProcessorBolt extends BaseRichBolt {
    private Graph graph;
    private UserRepository userRepository;
    private User user;
    private Authorizations authorizations;
    private WorkQueueRepository workQueueRepository;
    private Cache<String, Vertex> userVertexCache = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();

    @Override
    public void prepare(Map stormConf, TopologyContext topologyContext, OutputCollector outputCollector) {
        InjectHelper.inject(this, LumifyBootstrap.bootstrapModuleMaker(new io.lumify.core.config.Configuration(stormConf)));
        prepareUser(stormConf);
    }

    @Override
    public void execute(Tuple tuple) {
        String jsonString = tuple.getStringByField(TweetFileSpout.JSON_OUTPUT_FIELD);
        JSONObject json = new JSONObject(jsonString);

        Vertex userVertex = getUserVertex(json.getJSONObject("user"));
        Vertex tweetVertex = createTweetVertex(jsonString, json);
        createTweetedEdge(userVertex, tweetVertex);
    }

    private void createTweetedEdge(Vertex userVertex, Vertex tweetVertex) {
        Visibility visibility = new Visibility("");
        String tweetedEdgeId = userVertex.getId() + "_TWEETED_" + tweetVertex.getId();
        graph.addEdge(tweetedEdgeId, userVertex, tweetVertex, TwitterOntology.EDGE_LABEL_TWEETED, visibility, authorizations);
        graph.flush();
    }

    private Vertex createTweetVertex(String jsonString, JSONObject json) {
        String vertexId = "TWEET_" + json.getLong("id");
        Visibility visibility = new Visibility("");
        VertexBuilder v = this.graph.prepareVertex(vertexId, visibility, authorizations);

        OntologyLumifyProperties.CONCEPT_TYPE.setProperty(v, TwitterOntology.TWEET_CONCEPT_TYPE, visibility);

        StreamingPropertyValue rawValue = new StreamingPropertyValue(new ByteArrayInputStream(jsonString.getBytes()), byte[].class);
        rawValue.searchIndex(false);
        RawLumifyProperties.RAW.setProperty(v, rawValue, visibility);

        String text = json.getString("text");
        StreamingPropertyValue textValue = new StreamingPropertyValue(new ByteArrayInputStream(text.getBytes()), String.class);
        RawLumifyProperties.TEXT.setProperty(v, textValue, visibility);

        String title = json.getJSONObject("user").getString("name") + ":" + text;
        LumifyProperties.TITLE.setProperty(v, title, visibility);

        Vertex tweetVertex = v.save();
        graph.flush();

        workQueueRepository.pushGraphPropertyQueue(tweetVertex, RawLumifyProperties.RAW.getProperty(tweetVertex));
        workQueueRepository.pushGraphPropertyQueue(tweetVertex, RawLumifyProperties.TEXT.getProperty(tweetVertex));

        return tweetVertex;
    }

    private Vertex getUserVertex(JSONObject user) {
        String vertexId = "TWITTER_USER_" + user.getLong("id");

        Vertex userVertex = userVertexCache.getIfPresent(vertexId);
        if (userVertex != null) {
            return userVertex;
        }

        userVertex = graph.getVertex(vertexId, authorizations);
        if (userVertex == null) {
            Visibility visibility = new Visibility("");
            VertexBuilder v = this.graph.prepareVertex(vertexId, visibility, authorizations);

            OntologyLumifyProperties.CONCEPT_TYPE.setProperty(v, TwitterOntology.USER_CONCEPT_TYPE, visibility);

            LumifyProperties.TITLE.setProperty(v, user.getString("name"), visibility);
            TwitterOntology.PROFILE_IMAGE_URL.setProperty(v, user.getString("profile_image_url"), visibility);

            userVertex = v.save();

            graph.flush();

            workQueueRepository.pushGraphPropertyQueue(userVertex, LumifyProperties.TITLE.getProperty(userVertex));
            workQueueRepository.pushGraphPropertyQueue(userVertex, TwitterOntology.PROFILE_IMAGE_URL.getProperty(userVertex));
        }

        userVertexCache.put(vertexId, userVertex);
        return userVertex;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

    private void prepareUser(Map stormConf) {
        this.user = (User) stormConf.get("user");
        if (this.user == null) {
            this.user = this.userRepository.getSystemUser();
        }
        this.authorizations = this.userRepository.getAuthorizations(this.user);
    }

    @Inject
    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    @Inject
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Inject
    public void setWorkQueueRepository(WorkQueueRepository workQueueRepository) {
        this.workQueueRepository = workQueueRepository;
    }
}
