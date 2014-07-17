package io.lumify.model.bigtablequeue;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import com.altamiracorp.bigtable.model.ModelSession;
import com.altamiracorp.bigtable.model.Row;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.google.inject.Inject;
import io.lumify.core.bootstrap.InjectHelper;
import io.lumify.core.bootstrap.LumifyBootstrap;
import io.lumify.core.config.Configuration;
import io.lumify.core.config.ConfigurationLoader;
import io.lumify.core.metrics.MetricsManager;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.user.User;
import io.lumify.core.util.LumifyLogger;
import io.lumify.core.util.LumifyLoggerFactory;
import io.lumify.model.bigtablequeue.model.QueueItem;
import io.lumify.model.bigtablequeue.model.QueueItemRepository;
import io.lumify.model.bigtablequeue.model.QueueItemRowKey;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BigtableWorkQueueSpout extends BaseRichSpout {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(BigtableWorkQueueSpout.class);
    private UserRepository userRepository;
    private final String queueName;
    private final String tablePrefix;
    private ModelSession modelSession;
    private String tableName;
    private User user;
    private QueueItemRepository queueItemRepository;
    private SpoutOutputCollector collector;
    private Map<String, Boolean> workingSet = new HashMap<String, Boolean>();
    private MetricsManager metricsManager;
    private Counter totalProcessedCounter;
    private Counter totalErrorCounter;
    private Iterator<Row> rows;

    public BigtableWorkQueueSpout(Configuration configuration, String queueName) {
        this.queueName = queueName;
        this.tablePrefix = configuration.get(Configuration.WORK_QUEUE_REPOSITORY + ".tableprefix", BigTableWorkQueueRepository.DEFAULT_TABLE_PREFIX);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("json"));
    }

    @Override
    public void open(final Map conf, TopologyContext topologyContext, SpoutOutputCollector collector) {
        Configuration configuration = ConfigurationLoader.load();
        InjectHelper.inject(this, LumifyBootstrap.bootstrapModuleMaker(configuration));

        this.collector = collector;
        this.user = userRepository.getSystemUser();
        this.tableName = BigTableWorkQueueRepository.getTableName(this.tablePrefix, this.queueName);
        this.modelSession.initializeTable(this.tableName, user.getModelUserContext());
        this.queueItemRepository = new QueueItemRepository(this.modelSession, this.tableName);

        String namePrefix = metricsManager.getNamePrefix(this, this.queueName);
        registerMetrics(metricsManager, namePrefix);
    }

    private Row getNextRows() {
        try {
            if (rows != null && !rows.hasNext()) {
                rows = null;
            }
            if (rows == null) {
                this.modelSession.flush();
                rows = this.modelSession.findAll(this.tableName, this.user.getModelUserContext()).iterator();
            }
            if (rows.hasNext()) {
                return rows.next();
            }
            return null;
        } catch (Exception ex) {
            LOGGER.error("Could not get next", ex);
            rows = null;
            return null;
        }
    }

    private void registerMetrics(MetricsManager metricsManager, String namePrefix) {
        totalProcessedCounter = metricsManager.getRegistry().counter(namePrefix + "total-processed");
        totalErrorCounter = metricsManager.getRegistry().counter(namePrefix + "total-errors");
        metricsManager.getRegistry().register(namePrefix + "in-process",
                new Gauge<Integer>() {
                    @Override
                    public Integer getValue() {
                        return workingSet.size();
                    }
                });
    }

    @Override
    public void nextTuple() {
        while (true) {
            try {
                Row row = getNextRows();
                if (row != null) {
                    String rowKeyString = row.getRowKey().toString();
                    if (this.workingSet.containsKey(rowKeyString)) {
                        break;
                    }
                    QueueItem queueItem = this.queueItemRepository.fromRow(row);
                    this.workingSet.put(rowKeyString, true);
                    String jsonString = queueItem.getJson().toString();
                    LOGGER.debug("emit (%s): %s", this.tableName, rowKeyString);
                    this.collector.emit(new Values(jsonString), rowKeyString);
                    return;
                }
            } catch (Exception ex) {
                LOGGER.error("Could not get next tuple (" + this.tableName + ")", ex);
                this.collector.reportError(ex);
                Utils.sleep(10000);
            }
            Utils.sleep(1000);
        }
    }

    @Override
    public void ack(Object msgId) {
        try {
            LOGGER.debug("ack (%s): %s", this.tableName, msgId.toString());
            this.totalProcessedCounter.inc();
            QueueItemRowKey rowKey = new QueueItemRowKey(msgId);
            this.queueItemRepository.delete(rowKey);
            this.workingSet.remove(rowKey.toString());
        } catch (Exception ex) {
            LOGGER.error("Could not ack (" + this.tableName + "): " + msgId, ex);
            this.collector.reportError(ex);
        }
    }

    @Override
    public void fail(Object msgId) {
        try {
            LOGGER.debug("fail (%s): %s", this.tableName, msgId.toString());
            this.totalErrorCounter.inc();
        } catch (Exception ex) {
            LOGGER.error("Could not fail (" + this.tableName + "): " + msgId, ex);
            this.collector.reportError(ex);
        }
    }

    @Inject
    public void setModelSession(ModelSession modelSession) {
        this.modelSession = modelSession;
    }

    @Inject
    public void setMetricsManager(MetricsManager metricsManager) {
        this.metricsManager = metricsManager;
    }

    @Inject
    public void setUserRepository(UserRepository userProvider) {
        this.userRepository = userProvider;
    }
}
