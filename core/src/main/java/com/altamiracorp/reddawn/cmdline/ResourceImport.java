package com.altamiracorp.reddawn.cmdline;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.resources.ResourceRepository;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.util.ToolRunner;

public class ResourceImport extends RedDawnCommandLineBase {
    private ResourceRepository resourceRepository = new ResourceRepository();

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new ResourceImport(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected void processOptions(CommandLine cmd) throws Exception {
        super.processOptions(cmd);
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        RedDawnSession redDawnSession = createRedDawnSession();
        redDawnSession.getModelSession().initializeTables();

        for (String arg : cmd.getArgs()) {
            System.out.println("Importing: " + arg);
            String rowKey = resourceRepository.importFile(redDawnSession.getModelSession(), arg);
            System.out.println("  rowKey: " + rowKey);
        }

        redDawnSession.close();
        return 0;
    }
}
