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

import java.io.IOException;
import java.util.List;

public class WholeFileInputFormat extends CombineFileInputFormat<MapWritable, Text> {

    /**
     * @Override public List<InputSplit> getSplits (JobContext context) throws IOException {
     * // all the files in input set
     * Path[] paths = FileUtil.stat2Paths(
     * listStatus(context).toArray(new FileStatus[0]));
     * List<InputSplit> splits = new ArrayList<InputSplit>();
     * if (paths.length == 0) {
     * return splits;
     * }
     * <p/>
     * int mappers = context.getConfiguration().getInt("mapred.map.tasks",1);
     * int partition = (int)Math.ceil((double)(paths.length / mappers));
     * <p/>
     * List<List<Path>> pathLists = Lists.partition(Arrays.asList(paths),partition);
     * <p/>
     * for (List<Path> partitionedPaths : pathLists) {
     * for (Path path : partitionedPaths) {
     * FileSystem fs = path.getFileSystem(context.getConfiguration());
     * Path p = fs.makeQualified(path);
     * <p/>
     * }
     * }
     * }
     */


    @Override
    public boolean isSplitable(JobContext context, Path file) {
        return false;
    }

    @Override
    public List<InputSplit> getSplits(JobContext context) throws IOException {
        List<InputSplit> splits = super.getSplits(context);
        try {
            int splitNum = 1;
            for (InputSplit split : splits) {
                CombineFileSplit combineFileSplit = (CombineFileSplit) split;
                StringBuilder splitLogMsg = new StringBuilder("Split: ").append(splitNum).append(", Size: ").append(combineFileSplit.getLength()).append(", Paths: ");
                for (Path path : combineFileSplit.getPaths()) {
                    splitLogMsg.append(path.toString()).append(", ");
                }
                System.out.println(splitLogMsg);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
        return splits;
    }

    @Override
    public RecordReader<MapWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException {
        return new CombineFileRecordReader<MapWritable, Text>((CombineFileSplit) split, context, WholeFileRecordReader.class);
    }
}
