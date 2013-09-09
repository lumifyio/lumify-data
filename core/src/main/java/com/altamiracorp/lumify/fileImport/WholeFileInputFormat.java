package com.altamiracorp.lumify.fileImport;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.lib.input.CombineFileInputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileRecordReader;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;

import java.io.IOException;

public class WholeFileInputFormat extends CombineFileInputFormat<MapWritable, BytesWritable> {
    @Override
    public RecordReader<MapWritable, BytesWritable> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException {
        return new CombineFileRecordReader<MapWritable, BytesWritable>((CombineFileSplit)split, context, WholeFileRecordReader.class);
    }
}
