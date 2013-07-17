package com.altamiracorp.reddawn.cmdline;

import com.altamiracorp.reddawn.RedDawnSession;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.util.ToolRunner;

public class FormatUcd extends RedDawnCommandLineBase {
    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new FormatUcd(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        RedDawnSession session = createRedDawnSession();
        session.getModelSession().deleteTables();
        session.getSearchProvider().deleteTables();
        session.getModelSession().initializeTables();
        session.getSearchProvider().initializeTables();

        session.close();
        return 0;
    }
}
