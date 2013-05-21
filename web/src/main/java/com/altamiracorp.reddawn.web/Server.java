package com.altamiracorp.reddawn.web;

import com.altamiracorp.reddawn.cmdline.UcdCommandLineBase;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.util.ToolRunner;
import org.mortbay.jetty.servlet.ServletHandler;

public class Server extends UcdCommandLineBase {

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new Server(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        // TODO refactor this
        WebUcdClientFactory.setUcdCommandLineBase(this);
        WebUcdClientFactory.createUcdClient().initializeTables();

        org.mortbay.jetty.Server server = new org.mortbay.jetty.Server(8080);
        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(App.class, "/*");
        server.setHandler(handler);
        server.start();
        server.join();

        return 0;
    }
}
