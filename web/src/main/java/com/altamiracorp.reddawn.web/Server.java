package com.altamiracorp.reddawn.web;

import com.altamiracorp.reddawn.cmdline.UcdCommandLineBase;
import com.altamiracorp.reddawn.web.routes.artifact.ArtifactByRowKey;
import com.altamiracorp.reddawn.web.routes.artifact.ArtifactRawByRowKey;
import com.altamiracorp.reddawn.web.routes.artifact.ArtifactTermsByRowKey;
import com.altamiracorp.reddawn.web.routes.artifact.ArtifactTextByRowKey;
import com.altamiracorp.reddawn.web.routes.search.Search;
import com.altamiracorp.reddawn.web.routes.term.TermByRowKey;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.util.ToolRunner;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

import java.io.File;

public class Server extends UcdCommandLineBase {
  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(CachedConfiguration.getInstance(), new Server(), args);
    if (res != 0) {
      System.exit(res);
    }
  }

  @Override
  protected int run(CommandLine cmd) throws Exception {
    final String rootDir = new File("./web/src/main/webapp").toURI().toURL().toString();

    // TODO refactor this
    WebUcdClientFactory.setUcdCommandLineBase(this);
    WebUcdClientFactory.createUcdClient().initializeTables();

    Component component = new Component();
    component.getServers().add(Protocol.HTTP, 8888);
    component.getClients().add(Protocol.FILE);

    Application application = new Application() {
      @Override
      public Restlet createInboundRoot() {
        Router router = new Router(getContext());

        router.attach("/search", Search.class);

        router.attach("/artifacts/{rowKey}/terms", ArtifactTermsByRowKey.class);
        router.attach("/artifacts/{rowKey}/text", ArtifactTextByRowKey.class);
        router.attach("/artifacts/{rowKey}/raw", ArtifactRawByRowKey.class);
        router.attach("/artifacts/{rowKey}", ArtifactByRowKey.class);

        router.attach("/terms/{rowKey}", TermByRowKey.class);

        router.attach("/", new Directory(getContext(), rootDir));
        return router;
      }
    };

    component.getDefaultHost().attach(application);
    component.start();
    return 0;
  }
}
