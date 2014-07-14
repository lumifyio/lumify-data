package io.lumify.elasticsearchBenchmark;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsNodes;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsRequest;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.admin.indices.stats.IndexStats;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
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
import java.util.concurrent.ExecutionException;

public class ElasticsearchBenchmark {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchBenchmark.class);
    private static final Logger RESULTS_LOGGER = LoggerFactory.getLogger("io.lumify.lumify-elasticsearch-benchmark-RESULTS");
    private static final String ELEMENT_TYPE = "benchmarkItem";
    private static final Random RANDOM = new Random(1000);

    @Parameter(names = "--count", description = "total number of documents to insert")
    private int count = 10000;

    private int currentCount = 0;

    @Parameter(names = "--bulkcount", description = "number of documents to insert in bulk batches, set to -1 to not use bulk api")
    private int bulkCount = -1;

    @Parameter(names = "--bulksize", description = "target size of the bulk insert (bytes), set to -1 to not use bulk api")
    private int bulkSize = -1;

    @Parameter(names = "--hostname", description = "(comma separated) hostname(s) of elasticsearch node(s)")
    private String hostname;

    @Parameter(names = "--clustername", description = "name of the elasticsearch cluster")
    private String clusterName;

    @Parameter(names = "--indexname", description = "name of the index to test with")
    private String indexName = "benchmark";

    @Parameter(names = "--port", description = "port of the elasticsearch server")
    private int port = 9300;

    @Parameter(names = "--usenodeapi", description = "optionally use the node API and join the elasticsearch cluster")
    private boolean nodeApi = false;

    @Parameter(names = "--help", help = true, description = "display this help")
    private boolean help;

    @Parameter(names = "--storesourcedata", description = "store data in the _source field")
    private boolean storeSourceData = false;

    @Parameter(names = "--maxdocumentsize", description = "maximum size of the test documents (bytes)")
    private int maxDocumentSize = 10000;

    @Parameter(names = "--minimumdocumentqueuedepth", description = "how many documents to have ready to index")
    private int minimumDocumentQueueDepth = 1000;

    @Parameter(names = "--documentqueuecheckinterval", description = "how long to wait before checking if we need to create more documents (ms)")
    private int documentQueueCheckInterval = 10;

    @Parameter(names = "--indexrefreshinterval", description = "http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/indices-update-settings.html#bulk")
    private String indexRefreshInterval;

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

        if (indexRefreshInterval != null) {
            LOGGER.info("setting refresh_interval to {} for index {}...", indexRefreshInterval, indexName);
            ImmutableSettings.Builder indexSettings = ImmutableSettings.settingsBuilder();
            indexSettings.put("refresh_interval", indexRefreshInterval);
            UpdateSettingsRequest updateSettingsRequest = new UpdateSettingsRequest(indexName);
            updateSettingsRequest.settings(indexSettings);
            client.admin().indices().updateSettings(updateSettingsRequest).actionGet();
        }

        long lastReportingTime = System.currentTimeMillis();
        int nextReportingCount = 0;
        int lastReportingCount = 0;
        long startTime = System.currentTimeMillis();
        double rate;
        while (currentCount < count) {
            if (currentCount > 0 && currentCount >= nextReportingCount) {
                long duration = System.currentTimeMillis() - lastReportingTime;
                if (duration == 0) {
                    rate = 0.0;
                } else {
                    rate = ((double) (currentCount - lastReportingCount)) / ((double) duration) * 1000;
                }
                LOGGER.debug(String.format("inserted %d/%d (%.2f docs/s)", currentCount, count, rate));
                lastReportingCount = currentCount;
                nextReportingCount += 1000;
                lastReportingTime = System.currentTimeMillis();
            }
            insertDocuments(client);
        }
        long endTime = System.currentTimeMillis();
        rate = ((double) count) / ((double) (endTime - startTime)) * 1000;
        double avgBytesPerDocument = ((double) numberOfBytesInserted) / ((double) count);
        LOGGER.info("benchmark complete");
        LOGGER.info(String.format("             documents: %,d", count));
        LOGGER.info(String.format("  documents per second: %,.2f", rate));
        LOGGER.info(String.format("avg bytes per document: %,.2f", avgBytesPerDocument));

        RESULTS_LOGGER.info(formatResults(args, client, rate, avgBytesPerDocument));

        client.close();
    }

    private String formatResults(String[] args, Client client, double rate, double avgBytesPerDocument) throws ExecutionException, InterruptedException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("count", count);
        map.put("bulkCount", bulkCount);
        map.put("bulkSize", bulkSize);
        map.put("nodeApi", nodeApi);
        map.put("storeSourceData", storeSourceData);
        map.put("maxDocumentSize", maxDocumentSize);
        map.put("minimumDocumentQueueDepth", minimumDocumentQueueDepth);
        map.put("documentQueueCheckInterval", documentQueueCheckInterval);

        ClusterStatsResponse clusterStatsResponse = client.admin().cluster().clusterStats(new ClusterStatsRequest()).get();
        ClusterStatsNodes.Counts nodeCounts = clusterStatsResponse.getNodesStats().getCounts();
        map.put("cluster master-only nodes", nodeCounts.getMasterOnly());
        map.put("cluster data-only nodes", nodeCounts.getDataOnly());
        map.put("cluster master/data nodes", nodeCounts.getMasterData());
        map.put("cluster client nodes", nodeCounts.getClient());

        client.admin().indices().flush(new FlushRequest()).actionGet();
        IndicesStatsResponse indicesStatsResponse = client.admin().indices().stats(new IndicesStatsRequest()).get();
        IndexStats indexStats = indicesStatsResponse.getIndex(indexName);
        map.put("index document count", indexStats.getPrimaries().getDocs().getCount());
        map.put("index size (bytes)", indexStats.getPrimaries().getStore().getSizeInBytes());

        GetSettingsResponse getSettingsResponse = client.admin().indices().getSettings(new GetSettingsRequest()).get();
        String indexRefreshIntervalSetting = getSettingsResponse.getSetting(indexName, "index.refresh_interval");
        map.put("index.refresh_interval", indexRefreshIntervalSetting != null ? indexRefreshIntervalSetting : "default");

        map.put("index rate", rate);
        map.put("avg bytes/doc", avgBytesPerDocument);

        map.put("command line args", toArgsString(args));

        return toCsvString(map);
    }

    private String toCsvString(Map<String, Object> map) {
        List<Object> values = new ArrayList<Object>();
        values.addAll(map.values());
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < values.size(); i++) {
            if (i != 0) {
                sb.append("\", \"");
            }
            sb.append(values.get(i));
        }
        sb.append("\"");
        return sb.toString();
    }

    private String toArgsString(String[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                sb.append(" ");
            }
            sb.append(array[i]);
        }
        return sb.toString();
    }

    private void startCreateDocumentTextsThread() {
        if (bulkCount != -1 && bulkCount != Integer.MAX_VALUE && bulkCount > minimumDocumentQueueDepth) {
            LOGGER.warn("bulkCount ({}) is larger than minimumDocumentQueueDepth!", bulkCount);
        }
        if (bulkSize != -1 && bulkSize > (maxDocumentSize / 2) * minimumDocumentQueueDepth) {
            LOGGER.warn("bulkSize ({}) is larger than maxDocumentSize/2 * minimumDocumentQueueDepth!", bulkSize);
        }

        Thread createIndexRequestsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        if (documentTexts.size() < minimumDocumentQueueDepth) {
                            synchronized (documentTexts) {
                                documentTexts.add(getRandomText());
                            }
                        } else {
                            Thread.sleep(documentQueueCheckInterval);
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.error("oops!", ex);
                }
            }
        });
        createIndexRequestsThread.setDaemon(true);
        createIndexRequestsThread.start();
    }

    private void readWordList() throws IOException {
        LOGGER.info("loading word list...");
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
            LOGGER.trace("inserting a single document, {} bytes...", text.length());
            client.index(createIndexRequest(text)).actionGet();
            numberOfBytesInserted += text.length();
            currentCount++;
        } else {
            long initialSize = numberOfBytesInserted;
            BulkRequest bulkRequest = new BulkRequest();
            int requestCount = 0;
            long requestSize = 0;
            for (int i = 0; (i == 0) || ((i < bulkCount) && ((numberOfBytesInserted - initialSize) < bulkSize)); i++) {
                String text = getNextDocumentText();
                bulkRequest.add(createIndexRequest(text));
                numberOfBytesInserted += text.length();
                currentCount++;
                requestCount++;
                requestSize += text.length();
            }
            LOGGER.trace("bulk inserting {} documents, {} bytes...", requestCount, requestSize);
            client.bulk(bulkRequest).actionGet();
        }
    }

    private String getNextDocumentText() throws InterruptedException {
        while (documentTexts.size() == 0) {
            LOGGER.error("document queue underflow!");
            Thread.sleep(100);
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
        int valueLength = RANDOM.nextInt(maxDocumentSize);
        while (value.length() < valueLength) {
            if (value.length() > 0) {
                value.append(' ');
            }
            value.append(words.get(RANDOM.nextInt(words.size())));
        }
        return value.toString();
    }

    private void ensureIndexIsCreated(Client client) throws IOException {
        LOGGER.info("checking for index {}...", indexName);
        if (!client.admin().indices().prepareExists(indexName).execute().actionGet().isExists()) {
            LOGGER.info("creating index {}...", indexName);
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject(ELEMENT_TYPE)
                    .startObject("_source").field("enabled", storeSourceData).endObject()
                    .endObject()
                    .endObject();
            client.admin().indices().prepareCreate(indexName).addMapping(ELEMENT_TYPE, builder).execute().actionGet();
            LOGGER.info("index {} created", indexName);
        } else {
            LOGGER.info("index {} already exists", indexName);
        }
    }

    private Client createClient(Settings settings) {
        Client client;
        long startTime = System.currentTimeMillis();
        if (nodeApi) {
            LOGGER.info("connecting to elasticsearch via node API...");
            Node node = NodeBuilder.nodeBuilder()
                    .settings(settings)
                    .client(true)
                    .node();
            client = node.client();
        } else {
            LOGGER.info("connecting to elasticsearch via transport client...");
            client = new TransportClient(settings);
            for (String host : hostname.split(",")) {
                LOGGER.info("adding address {}:{}...", host, port);
                ((TransportClient) client).addTransportAddress(new InetSocketTransportAddress(host, port));
            }
        }
        long stopTime = System.currentTimeMillis();
        LOGGER.info("connected in {} ms", stopTime - startTime);
        return client;
    }
}
