package io.lumify.model.bigtablequeue;

import com.altamiracorp.bigtable.model.FlushFlag;
import com.altamiracorp.bigtable.model.ModelSession;
import com.altamiracorp.bigtable.model.user.ModelUserContext;
import io.lumify.core.config.Configurable;
import io.lumify.core.config.Configuration;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.model.workQueue.WorkQueueRepository;
import io.lumify.core.user.User;
import io.lumify.core.util.LumifyLogger;
import io.lumify.core.util.LumifyLoggerFactory;
import io.lumify.model.bigtablequeue.model.QueueItem;
import io.lumify.model.bigtablequeue.model.QueueItemRepository;
import org.securegraph.Graph;
import com.google.inject.Inject;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BigTableWorkQueueRepository extends WorkQueueRepository {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(BigTableWorkQueueRepository.class);
    private final UserRepository userRepository;
    public static final String DEFAULT_TABLE_PREFIX = "atc_accumuloqueue_";
    private final ModelSession modelSession;
    private Map<String, QueueItemRepository> queues = new HashMap<String, QueueItemRepository>();
    private String tablePrefix;
    private User user;

    @Inject
    public BigTableWorkQueueRepository(Graph graph, UserRepository userRepository, ModelSession modelSession) {
        super(graph);
        this.userRepository = userRepository;
        this.modelSession = modelSession;
    }

    @Override
    public void init(Map config) {
        super.init(config);

        this.tablePrefix = getTablePrefix(config);
        if (this.tablePrefix == null) {
            this.tablePrefix = DEFAULT_TABLE_PREFIX;
        }
    }

    public static String getTablePrefix(Map config) {
        return (String) config.get(Configuration.WORK_QUEUE_REPOSITORY + ".tableprefix");
    }

    @Configurable(name = "tableprefix", defaultValue = DEFAULT_TABLE_PREFIX)
    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    @Override
    public Object createSpout(Configuration configuration, String queueName) {
        return new BigtableWorkQueueSpout(configuration, queueName);
    }

    @Override
    public void flush() {
        for (QueueItemRepository queue : queues.values()) {
            queue.flush();
        }
    }

    @Override
    public void format() {
        LOGGER.debug("BEGIN format");
        ModelUserContext ctx = userRepository.getModelUserContext();
        List<String> tableList = this.modelSession.getTableList(ctx);
        for (String tableName : tableList) {
            if (tableName.startsWith(this.tablePrefix)) {
                LOGGER.info("Deleting queue table: " + tableName);
                this.modelSession.deleteTable(tableName, ctx);
            }
        }
        LOGGER.debug("END format");
    }

    @Override
    public void subscribeToBroadcastMessages(BroadcastConsumer broadcastConsumer) {
        LOGGER.error("subscribeToBroadcastMessages not supported");
    }

    @Override
    protected void broadcastJson(JSONObject json) {
        LOGGER.error("broadcastJson not supported");
    }

    @Override
    public void pushOnQueue(String queueName, FlushFlag flushFlag, JSONObject json) {
        String tableName = getTableName(this.tablePrefix, queueName);

        if (this.user == null) {
            this.user = userRepository.getSystemUser();
        }

        QueueItemRepository queue = this.queues.get(queueName);
        if (queue == null) {
            this.modelSession.initializeTable(tableName, this.user.getModelUserContext());
            queue = new QueueItemRepository(this.modelSession, tableName);
            this.queues.put(queueName, queue);
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("push on queue %s: %s", tableName, json.toString(2));
        }
        QueueItem queueItem = createQueueItem(tableName, json);
        queue.save(queueItem, flushFlag);
    }

    public static QueueItem createQueueItem(String queueTableName, JSONObject json, String... extra) {
        return new QueueItem(queueTableName, json, extra);
    }

    public static String getTableName(String tablePrefix, String queueName) {
        return tablePrefix + queueName;
    }
}
