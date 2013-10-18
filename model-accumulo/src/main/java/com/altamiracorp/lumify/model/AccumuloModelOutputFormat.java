package com.altamiracorp.lumify.model;

import com.altamiracorp.lumify.core.config.Configuration;
import com.altamiracorp.lumify.core.model.Row;
import org.apache.accumulo.core.client.mapreduce.AccumuloOutputFormat;
import org.apache.accumulo.core.data.Mutation;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;

import java.io.IOException;

public class AccumuloModelOutputFormat extends OutputFormat<Text, Row> {
    private AccumuloOutputFormat accumuloOutputFormat = new AccumuloOutputFormat();

    public static void init(Job job, Configuration configuration, String tableName) {
        AccumuloOutputFormat.setZooKeeperInstance(job.getConfiguration(), configuration.get(AccumuloSession.INSTANCE_NAME), configuration.get(Configuration.ZK_SERVERS));
        AccumuloOutputFormat.setOutputInfo(job.getConfiguration(), configuration.get(Configuration.MODEL_USER), configuration.get(Configuration.MODEL_PASSWORD).getBytes(), false, tableName);
    }

    @Override
    public RecordWriter<Text, Row> getRecordWriter(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        return new RowRecordWriter(accumuloOutputFormat.getRecordWriter(taskAttemptContext));
    }

    @Override
    public void checkOutputSpecs(JobContext jobContext) throws IOException, InterruptedException {
        accumuloOutputFormat.checkOutputSpecs(jobContext);
    }

    @Override
    public OutputCommitter getOutputCommitter(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        return accumuloOutputFormat.getOutputCommitter(taskAttemptContext);
    }

    private class RowRecordWriter extends RecordWriter<Text, Row> {
        private final RecordWriter<Text, Mutation> recordWriter;

        public RowRecordWriter(RecordWriter<Text, Mutation> recordWriter) {
            this.recordWriter = recordWriter;
        }

        @Override
        public void write(Text text, Row row) throws IOException, InterruptedException {
            Mutation mutation = AccumuloSession.createMutationFromRow(row);
            if (mutation != null) {
                this.recordWriter.write(text, mutation);
            }
        }

        @Override
        public void close(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
            this.recordWriter.close(taskAttemptContext);
        }
    }
}
