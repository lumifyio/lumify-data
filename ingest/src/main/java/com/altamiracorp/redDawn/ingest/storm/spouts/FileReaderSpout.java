package com.altamiracorp.redDawn.ingest.storm.spouts;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

public class FileReaderSpout extends BaseRichSpout {
  public static final String FIELD_FILE_NAME = "fileName";
  public static final String FIELD_TIMESTAMP = "timestamp";
  public static final String FIELD_DATA = "data";
  private final File directory;
  private final String pattern;
  private SpoutOutputCollector spoutOutputCollector;
  private Iterator<File> fileIterator;

  public FileReaderSpout(File directory, String pattern) {
    this.directory = directory;
    this.pattern = pattern;
  }

  @Override
  public void activate() {
    IOFileFilter fileFilter = new WildcardFileFilter(pattern);
    IOFileFilter directoryFilter = TrueFileFilter.INSTANCE;
    this.fileIterator = FileUtils.iterateFiles(directory, fileFilter, directoryFilter);
  }

  @Override
  public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
    this.spoutOutputCollector = spoutOutputCollector;
  }

  @Override
  public void nextTuple() {
    synchronized (fileIterator) {
      try {
        if (fileIterator.hasNext()) {
          File file = fileIterator.next();
          String fileName = file.getName();
          long timestamp = file.lastModified();
          byte[] data = FileUtils.readFileToByteArray(file);
          this.spoutOutputCollector.emit(new Values(fileName, timestamp, data));
        }
      } catch (Throwable ex) {
        this.spoutOutputCollector.reportError(ex);
      }
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    outputFieldsDeclarer.declare(new Fields(FIELD_FILE_NAME, FIELD_TIMESTAMP, FIELD_DATA));
  }
}
