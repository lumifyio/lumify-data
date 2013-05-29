package com.altamiracorp.reddawn.web;

import com.altamiracorp.reddawn.cmdline.UcdCommandLineBase;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.util.ToolRunner;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

public class Server extends UcdCommandLineBase {

    private int port;

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new Server(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected Options getOptions() {
        Options options = super.getOptions();

        options.addOption(
                OptionBuilder
                        .withArgName("p")
                        .withLongOpt("port")
                        .withDescription("The port to run the server on")
                        .withArgName("port")
                        .hasArg()
                        .create()
        );

        return options;
    }

    @Override
    protected void processOptions(CommandLine cmd) {
        super.processOptions(cmd);

        String port = cmd.getOptionValue("port");
        if (port == null) {
            this.port = 8080;
        } else {
            this.port = Integer.parseInt(port);
        }
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        // TODO refactor this
        WebUcdClientFactory.setUcdCommandLineBase(this);
        WebUcdClientFactory.createUcdClient().initializeTables();

        org.mortbay.jetty.Server server = new org.mortbay.jetty.Server();
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setHost("127.0.0.1");
        connector.setPort(this.port);
        server.addConnector(connector);

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setWar("./web/src/main/webapp/");

        server.addHandler(webAppContext);
        server.start();
        server.join();

        return 0;
    }

}
