package com.altamiracorp.lumify.cmdline;

import com.altamiracorp.lumify.AppSession;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.util.ToolRunner;

public class FormatUcd extends CommandLineBase {
    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new FormatUcd(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        AppSession session = createSession();
        session.getModelSession().deleteTables(getUser());
        session.getSearchProvider().deleteIndex(getUser());
        session.getGraphSession().deleteSearchIndex(getUser());
        session.close();

        session = createSession();
        session.initialize();

        session.close();
        return 0;
    }
}
