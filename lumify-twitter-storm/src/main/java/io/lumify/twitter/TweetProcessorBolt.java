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
import io.lumify.core.model.termMention.TermMentionModel;
import io.lumify.core.model.termMention.TermMentionRepository;
import io.lumify.core.model.termMention.TermMentionRowKey;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.model.workQueue.WorkQueueRepository;
import io.lumify.core.user.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.securegraph.*;
import org.securegraph.property.StreamingPropertyValue;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TweetProcessorBolt extends BaseRichBolt {
    private Graph graph;
    private UserRepository userRepository;
    private Authorizations authorizations;
    private WorkQueueRepository workQueueRepository;
    private TermMentionRepository termMentionRepository;
    private Cache<String, Vertex> userVertexCache = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();
    private Cache<String, Vertex> urlVertexCache = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();
    private Cache<String, Vertex> hashtagVertexCache = CacheBuilder.newBuilder()
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
        processEntities(tweetVertex, json);

        termMentionRepository.flush();
    }

    private Vertex createTweetVertex(String jsonString, JSONObject json) {
        String vertexId = "TWEET_" + json.getLong("id");
        Visibility visibility = new Visibility("");
        VertexBuilder v = this.graph.prepareVertex(vertexId, visibility, authorizations);

        OntologyLumifyProperties.CONCEPT_TYPE.setProperty(v, TwitterOntology.CONCEPT_TYPE_TWEET, visibility);

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

    private Vertex getUserVertex(JSONObject userJson) {
        String vertexId = "TWITTER_USER_" + userJson.getLong("id");

        Vertex userVertex = userVertexCache.getIfPresent(vertexId);
        if (userVertex != null) {
            return userVertex;
        }

        userVertex = graph.getVertex(vertexId, authorizations);
        if (userVertex == null) {
            Visibility visibility = new Visibility("");
            VertexBuilder v = this.graph.prepareVertex(vertexId, visibility, authorizations);

            OntologyLumifyProperties.CONCEPT_TYPE.setProperty(v, TwitterOntology.CONCEPT_TYPE_USER, visibility);

            LumifyProperties.TITLE.setProperty(v, userJson.getString("name"), visibility);
            String profileImageUrl = userJson.optString("profile_image_url");
            if (profileImageUrl != null) {
                TwitterOntology.PROFILE_IMAGE_URL.setProperty(v, profileImageUrl, visibility);
            }
            TwitterOntology.SCREEN_NAME.setProperty(v, userJson.getString("screen_name"), visibility);

            userVertex = v.save();

            graph.flush();

            workQueueRepository.pushGraphPropertyQueue(userVertex, LumifyProperties.TITLE.getProperty(userVertex));
            if (profileImageUrl != null) {
                workQueueRepository.pushGraphPropertyQueue(userVertex, TwitterOntology.PROFILE_IMAGE_URL.getProperty(userVertex));
            }
            workQueueRepository.pushGraphPropertyQueue(userVertex, TwitterOntology.SCREEN_NAME.getProperty(userVertex));
        }

        userVertexCache.put(vertexId, userVertex);
        return userVertex;
    }

    private void processEntities(Vertex tweetVertex, JSONObject json) {
        JSONObject entitiesJson = json.optJSONObject("entities");
        if (entitiesJson == null) {
            return;
        }

        JSONArray hashtagsJson = entitiesJson.optJSONArray("hashtags");
        if (hashtagsJson != null) {
            processHashtags(tweetVertex, hashtagsJson);
        }

        JSONArray urlsJson = entitiesJson.optJSONArray("urls");
        if (urlsJson != null) {
            processUrls(tweetVertex, urlsJson);
        }

        JSONArray userMentionsJson = entitiesJson.optJSONArray("user_mentions");
        if (userMentionsJson != null) {
            processUserMentions(tweetVertex, userMentionsJson);
        }
    }

    private void processUserMentions(Vertex tweetVertex, JSONArray userMentionsJson) {
        for (int i = 0; i < userMentionsJson.length(); i++) {
            processUserMention(tweetVertex, userMentionsJson.getJSONObject(i));
        }
    }

    private void processUserMention(Vertex tweetVertex, JSONObject userMentionJson) {
        Vertex userVertex = getUserVertex(userMentionJson);
        Edge edge = createMentionedEdge(tweetVertex, userVertex);

        JSONArray offsets = userMentionJson.getJSONArray("indices");
        createTermMention(tweetVertex, userVertex, edge, TwitterOntology.CONCEPT_TYPE_HASHTAG, offsets);
    }

    private Edge createMentionedEdge(Vertex tweetVertex, Vertex userVertex) {
        Visibility visibility = new Visibility("");
        String mentionedEdgeId = tweetVertex.getId() + "_MENTIONED_" + userVertex.getId();
        Edge edge = graph.addEdge(mentionedEdgeId, tweetVertex, userVertex, TwitterOntology.EDGE_LABEL_MENTIONED, visibility, authorizations);
        graph.flush();
        return edge;
    }

    private void processUrls(Vertex tweetVertex, JSONArray urlsJson) {
        for (int i = 0; i < urlsJson.length(); i++) {
            processUrl(tweetVertex, urlsJson.getJSONObject(i));
        }
    }

    private void processUrl(Vertex tweetVertex, JSONObject urlJson) {
        Vertex urlVertex = getUrlVertex(urlJson);
        Edge edge = createReferencesUrlEdge(tweetVertex, urlVertex);

        JSONArray offsets = urlJson.getJSONArray("indices");
        createTermMention(tweetVertex, urlVertex, edge, TwitterOntology.CONCEPT_TYPE_HASHTAG, offsets);
    }

    private Vertex getUrlVertex(JSONObject urlJson) {
        String url = urlJson.optString("expanded_url");
        if (url == null) {
            url = urlJson.getString("url");
        }

        String vertexId = "TWITTER_URL_" + url;

        Vertex urlVertex = urlVertexCache.getIfPresent(vertexId);
        if (urlVertex != null) {
            return urlVertex;
        }

        urlVertex = graph.getVertex(vertexId, authorizations);
        if (urlVertex == null) {
            Visibility visibility = new Visibility("");
            VertexBuilder v = this.graph.prepareVertex(vertexId, visibility, authorizations);

            OntologyLumifyProperties.CONCEPT_TYPE.setProperty(v, TwitterOntology.CONCEPT_TYPE_URL, visibility);

            LumifyProperties.TITLE.setProperty(v, url, visibility);

            urlVertex = v.save();

            graph.flush();

            workQueueRepository.pushGraphPropertyQueue(urlVertex, LumifyProperties.TITLE.getProperty(urlVertex));
        }

        urlVertexCache.put(vertexId, urlVertex);
        return urlVertex;
    }

    private Edge createReferencesUrlEdge(Vertex tweetVertex, Vertex urlVertex) {
        Visibility visibility = new Visibility("");
        String mentionedEdgeId = tweetVertex.getId() + "_REFURL_" + urlVertex.getId();
        Edge edge = graph.addEdge(mentionedEdgeId, tweetVertex, urlVertex, TwitterOntology.EDGE_LABEL_REFERENCED_URL, visibility, authorizations);
        graph.flush();
        return edge;
    }

    private void processHashtags(Vertex tweetVertex, JSONArray hashtagsJson) {
        for (int i = 0; i < hashtagsJson.length(); i++) {
            processHashtag(tweetVertex, hashtagsJson.getJSONObject(i));
        }
    }

    private void processHashtag(Vertex tweetVertex, JSONObject hashtagJson) {
        Vertex hashtagVertex = getHashtagVertex(hashtagJson);
        Edge edge = createTaggedEdge(tweetVertex, hashtagVertex);

        JSONArray offsets = hashtagJson.getJSONArray("indices");
        createTermMention(tweetVertex, hashtagVertex, edge, TwitterOntology.CONCEPT_TYPE_HASHTAG, offsets);
    }

    private Vertex getHashtagVertex(JSONObject hashtagJson) {
        String text = hashtagJson.optString("text");

        String vertexId = "TWITTER_HASHTAG_" + text;

        Vertex hashtagVertex = hashtagVertexCache.getIfPresent(vertexId);
        if (hashtagVertex != null) {
            return hashtagVertex;
        }

        hashtagVertex = graph.getVertex(vertexId, authorizations);
        if (hashtagVertex == null) {
            Visibility visibility = new Visibility("");
            VertexBuilder v = this.graph.prepareVertex(vertexId, visibility, authorizations);

            OntologyLumifyProperties.CONCEPT_TYPE.setProperty(v, TwitterOntology.CONCEPT_TYPE_HASHTAG, visibility);

            LumifyProperties.TITLE.setProperty(v, text, visibility);

            hashtagVertex = v.save();

            graph.flush();

            workQueueRepository.pushGraphPropertyQueue(hashtagVertex, LumifyProperties.TITLE.getProperty(hashtagVertex));
        }

        hashtagVertexCache.put(vertexId, hashtagVertex);
        return hashtagVertex;
    }

    private Edge createTaggedEdge(Vertex tweetVertex, Vertex hashtagVertex) {
        Visibility visibility = new Visibility("");
        String mentionedEdgeId = tweetVertex.getId() + "_TAGGED_" + hashtagVertex.getId();
        Edge edge = graph.addEdge(mentionedEdgeId, tweetVertex, hashtagVertex, TwitterOntology.EDGE_LABEL_TAGGED, visibility, authorizations);
        graph.flush();
        return edge;
    }

    private void createTweetedEdge(Vertex userVertex, Vertex tweetVertex) {
        Visibility visibility = new Visibility("");
        String tweetedEdgeId = userVertex.getId() + "_TWEETED_" + tweetVertex.getId();
        graph.addEdge(tweetedEdgeId, userVertex, tweetVertex, TwitterOntology.EDGE_LABEL_TWEETED, visibility, authorizations);
        graph.flush();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }

    private void prepareUser(Map stormConf) {
        User user = (User) stormConf.get("user");
        if (user == null) {
            user = this.userRepository.getSystemUser();
        }
        this.authorizations = this.userRepository.getAuthorizations(user);
    }

    private void createTermMention(Vertex tweetVertex, Vertex vertex, Edge edge, String conceptUri, JSONArray offsets) {
        Visibility visibility = new Visibility("");
        long startOffset = offsets.getInt(0);
        long endOffset = offsets.getInt(1);
        TermMentionRowKey termMentionRowKey = new TermMentionRowKey(tweetVertex.getId().toString(), "", startOffset, endOffset);
        TermMentionModel termMention = new TermMentionModel(termMentionRowKey);
        String title = LumifyProperties.TITLE.getPropertyValue(vertex);
        termMention.getMetadata()
                .setConceptGraphVertexId(conceptUri, visibility)
                .setSign(title, visibility)
                .setVertexId(vertex.getId().toString(), visibility)
                .setEdgeId(edge.getId().toString(), visibility)
                .setOntologyClassUri(conceptUri, visibility);
        termMentionRepository.save(termMention);
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

    @Inject
    public void setTermMentionRepository(TermMentionRepository termMentionRepository) {
        this.termMentionRepository = termMentionRepository;
    }
}
