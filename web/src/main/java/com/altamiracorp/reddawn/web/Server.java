package com.altamiracorp.reddawn.web;

import java.net.InetSocketAddress;

import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.util.ToolRunner;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;

import com.altamiracorp.reddawn.cmdline.RedDawnCommandLineBase;

public class Server extends RedDawnCommandLineBase {

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
    protected void processOptions(CommandLine cmd) throws Exception {
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
        InetSocketAddress addr = new InetSocketAddress("0.0.0.0", port);
        org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(addr);

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setWar("./web/src/main/webapp/");

        WebAppContext messagingContext = new WebAppContext();
        messagingContext.setContextPath("/messaging");
        messagingContext.setWar("./messaging/src/main/webapp/");

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[]{webAppContext, messagingContext});

        server.setHandler(contexts);
        server.start();
        server.join();

        return 0;
    }
}
