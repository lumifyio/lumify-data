package com.altamiracorp.redDawn.ingest.storm.topologies;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import com.altamiracorp.redDawn.ingest.storm.bolts.FileDataToUcdArtifactBolt;
import com.altamiracorp.redDawn.ingest.storm.bolts.RowIdStdoutPrinterBolt;
import com.altamiracorp.redDawn.ingest.storm.bolts.SimpleTextExtractorBolt;
import com.altamiracorp.redDawn.ingest.storm.spouts.FileReaderSpout;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LocalFileIngestTopology {
  private File directory = new File(System.getProperty("user.dir"));
  private String fileSearchPattern = "*";
  private String instanceName = "reddawn";
  private String zooServers = "192.168.211.130";
  private String userName = "root";
  private byte[] password = "reddawn".getBytes();

  public static void main(String[] args) {
    LocalFileIngestTopology topo = new LocalFileIngestTopology();
    try {
      topo.run();
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private void run() {
    TopologyBuilder builder = new TopologyBuilder();

    builder.setSpout("files", new FileReaderSpout(this.directory, this.fileSearchPattern), 1);

    builder.setBolt("textExtractor", new SimpleTextExtractorBolt(), 2)
        .shuffleGrouping("files");

    builder.setBolt("dataToUcdArtifact", new FileDataToUcdArtifactBolt(instanceName, zooServers, userName, password))
        .shuffleGrouping("textExtractor");

    builder.setBolt("sendRowIds", new RowIdStdoutPrinterBolt())
        .shuffleGrouping("dataToUcdArtifact");

    LocalCluster cluster = new LocalCluster();
    Config conf = new Config();
    cluster.submitTopology("ingest", conf, builder.createTopology());
  }
}
