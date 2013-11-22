package com.altamiracorp.lumify.storm.twitter;

import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermRegexFinder;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.core.model.artifact.ArtifactType;
import com.altamiracorp.lumify.core.model.graph.GraphGeoLocation;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.ontology.VertexType;
import com.altamiracorp.lumify.core.model.search.SearchProvider;
import com.altamiracorp.lumify.core.model.termMention.TermMention;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class TwitterStreamingBolt extends BaseLumifyBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterStreamingBolt.class);
    private static final String TWITTER_HANDLE = "twitterHandle";
    private static final String TWEETED_BY = "tweetTweetedByHandle";
    private static final String TWEET_MENTION = "tweetMentionedHandle";
    private static final String TWEET_HASHTAG = "tweetHasHashtag";
    private static final String TWEET_URL = "tweetHasURL";
    private static final String HASHTAG_CONCEPT = "hashtag";
    private static final String URL_CONCEPT = "url";

    private SearchProvider searchProvider;
    private GraphVertex tweet;
    private String text;

    @Override
    public void safeExecute(Tuple tuple) throws Exception {
        final JSONObject json = getJsonFromTuple(tuple);

        //if an actual tweet, process it
        if (json.has("text")) {
            Concept handleConcept = ontologyRepository.getConceptByName(TWITTER_HANDLE, getUser());
            saveToDatabase(json, handleConcept);

            // Indexing
            InputStream in = new ByteArrayInputStream(text.getBytes());
            searchProvider.add(tweet, in);

            // Creating entities for mentions, hashtags, and urls
            createMentionEntities(handleConcept);
            createHashTagEntities();
            createURLEntities();

            workQueueRepository.pushArtifactHighlight(tweet.getId());
        }

        getCollector().ack(tuple);
    }

    private void saveToDatabase(JSONObject json, Concept handleConcept) {
        text = json.getString("text");
        String createdAt = json.has("created_at") ? json.getString("created_at") : null;
        String tweeter = json.getJSONObject("user").getString("screen_name");
        String title = "tweet from : " + tweeter;
        if (createdAt != null) {
            title = title + " " + createdAt;
        }

        ArtifactRowKey build = ArtifactRowKey.build(json.toString().getBytes());
        String rowKey = build.toString();

        ArtifactExtractedInfo artifactExtractedInfo = new ArtifactExtractedInfo();
        artifactExtractedInfo.setText(text);
        artifactExtractedInfo.setRaw(json.toString().getBytes());

        artifactExtractedInfo.setMimeType("text/plain");
        artifactExtractedInfo.setRowKey(rowKey);
        artifactExtractedInfo.setArtifactType(ArtifactType.DOCUMENT.toString());
        artifactExtractedInfo.setTitle(title);

        // Write to accumulo and create graph vertex for artifact
        tweet = saveArtifact(artifactExtractedInfo);

        LOGGER.info("Saving tweet to accumulo and as graph vertex: " + tweet.getId());

        if (createdAt != null) {
            final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
            SimpleDateFormat sf = new SimpleDateFormat(TWITTER);
            sf.setLenient(true);

            Date date = null;
            try {
                date = sf.parse(createdAt);
            } catch (ParseException e) {
                new RuntimeException("Cannot parse " + createdAt);
            }
            artifactExtractedInfo.setDate(date);
            tweet.setProperty(PropertyName.PUBLISHED_DATE, date.getTime());
        } else {
            tweet.setProperty(PropertyName.PUBLISHED_DATE, new Date().getTime());
        }

        if (json.has("coordinates") && !json.get("coordinates").equals(JSONObject.NULL)) {
            JSONArray coordinates = json.getJSONObject("coordinates").getJSONArray("coordinates");
            tweet.setProperty(PropertyName.GEO_LOCATION, new GraphGeoLocation(coordinates.getDouble(1), coordinates.getDouble(0)));
        }

        tweet.setProperty(PropertyName.TITLE, title);
        tweet.setProperty(PropertyName.ROW_KEY, rowKey);
        tweet.setProperty(PropertyName.SOURCE, json.getString("source"));
        graphRepository.commit();

        String tweeterId = createOrUpdateTweeterEntity(handleConcept, tweeter);
        graphRepository.saveRelationship(tweeterId, tweet.getId(), TWEETED_BY, getUser());
    }

    private String createOrUpdateTweeterEntity(Concept handleConcept, String tweeter) {
        GraphVertex tweeterVertex = graphRepository.findVertexByTitleAndType(tweeter, VertexType.ENTITY, getUser());
        if (tweeterVertex == null) {
            tweeterVertex = new InMemoryGraphVertex();
        }
        tweeterVertex.setProperty(PropertyName.TITLE, tweeter);
        tweeterVertex.setProperty(PropertyName.TYPE, VertexType.ENTITY.toString());
        tweeterVertex.setProperty(PropertyName.SUBTYPE, handleConcept.getId());
        graphRepository.save(tweeterVertex, getUser());
        return tweeterVertex.getId();
    }

    private void createMentionEntities(Concept handleConcept) {
        createEntities(handleConcept, "(@(\\w+))", TWEET_MENTION);
    }

    private void createHashTagEntities() {
        Concept hashtagConcept = ontologyRepository.getConceptByName(HASHTAG_CONCEPT, getUser());
        createEntities(hashtagConcept, "(#(\\w+))", TWEET_HASHTAG);
    }

    private void createURLEntities() {
        Concept urlConcept = ontologyRepository.getConceptByName(URL_CONCEPT, getUser());
        createEntities(urlConcept, "((http://[^\\s]+))", TWEET_URL);
    }

    private void createEntities(Concept concept, String regex, String relationshipLabel) {
        GraphVertex conceptVertex = graphRepository.findVertex(concept.getId(), getUser());
        List<TermMention> termMentionList = TermRegexFinder.find(tweet.getId(), conceptVertex, text, regex);
        for (TermMention mention : termMentionList) {
            String sign = mention.getMetadata().getSign();
            GraphVertex vertex = graphRepository.findVertexByTitleAndType(sign, VertexType.ENTITY, getUser());
            if (vertex == null) {
                vertex = new InMemoryGraphVertex();
            }
            vertex.setProperty(PropertyName.TITLE, sign);
            vertex.setProperty(PropertyName.ROW_KEY, mention.getRowKey().toString());
            vertex.setProperty(PropertyName.TYPE, VertexType.ENTITY.toString());
            vertex.setProperty(PropertyName.SUBTYPE, concept.getId());
            graphRepository.save(vertex, getUser());

            mention.getMetadata().setGraphVertexId(vertex.getId());
            termMentionRepository.save(mention, getUser().getModelUserContext());

            graphRepository.saveRelationship(tweet.getId(), vertex.getId(), relationshipLabel, getUser());
        }

    }

    @Inject
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }
}
