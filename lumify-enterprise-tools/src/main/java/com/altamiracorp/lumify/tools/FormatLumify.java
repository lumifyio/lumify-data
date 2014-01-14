package com.altamiracorp.lumify.tools;

import com.altamiracorp.bigtable.model.ModelSession;
import com.altamiracorp.lumify.core.cmdline.CommandLineBase;
import com.altamiracorp.lumify.core.util.ModelUtil;
import com.altamiracorp.securegraph.Graph;
import com.google.inject.Inject;
import org.apache.commons.cli.CommandLine;

public class FormatLumify extends CommandLineBase {
    private ModelSession modelSession;
    private Graph graph;

    public static void main(String[] args) throws Exception {
        int res = new FormatLumify().run(args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        ModelUtil.deleteTables(modelSession, getUser());
        // TODO provide a way to delete the graph and it's search index
        // graph.delete(getUser());
        return 0;
    }

    @Inject
    public void setModelSession(ModelSession modelSession) {
        this.modelSession = modelSession;
    }

    @Inject
    public void setGraph(Graph graph) {
        this.graph = graph;
    }
}
