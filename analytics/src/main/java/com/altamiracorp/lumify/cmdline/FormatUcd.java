package com.altamiracorp.lumify.cmdline;

import com.altamiracorp.lumify.model.ModelSession;
import com.altamiracorp.lumify.model.TitanGraphSession;
import com.altamiracorp.lumify.search.ElasticSearchProvider;
import com.google.inject.Inject;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.util.ToolRunner;

public class FormatUcd extends CommandLineBase {
    private ModelSession modelSession;
    private ElasticSearchProvider searchProvider;
    private TitanGraphSession graphSession;

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new FormatUcd(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        modelSession.deleteTables(getUser());
        searchProvider.deleteIndex(getUser());
        graphSession.deleteSearchIndex(getUser());
        return 0;
    }

    @Inject
    public void setModelSession(ModelSession modelSession) {
        this.modelSession = modelSession;
    }

    @Inject
    public void setSearchProvider(ElasticSearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    @Inject
    public void setGraphSession(TitanGraphSession graphSession) {
        this.graphSession = graphSession;
    }
}
