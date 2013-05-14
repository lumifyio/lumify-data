package com.altamiracorp.reddawn.ingest.storm.bolts;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.altamiracorp.reddawn.ingest.storm.spouts.FileReaderSpout;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.models.Artifact;
import org.apache.accumulo.core.client.*;

import java.util.Date;
import java.util.Map;

public class FileDataToUcdArtifactBolt extends BaseRichBolt {
  public static final String FIELD_ROW_ID = "rowId";
  private final String instanceName;
  private final String zooServers;
  private final String userName;
  private final byte[] password;
  private OutputCollector outputCollector;
  private BatchWriter writer;
  private long memBuf = 1000000L; // bytes to store before sending a batch
  private long timeout = 1000L; // milliseconds to wait before sending
  private int numThreads = 10;

  public FileDataToUcdArtifactBolt(String instanceName, String zooServers, String userName, byte[] password) {
    this.instanceName = instanceName;
    this.zooServers = zooServers;
    this.userName = userName;
    this.password = password;
  }

  @Override
  public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
    this.outputCollector = outputCollector;

    try {
      ZooKeeperInstance zooKeeperInstance = new ZooKeeperInstance(instanceName, zooServers);
      Connector connection = zooKeeperInstance.getConnector(userName, password);
      UcdClient client = new UcdClient(connection);
      client.initializeTables();
      this.writer = client.getAccumuloConnector().createBatchWriter(Artifact.TABLE_NAME, memBuf, timeout, numThreads);
    } catch (Exception ex) {
      outputCollector.reportError(ex);
    }
  }

  @Override
  public void execute(Tuple input) {
    try {
      if (this.writer == null) {
        return;
      }

      String fileName = input.getStringByField(FileReaderSpout.FIELD_FILE_NAME);
      Date timestamp = new Date(input.getLongByField(FileReaderSpout.FIELD_TIMESTAMP));
      byte[] data = input.getBinaryByField(FileReaderSpout.FIELD_DATA);
      String extractedText = input.getStringByField(TextExtractorBoltBase.FIELD_EXTRACTED_TEXT);

      Artifact artifact = new Artifact();
      artifact.setFullFileName(fileName);
      artifact.setDocumentDateTime(timestamp);
      artifact.setData(data);
      artifact.setExtractedText(extractedText);
      String rowId = artifact.getRowId();
      this.writer.addMutation(artifact.getMutation());
      this.writer.flush();

      this.outputCollector.emit(new Values(rowId));
      this.outputCollector.ack(input);
    } catch (Throwable ex) {
      this.outputCollector.reportError(ex);
    }
  }

  @Override
  public void cleanup() {
    try {
      this.writer.close();
    } catch (MutationsRejectedException ex) {
      this.outputCollector.reportError(ex);
    }
    super.cleanup();
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    outputFieldsDeclarer.declare(new Fields(FIELD_ROW_ID));
  }
}
