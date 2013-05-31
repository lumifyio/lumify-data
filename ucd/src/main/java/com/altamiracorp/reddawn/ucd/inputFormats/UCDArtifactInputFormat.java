package com.altamiracorp.reddawn.ucd.inputFormats;

import com.altamiracorp.reddawn.ucd.model.Artifact;
import org.apache.accumulo.core.client.RowIterator;
import org.apache.accumulo.core.client.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.core.client.mapreduce.InputFormatBase;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.util.PeekingIterator;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;
import java.util.Map;

public class UCDArtifactInputFormat extends InputFormatBase<Text, Artifact> {
  public static void init(Job job, String username, byte[] password, Authorizations authorizations, String zookeeperInstanceName, String zookeeperServerNames) {
    AccumuloInputFormat.setZooKeeperInstance(job.getConfiguration(), zookeeperInstanceName, zookeeperServerNames);
    AccumuloInputFormat.setInputInfo(job.getConfiguration(), username, password, Artifact.TABLE_NAME, authorizations);
  }

  @Override
  public RecordReader<Text, Artifact> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
    return new RecordReaderBase<Text, Artifact>() {
      RowIterator rowIterator;

      @Override
      public void initialize(InputSplit inSplit, TaskAttemptContext attempt) throws IOException {
        super.initialize(inSplit, attempt);
        this.rowIterator = new RowIterator(scannerIterator);
        this.currentK = new Text();
        this.currentV = null;
      }

      @Override
      public boolean nextKeyValue() throws IOException, InterruptedException {
        if (!rowIterator.hasNext()) {
          return false;
        }
        PeekingIterator<Map.Entry<Key, Value>> it = new PeekingIterator<Map.Entry<Key, Value>>(rowIterator.next());
        this.currentV = Artifact.newBuilder().buildFromRow(it);
        this.numKeysRead = this.rowIterator.getKVCount();
        this.currentKey = new Key(this.currentV.getKey().toString());
        this.currentK = new Text(this.currentKey.getRow());
        return true;
      }
    };
  }
}
