package com.altamiracorp.reddawn.cmdline;

import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.UcdClient;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.util.ToolRunner;

public class FormatUcd extends UcdCommandLineBase {
  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(CachedConfiguration.getInstance(), new FormatUcd(), args);
    if (res != 0) {
      System.exit(res);
    }
  }

  @Override
  protected int run(CommandLine cmd) throws Exception {
    UcdClient<AuthorizationLabel> client = createUcdClient();
    client.deleteTables();
    client.initializeTables();

    client.close();
    return 0;
  }
}
