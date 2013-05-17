package com.altamiracorp.reddawn.ucd.outputFormats;

import org.apache.accumulo.core.client.mapreduce.AccumuloOutputFormat;
import org.apache.accumulo.core.data.Mutation;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;

public class UCDOutputFormat extends AccumuloOutputFormat {
  public static void init(Job job, String username, byte[] password, String zookeeperInstanceName, String zookeeperServerNames, String tableName) {
    AccumuloOutputFormat.setZooKeeperInstance(job.getConfiguration(), zookeeperInstanceName, zookeeperServerNames);
    AccumuloOutputFormat.setOutputInfo(job.getConfiguration(), username, password, false, tableName);
  }

  @Override
  public RecordWriter<Text, Mutation> getRecordWriter(TaskAttemptContext attempt) throws IOException {
    return super.getRecordWriter(attempt);
  }
}
