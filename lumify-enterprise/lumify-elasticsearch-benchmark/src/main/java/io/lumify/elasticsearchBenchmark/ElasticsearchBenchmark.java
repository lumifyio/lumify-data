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
import java.net.InetAddress;
import java.util.*;

public class ElasticsearchBenchmark {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchBenchmark.class);
    private static final Logger RESULTS_LOGGER = LoggerFactory.getLogger("io.lumify.lumify-elasticsearch-benchmark-RESULTS");
    private static final String ELEMENT_TYPE = "benchmarkItem";
    private static final Random RANDOM = new Random(1000);

    @Parameter(names = "--count", description = "Number of items to insert")
    private int count = 10000;

    private int currentCount = 0;

    @Parameter(names = "--bulkcount", description = "Number of items to insert in bulk batches. Use 1 to not use bulk api.")
    private int bulkCount = -1;

    @Parameter(names = "--bulksize", description = "Size of the bulk insert.")
    private int bulkSize = -1;

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
    private long numberOfBytesInserted;

    private final Queue<String> documentTexts = new LinkedList<String>();

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
        settingsBuilder.put("node.data", false);
        settingsBuilder.put("node.name", "benchmark-" + InetAddress.getLocalHost().getHostName() + "-" + UUID.randomUUID().toString());
        if (clusterName != null) {
            settingsBuilder.put("cluster.name", clusterName);
        }
        if (hostname != null) {
            settingsBuilder.put("discovery.zen.ping.multicast.enabled", false);
            settingsBuilder.put("discovery.zen.ping.unicast.enabled", true);
            settingsBuilder.put("discovery.zen.ping.unicast.hosts", hostname);
        }
        Settings settings = settingsBuilder.build();
        LOGGER.info("settings:\n" + settings.toDelimitedString('\n'));

        if (bulkCount != -1 && bulkSize == -1) {
            bulkSize = Integer.MAX_VALUE;
        }
        if (bulkSize != -1 && bulkCount == -1) {
            bulkCount = Integer.MAX_VALUE;
        }

        startCreateDocumentTextsThread();

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
                LOGGER.debug(String.format("inserting %d/%d (%.2f docs/s)", currentCount, count, rate));
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
        LOGGER.info(String.format("avg bytes per document: %,.2f", ((double) numberOfBytesInserted) / ((double) count)));

        RESULTS_LOGGER.info(formatResults(args, rate));

        client.close();
    }

    // TODO: add cluster info
    private String formatResults(String[] args, double rate) {
        List<Object> settings = new ArrayList<Object>();
        settings.add(count);
        settings.add(bulkCount);
        settings.add(bulkSize);
        settings.add(nodeApi);
        settings.add(storeSourceData);
        settings.add(documentSize);

        StringBuilder settingsSb = new StringBuilder("\"");
        for (int i = 0; i < settings.size(); i++) {
            if (i != 0) {
                settingsSb.append("\", \"");
            }
            settingsSb.append(settings.get(i));
        }
        settingsSb.append("\"");

        StringBuilder argsSb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                argsSb.append(' ');
            }
            argsSb.append(args[i]);
        }

        return String.format("RESULTS %s, \"%,.2f\", \"%s\"", settingsSb.toString(), rate, argsSb.toString());
    }

    private void startCreateDocumentTextsThread() {
        Thread createIndexRequestsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (documentTexts.size() < 1000) {
                            synchronized (documentTexts) {
                                documentTexts.add(getRandomText());
                            }
                        } else {
                            Thread.sleep(10);
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.error("oops", ex);
                }
            }
        });
        createIndexRequestsThread.setDaemon(true);
        createIndexRequestsThread.start();
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

    private void insertDocuments(Client client) throws IOException, InterruptedException {
        if (bulkSize == -1 && bulkCount == -1) {
            String text = getNextDocumentText();
            client.index(createIndexRequest(text)).actionGet();
            numberOfBytesInserted += text.length();
            currentCount++;
        } else {
            long initialSize = numberOfBytesInserted;
            BulkRequest bulkRequest = new BulkRequest();
            for (int i = 0; (i == 0) || ((i < bulkCount) && ((numberOfBytesInserted - initialSize) < bulkSize)); i++) {
                String text = getNextDocumentText();
                bulkRequest.add(createIndexRequest(text));
                numberOfBytesInserted += text.length();
                currentCount++;
            }
            client.bulk(bulkRequest).actionGet();
        }
    }

    private String getNextDocumentText() throws InterruptedException {
        while (documentTexts.size() == 0) {
            LOGGER.error("creating index requests is too slow.");
            Thread.sleep(10);
        }
        synchronized (documentTexts) {
            return documentTexts.remove();
        }
    }

    private IndexRequest createIndexRequest(String text) {
        try {
            XContentBuilder jsonBuilder;
            jsonBuilder = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("title", text);

            IndexRequest indexRequest = new IndexRequest(indexName, ELEMENT_TYPE);
            indexRequest.source(jsonBuilder);
            return indexRequest;
        } catch (IOException ex) {
            throw new RuntimeException("could not create index request", ex);
        }
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
