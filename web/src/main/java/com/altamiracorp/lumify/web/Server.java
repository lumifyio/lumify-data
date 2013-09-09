package com.altamiracorp.lumify.web;

import java.net.InetSocketAddress;

import com.altamiracorp.lumify.cmdline.CommandLineBase;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.util.ToolRunner;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;

public class Server extends CommandLineBase {

    private static final String PORT_OPTION_VALUE = "port";
    private static final int DEFAULT_SERVER_PORT = 8080;
    private int serverPort;

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new Server(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected Options getOptions() {
        final Options options = new Options();

        options.addOption(
                OptionBuilder
                        .withLongOpt(PORT_OPTION_VALUE)
                        .withDescription("The port to run the server on")
                        .withArgName("port_number")
                        .hasArg()
                        .create()
        );

        return options;
    }

    @Override
    protected void processOptions(CommandLine cmd) throws Exception {
        final String port = cmd.getOptionValue(PORT_OPTION_VALUE);

        if (port == null) {
            serverPort = DEFAULT_SERVER_PORT;
        } else {
            serverPort = Integer.parseInt(port);
        }
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        InetSocketAddress addr = new InetSocketAddress("0.0.0.0", serverPort);
        org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(addr);

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setWar("./web/src/main/webapp/");

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[]{webAppContext});

        server.setHandler(contexts);
        server.start();
        server.join();

        return 0;
    }
}
