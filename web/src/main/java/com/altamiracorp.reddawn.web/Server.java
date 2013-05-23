package com.altamiracorp.reddawn.web;

import com.altamiracorp.reddawn.cmdline.UcdCommandLineBase;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.util.ToolRunner;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.webapp.WebAppContext;

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

        org.mortbay.jetty.Server server = new org.mortbay.jetty.Server();
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setHost("127.0.0.1");
        connector.setPort(8080);
        server.addConnector(connector);

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setResourceBase("./web/src/main/webapp/");

        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(Router.class, "/*");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{webAppContext, servletHandler});

        server.setHandler(handlers);
        server.start();
        server.join();

        return 0;
    }

}
