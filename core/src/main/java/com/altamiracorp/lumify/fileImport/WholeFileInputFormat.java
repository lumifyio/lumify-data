package com.altamiracorp.lumify.fileImport;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileInputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileRecordReader;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class WholeFileInputFormat extends CombineFileInputFormat<MapWritable, Text> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WholeFileInputFormat.class);


    @Override
    public boolean isSplitable(JobContext context, Path file) {
        return false;
    }


    @Override
    public List<InputSplit> getSplits(JobContext context) throws IOException {
        LOGGER.warn("Entering getSplits");
        LOGGER.warn("Max split from configuration: " + context.getConfiguration().get("mapreduce.input.fileinputformat.split.maxsize"));
        List<InputSplit> splits = super.getSplits(context);
        try {
            int splitNum = 1;
            if (splits.isEmpty()) {
                LOGGER.warn("Splits is empty!");
            }
            for (InputSplit split : splits) {
                CombineFileSplit combineFileSplit = (CombineFileSplit) split;
                StringBuilder splitLogMsg = new StringBuilder("Split: ").append(splitNum).append(", Size: ").append(combineFileSplit.getLength()).append(", Paths: ");
                for (Path path : combineFileSplit.getPaths()) {
                    splitLogMsg.append(path.toString()).append(", ");
                }
                LOGGER.warn(splitLogMsg.toString());
                splitNum++;
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
        LOGGER.warn("Leaving getSplits");
        return splits;
    }

    @Override
    public RecordReader<MapWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException {
        return new CombineFileRecordReader<MapWritable, Text>((CombineFileSplit) split, context, WholeFileRecordReader.class);
    }
}
