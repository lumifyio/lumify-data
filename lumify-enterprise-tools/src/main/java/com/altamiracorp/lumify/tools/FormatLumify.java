package com.altamiracorp.lumify.tools;

import com.altamiracorp.bigtable.model.ModelSession;
import com.altamiracorp.lumify.core.cmdline.CommandLineBase;
import com.altamiracorp.lumify.core.model.workQueue.WorkQueueRepository;
import com.altamiracorp.lumify.core.util.ModelUtil;
import com.altamiracorp.securegraph.Graph;
import com.google.inject.Inject;
import org.apache.commons.cli.CommandLine;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class FormatLumify extends CommandLineBase {
    private ModelSession modelSession;
    private Graph graph;
    private WorkQueueRepository workQueueRepository;

    public static void main(String[] args) throws Exception {
        int res = new FormatLumify().run(args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        ModelUtil.deleteTables(modelSession, getUser());
        workQueueRepository.format();
        // TODO provide a way to delete the graph and it's search index
        // graph.delete(getUser());

        // TODO refactor to secure graph
        String ES_INDEX = "securegraph";
        TransportClient client = new TransportClient();
        for (String esLocation : new String[]{"192.168.33.10:9300"}) {
            String[] locationSocket = esLocation.split(":");
            client.addTransportAddress(new InetSocketTransportAddress(locationSocket[0], Integer.parseInt(locationSocket[1])));
        }
        DeleteIndexResponse response = client.admin().indices().delete(new DeleteIndexRequest(ES_INDEX)).actionGet();
        if (!response.isAcknowledged()) {
            LOGGER.error("Failed to delete elastic search index named %s", ES_INDEX);
        }

        return 0;
    }

    @Inject
    public void setModelSession(ModelSession modelSession) {
        this.modelSession = modelSession;
    }

    @Inject
    public void setWorkQueueRepository(WorkQueueRepository workQueueRepository) {
        this.workQueueRepository = workQueueRepository;
    }

    @Inject
    public void setGraph(Graph graph) {
        this.graph = graph;
    }
}
