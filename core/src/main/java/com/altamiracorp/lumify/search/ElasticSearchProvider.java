package com.altamiracorp.lumify.search;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.mapreduce.Mapper;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactType;

public class ElasticSearchProvider implements SearchProvider {
    public static final String ES_LOCATIONS_PROP_KEY = "elasticsearch.locations";

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchProvider.class.getName());
    private static final String ES_INDEX = "atc";
    private static final String ES_INDEX_TYPE = "artifact";
    private static final String FIELD_TEXT = "text";
    private static final String FIELD_SUBJECT = "subject";
    private static final String FIELD_PUBLISHED_DATE = "publishedDate";
    private static final String FIELD_GRAPH_VERTEX_ID = "graphVertexId";
    private static final String FIELD_SOURCE = "source";
    private static final String FIELD_ARTIFACT_TYPE = "type";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final int ES_QUERY_MAX_SIZE = 100;

    private static TransportClient client;

    @Override
    public void setup(Properties props) {
        setup(props.getProperty(ES_LOCATIONS_PROP_KEY).split(","));
    }

    @Override
    public void setup(Mapper.Context context) throws Exception {
        setup(context.getConfiguration().getStrings(ES_LOCATIONS_PROP_KEY));
    }

    private void setup(String[] esLocations) {
        if (client != null) {
            return;
        }

        client = new TransportClient();
        for (String esLocation : esLocations) {
            String[] locationSocket = esLocation.split(":");
            client.addTransportAddress(new InetSocketTransportAddress(locationSocket[0], Integer.parseInt(locationSocket[1])));
        }

        initializeIndex();
    }

    @Override
    public void teardown() throws Exception {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    @Override
    public void add(Artifact artifact) throws Exception {
        if (artifact.getContent() == null) {
            return;
        }

        LOGGER.info("Adding artifact \"" + artifact.getRowKey().toString() + "\" to elastic search index.");

        String id = artifact.getRowKey().toString();
        String graphVertexId = artifact.getGenericMetadata().getGraphVertexId();
        String source = artifact.getGenericMetadata().getSource();
        String text = artifact.getContent().getDocExtractedTextString();
        text = text == null ? "" : text;
        String subject = artifact.getGenericMetadata().getSubject();
        subject = subject == null ? "" : subject;

        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()
                .startObject()
                .field(FIELD_TEXT, text)
                .field(FIELD_SUBJECT, subject)
                .field(FIELD_PUBLISHED_DATE, artifact.getPublishedDate())
                .field(FIELD_ARTIFACT_TYPE, artifact.getType().toString());

        if (graphVertexId != null) {
            jsonBuilder = jsonBuilder.field(FIELD_GRAPH_VERTEX_ID, graphVertexId);
        }

        if (source != null) {
            jsonBuilder = jsonBuilder.field(FIELD_SOURCE, source);
        }

        IndexResponse response = client.prepareIndex(ES_INDEX, ES_INDEX_TYPE, id)
                .setSource(jsonBuilder.endObject())
                .execute().actionGet();

        if (response.getId() == null) {
            LOGGER.error("Failed to index artifact " + id + " with elastic search");
        }
    }

    @Override
    public Collection<ArtifactSearchResult> searchArtifacts(String query) throws Exception {
        SearchResponse response = client.prepareSearch(ES_INDEX)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setTypes(ES_INDEX_TYPE)
                .setQuery(new QueryStringQueryBuilder(query).defaultField("_all"))
                .setFrom(0)
                .setSize(ES_QUERY_MAX_SIZE)
                .addFields(FIELD_SUBJECT, FIELD_GRAPH_VERTEX_ID, FIELD_SOURCE, FIELD_PUBLISHED_DATE, FIELD_ARTIFACT_TYPE)
                .execute().actionGet();

        SearchHit[] hits = response.getHits().getHits();
        Collection<ArtifactSearchResult> results = new ArrayList<ArtifactSearchResult>(hits.length);
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        for (SearchHit hit : hits) {
            Map<String, SearchHitField> fields = hit.getFields();
            String id = hit.getId();
            String subject = getString(fields, FIELD_SUBJECT);
            String source = getString(fields, FIELD_SOURCE);
            String graphVertexId = getString(fields, FIELD_GRAPH_VERTEX_ID);
            ArtifactType type = ArtifactType.valueOf(getString(fields, FIELD_ARTIFACT_TYPE).toUpperCase());
            Date publishedDate = dateFormat.parse(fields.get(FIELD_PUBLISHED_DATE).getValue().toString());
            ArtifactSearchResult result = new ArtifactSearchResult(id, subject, publishedDate, source, type, graphVertexId);
            results.add(result);
        }

        return results;
    }

    @Override
    public void deleteIndex() {
        DeleteIndexResponse response = client.admin().indices().delete(new DeleteIndexRequest(ES_INDEX)).actionGet();
        if (!response.isAcknowledged()) {
            LOGGER.error("Failed to delete elastic search index named " + ES_INDEX);
        }
    }

    @Override
    public void initializeIndex() {
        try {
            IndicesExistsResponse existsResponse = client.admin().indices().exists(new IndicesExistsRequest(ES_INDEX)).actionGet();
            if (existsResponse.isExists()) {
                LOGGER.info("Elastic search index " + ES_INDEX + " already exists, skipping creation.");
                return;
            }

            JSONObject indexConfig = new JSONObject();
            indexConfig.put("_source", new JSONObject().put("enabled", false));
            JSONObject properties = new JSONObject();
            properties.put(FIELD_TEXT, new JSONObject().put("type", "string").put("store", "no"));
            properties.put(FIELD_SUBJECT, new JSONObject().put("type", "string").put("store", "yes"));
            properties.put(FIELD_GRAPH_VERTEX_ID, new JSONObject().put("type", "string").put("store", "yes"));
            properties.put(FIELD_SOURCE, new JSONObject().put("type", "string").put("store", "yes"));
            properties.put(FIELD_ARTIFACT_TYPE, new JSONObject().put("type", "string").put("store", "yes"));
            properties.put(FIELD_PUBLISHED_DATE, new JSONObject().put("type", "date").put("store", "yes"));
            indexConfig.put("properties", properties);
            JSONObject indexType = new JSONObject();
            indexType.put(ES_INDEX_TYPE, indexConfig);

            CreateIndexRequest request = new CreateIndexRequest(ES_INDEX).mapping(ES_INDEX_TYPE, indexType.toString());
            CreateIndexResponse response = client.admin().indices().create(request).actionGet();

            if (!response.isAcknowledged()) {
                LOGGER.error("Failed to create elastic search index named " + ES_INDEX);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to create Elastic Search index named " + ES_INDEX, e);
        }
    }

    private String getString(Map<String, SearchHitField> fields, String fieldName) {
        SearchHitField field = fields.get(fieldName);
        if (field != null) {
            Object value = field.getValue();
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }
}
