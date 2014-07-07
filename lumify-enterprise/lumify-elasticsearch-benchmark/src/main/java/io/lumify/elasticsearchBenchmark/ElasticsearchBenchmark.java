package io.lumify.elasticsearchBenchmark;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class ElasticsearchBenchmark {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchBenchmark.class);
    private static final String ELEMENT_TYPE = "benchmarkItem";
    private static final Random RANDOM = new Random(1000);

    @Parameter(names = "--count", description = "Number of items to insert")
    private int count = 10000;

    private int currentCount = 0;

    @Parameter(names = "--bulksize", description = "Number of items to insert in bulk batches. Use 1 to not use bulk api.")
    private int bulkSize = 100;

    @Parameter(names = "--hostname", description = "hostname of elasticsearch node.")
    private String hostname;

    @Parameter(names = "--clustername", description = "clustername of elasticsearch cluster.")
    private String clusterName;

    @Parameter(names = "--indexname", description = "Name of the index to test with.")
    private String indexName = "benchmark";

    @Parameter(names = "--port", description = "port of elasticsearch node.")
    private int port = 9300;

    @Parameter(names = "--usenodeapi", description = "Use the node API and join the elasticsearch cluster.")
    private boolean nodeApi = false;

    @Parameter(names = "--help", help = true, description = "Prints help")
    private boolean help;

    @Parameter(names = "--storesourcedata", description = "Store data in the _source field.")
    private boolean storeSourceData = false;

    @Parameter(names = "--documentsize", description = "The max size of the document in bytes.")
    private int documentSize = 10000;

    private ArrayList<String> words;
    private int numberOfBytesInserted;

    public static void main(String[] args) throws Exception {
        new ElasticsearchBenchmark().run(args);
    }

    private void run(String[] args) throws Exception {
        JCommander jcommander = new JCommander(this, args);
        if (help) {
            jcommander.usage();
            return;
        }

        readWordList();

        ImmutableSettings.Builder settingsBuilder = ImmutableSettings.settingsBuilder();
        if (clusterName != null) {
            settingsBuilder.put("cluster.name", clusterName);
        }
        if (hostname != null) {
            settingsBuilder.put("discovery.zen.ping.unicast.hosts", hostname);
        }
        Settings settings = settingsBuilder.build();

        Client client = createClient(settings);
        ensureIndexIsCreated(client);
        long lastReportingTime = System.currentTimeMillis();
        int nextReportingCount = 0;
        int lastReportingCount = 0;
        long startTime = System.currentTimeMillis();
        double rate;
        while (currentCount < count) {
            if (currentCount >= nextReportingCount) {
                long duration = System.currentTimeMillis() - lastReportingTime;
                if (duration == 0) {
                    rate = 0.0;
                } else {
                    rate = ((double) (currentCount - lastReportingCount)) / ((double) duration) * 1000;
                }
                LOGGER.info(String.format("inserting %d/%d (%.2f docs/s)", currentCount, count, rate));
                lastReportingCount = currentCount;
                nextReportingCount += 1000;
                lastReportingTime = System.currentTimeMillis();
            }
            insertDocuments(client);
        }
        long endTime = System.currentTimeMillis();
        rate = ((double) count) / ((double) (endTime - startTime)) * 1000;
        LOGGER.info("Complete");
        LOGGER.info(String.format("             documents: %,d", count));
        LOGGER.info(String.format("  documents per second: %,.2f", rate));
        LOGGER.info(String.format("    bytes per document: %,.2f", ((double) numberOfBytesInserted) / ((double) count)));

        client.close();
    }

    private void readWordList() throws IOException {
        LOGGER.info("Loading word list");
        words = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/words.txt")));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) {
                    break;
                }
                words.add(line);
            }
        } finally {
            reader.close();
        }
    }

    private void insertDocuments(Client client) throws IOException {
        if (bulkSize == 1) {
            IndexRequest indexRequest = createIndexRequest();
            client.index(indexRequest).actionGet();
            currentCount++;
        } else {
            BulkRequest bulkRequest = new BulkRequest();
            for (int i = 0; i < bulkSize; i++) {
                bulkRequest.add(createIndexRequest());
            }
            client.bulk(bulkRequest).actionGet();
            currentCount += bulkSize;
        }
    }

    private IndexRequest createIndexRequest() throws IOException {
        XContentBuilder jsonBuilder;
        String text = getRandomText();
        numberOfBytesInserted += text.length();
        jsonBuilder = XContentFactory.jsonBuilder()
                .startObject()
                .field("title", text);

        IndexRequest indexRequest = new IndexRequest(indexName, ELEMENT_TYPE);
        indexRequest.source(jsonBuilder);
        return indexRequest;
    }

    private String getRandomText() {
        StringBuilder value = new StringBuilder();
        int valueLength = RANDOM.nextInt(documentSize);
        while (value.length() < valueLength) {
            if (value.length() > 0) {
                value.append(' ');
            }
            value.append(words.get(RANDOM.nextInt(words.size())));
        }
        return value.toString();
    }

    private void ensureIndexIsCreated(Client client) throws IOException {
        LOGGER.info("checking index " + indexName);
        if (!client.admin().indices().prepareExists(indexName).execute().actionGet().isExists()) {
            LOGGER.info("creating index " + indexName);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject(ELEMENT_TYPE)
                    .startObject("_source").field("enabled", storeSourceData).endObject()
                    .endObject()
                    .endObject();
            client.admin().indices().prepareCreate(indexName).addMapping(ELEMENT_TYPE, builder).execute().actionGet();
        } else {
            LOGGER.info("index " + indexName + " already exists");
        }
    }

    private Client createClient(Settings settings) {
        Client client;
        if (nodeApi) {
            LOGGER.info("Connecting to elasticsearch via node API");
            Node node = NodeBuilder.nodeBuilder()
                    .settings(settings)
                    .client(true)
                    .node();
            client = node.client();
        } else {
            LOGGER.info("Connecting to elasticsearch via transport client " + hostname + ":" + port);
            client = new TransportClient(settings);
            for (String host : hostname.split(",")) {
                ((TransportClient) client).addTransportAddress(new InetSocketTransportAddress(host, port));
            }
        }
        return client;
    }
}
