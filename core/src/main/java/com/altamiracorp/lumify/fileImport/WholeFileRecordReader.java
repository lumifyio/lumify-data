package com.altamiracorp.lumify.fileImport;


import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;
import org.apache.hadoop.mapreduce.RecordReader;

import java.io.IOException;

class WholeFileRecordReader extends RecordReader<MapWritable, Text> {

    private Path file;
    private Configuration conf;
    private MapWritable currentK;
    private Text currentV;
    private boolean processed = false;

    public WholeFileRecordReader(CombineFileSplit split, TaskAttemptContext context, Integer idx) {
        this.file = split.getPath(idx);
        this.conf = context.getConfiguration();
        this.currentK = new MapWritable();
        this.currentV = new Text();
    }

    @Override
    public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
        //do nothing
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        if (!processed) {
            FileSystem fs = file.getFileSystem(conf);
            //set metadata in key
            FileStatus fileInfo = fs.getFileStatus(file);
            this.currentK.put(new Text("name"), new Text(file.getName()));
            this.currentK.put(new Text("length"), new LongWritable(fileInfo.getLen()));
            this.currentK.put(new Text("lastModified"), new LongWritable(fileInfo.getModificationTime()));

            //set value
            this.currentV.set(file.toString());

            processed = true;
            return true;
        }
        return false;
    }

    @Override
    public MapWritable getCurrentKey() throws IOException, InterruptedException {
        return this.currentK;
    }

    @Override
    public Text getCurrentValue() throws IOException, InterruptedException {
        return this.currentV;
    }

    @Override
    public float getProgress() throws IOException {
        return processed ? 1.0f : 0.0f;
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }
}

