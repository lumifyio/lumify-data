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
import com.altamiracorp.lumify.core.model.workQueue.WorkQueueRepository;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class TwitterStreamingBolt extends BaseLumifyBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterStreamingBolt.class);
    private SearchProvider searchProvider;

    @Override
    public void safeExecute(Tuple tuple) throws Exception {
        JSONObject json = getJsonFromTuple(tuple);

        //if an actual tweet, process it
        if (json.has("text")) {
            Concept handleConcept = ontologyRepository.getConceptByName("twitterHandle", getUser());
            String text = json.getString("text");
            String user = json.getJSONObject("user").getString("screen_name");
            String title = "tweet from : " + user;
            GraphVertex userVertex = graphRepository.findVertexByTitleAndType(user, VertexType.ENTITY, getUser());
            if (userVertex == null) {
                userVertex = new InMemoryGraphVertex();
            }
            userVertex.setProperty(PropertyName.TITLE, user);
            userVertex.setProperty(PropertyName.TYPE, VertexType.ENTITY.toString());
            userVertex.setProperty(PropertyName.SUBTYPE, handleConcept.getId());
            graphRepository.save(userVertex, getUser());

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
            GraphVertex tweet = saveArtifact(artifactExtractedInfo);
            LOGGER.info("Saving tweet to accumulo and as graph vertex: " + tweet.getId());

            tweet.setProperty(PropertyName.TITLE, title);
            if (json.has("created_at")) {
                final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
                SimpleDateFormat sf = new SimpleDateFormat(TWITTER);
                sf.setLenient(true);

                Date date = sf.parse(json.getString("created_at"));
                artifactExtractedInfo.setDate(date);
                tweet.setProperty(PropertyName.PUBLISHED_DATE, date.getTime());
            } else {
                tweet.setProperty(PropertyName.PUBLISHED_DATE, new Date().getTime());
            }
            if (json.has("coordinates") && !json.get("coordinates").equals(JSONObject.NULL)) {
                JSONArray coordinates = json.getJSONObject("coordinates").getJSONArray("coordinates");
                tweet.setProperty(PropertyName.GEO_LOCATION, new GraphGeoLocation(coordinates.getDouble(1), coordinates.getDouble(0)));
            }
            tweet.setProperty(PropertyName.ROW_KEY, rowKey);
            tweet.setProperty(PropertyName.SOURCE, json.getString("source"));
            graphRepository.commit();
            graphRepository.saveRelationship(userVertex.getId(), tweet.getId(), "tweetTweetedByHandle", getUser());

            // Indexing
            InputStream in = new ByteArrayInputStream(text.getBytes());
            searchProvider.add(tweet, in);

            // regex filter for mentions
            GraphVertex conceptVertex = graphRepository.findVertex(handleConcept.getId(), getUser());
            List<TermMention> termMentionList = TermRegexFinder.find(tweet.getId(), conceptVertex, text, "(@(\\w+))");
            for (TermMention mention : termMentionList) {
                String sign = mention.getMetadata().getSign();
                GraphVertex twitterHandler = graphRepository.findVertexByTitleAndType(sign, VertexType.ENTITY, getUser());
                if (twitterHandler == null) {
                    twitterHandler = new InMemoryGraphVertex();
                }
                twitterHandler.setProperty(PropertyName.TITLE, mention.getMetadata().getSign());
                twitterHandler.setProperty(PropertyName.ROW_KEY, mention.getRowKey().toString());
                twitterHandler.setProperty(PropertyName.TYPE, VertexType.ENTITY.toString());
                twitterHandler.setProperty(PropertyName.SUBTYPE, handleConcept.getId());
                graphRepository.save(twitterHandler, getUser());
                mention.getMetadata().setGraphVertexId(twitterHandler.getId());
                termMentionRepository.save(mention, getUser().getModelUserContext());
                graphRepository.saveRelationship(tweet.getId(), twitterHandler.getId(), "tweetMentionedHandle", getUser());
            }

            // regex filter for hashtags
            Concept hashtagConcept = ontologyRepository.getConceptByName("hashtag", getUser());
            conceptVertex = graphRepository.findVertex(hashtagConcept.getId(), getUser());
            termMentionList = TermRegexFinder.find(tweet.getId(), conceptVertex, text, "(#(\\w+))");
            for (TermMention mention : termMentionList) {
                String sign = mention.getMetadata().getSign();
                GraphVertex hashtag = graphRepository.findVertexByTitleAndType(sign, VertexType.ENTITY, getUser());
                if (hashtag == null) {
                    hashtag = new InMemoryGraphVertex();
                }
                hashtag.setProperty(PropertyName.TITLE, mention.getMetadata().getSign());
                hashtag.setProperty(PropertyName.ROW_KEY, mention.getRowKey().toString());
                hashtag.setProperty(PropertyName.TYPE, VertexType.ENTITY.toString());
                hashtag.setProperty(PropertyName.SUBTYPE, hashtagConcept.getId());
                graphRepository.save(hashtag, getUser());
                mention.getMetadata().setGraphVertexId(hashtag.getId());
                termMentionRepository.save(mention, getUser().getModelUserContext());
                graphRepository.saveRelationship(tweet.getId(), hashtag.getId(), "tweetHasHashtag", getUser());
            }

            // Creating url entities
            Concept urlConcept = ontologyRepository.getConceptByName("url", getUser());
            termMentionList = TermRegexFinder.find(tweet.getId(), urlConcept, text, "((http://[^\\s]+))");
            for (TermMention mention : termMentionList) {
                GraphVertex urlVertex = graphRepository.findVertexByTitleAndType(user, VertexType.ENTITY, getUser());
                if (urlVertex == null) {
                    urlVertex = new InMemoryGraphVertex();
                }
                urlVertex.setProperty(PropertyName.TITLE, mention.getMetadata().getSign());
                urlVertex.setProperty(PropertyName.TYPE, VertexType.ENTITY.toString());
                urlVertex.setProperty(PropertyName.SUBTYPE, urlConcept.getId());
                graphRepository.save(urlVertex, getUser());
                mention.getMetadata().setGraphVertexId(urlVertex.getId());
                termMentionRepository.save(mention, getUser().getModelUserContext());
                graphRepository.saveRelationship(tweet.getId(), urlVertex.getId(), "tweetHasURL", getUser());
            }
            workQueueRepository.pushArtifactHighlight(tweet.getId());
        }

        getCollector().ack(tuple);
    }

    @Inject
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }
}
