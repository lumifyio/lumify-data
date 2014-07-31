package io.lumify.tools.format;

import com.altamiracorp.bigtable.model.ModelSession;
import io.lumify.core.cmdline.CommandLineBase;
import io.lumify.core.model.user.AuthorizationRepository;
import io.lumify.core.util.LumifyLogger;
import io.lumify.core.util.LumifyLoggerFactory;
import io.lumify.core.util.ModelUtil;
import com.google.inject.Inject;
import org.apache.commons.cli.CommandLine;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.securegraph.GraphConfiguration;
import org.securegraph.elasticsearch.ElasticSearchSearchIndexBase;

public class FormatLumify extends CommandLineBase {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(FormatLumify.class);
    private ModelSession modelSession;
    private AuthorizationRepository authorizationRepository;

    public static void main(String[] args) throws Exception {
        int res = new FormatLumify().run(args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        ModelUtil.deleteTables(modelSession, getUser());
        getWorkQueueRepository().format();
        // TODO provide a way to delete the graph and it's search index
        // graph.delete(getUser());

        LOGGER.debug("BEGIN remove all authorizations");
        for (String auth : authorizationRepository.getGraphAuthorizations()) {
            LOGGER.debug("removing auth %s", auth);
            authorizationRepository.removeAuthorizationFromGraph(auth);
        }
        LOGGER.debug("END remove all authorizations");

        getGraph().shutdown();

        // TODO refactor to pull graph. from some static reference
        String indexName = getConfiguration().get("graph." + GraphConfiguration.SEARCH_INDEX_PROP_PREFIX + "." + ElasticSearchSearchIndexBase.CONFIG_INDEX_NAME);
        String[] esLocations = getConfiguration().get("graph." + GraphConfiguration.SEARCH_INDEX_PROP_PREFIX + "." + ElasticSearchSearchIndexBase.CONFIG_ES_LOCATIONS).split(",");
        LOGGER.debug("BEGIN deleting elastic search index: " + indexName);
        TransportClient client = new TransportClient();
        for (String esLocation : esLocations) {
            String[] locationSocket = esLocation.split(":");
            String host = locationSocket[0];
            String port = locationSocket.length > 1 ? locationSocket[1] : "9300";
            client.addTransportAddress(new InetSocketTransportAddress(host, Integer.parseInt(port)));
        }
        DeleteIndexResponse response = client.admin().indices().delete(new DeleteIndexRequest(indexName)).actionGet();
        if (!response.isAcknowledged()) {
            LOGGER.error("Failed to delete elastic search index named %s", indexName);
        }
        LOGGER.debug("END deleting elastic search index: " + indexName);
        client.close();

        return 0;
    }

    @Inject
    public void setModelSession(ModelSession modelSession) {
        this.modelSession = modelSession;
    }

    @Inject
    public void setAuthorizationRepository(AuthorizationRepository authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }
}
