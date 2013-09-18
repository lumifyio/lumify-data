package com.altamiracorp.lumify.fileImport;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileInputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileRecordReader;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;
import org.apache.hadoop.mapreduce.security.TokenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WholeFileInputFormat extends CombineFileInputFormat<MapWritable, Text> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WholeFileInputFormat.class);

    @Override
    public boolean isSplitable(JobContext context, Path file) {
        return false;
    }

    @Override
    public List<FileStatus> listStatus (JobContext context) throws IOException{
        List<FileStatus> result = new ArrayList<FileStatus>();
        Path[] dirs = getInputPaths(context);
        if (dirs.length == 0) {
            throw new IOException("No input paths specified in job");
        }

        // get tokens for all the required FileSystems..
        TokenCache.obtainTokensForNamenodes(context.getCredentials(), dirs,
                context.getConfiguration());

        List<IOException> errors = new ArrayList<IOException>();

        for (int i=0; i < dirs.length; ++i) {
            Path p = dirs[i];
            FileSystem fs = p.getFileSystem(context.getConfiguration());
            FileStatus[] matches = fs.globStatus(p);
            if (matches == null) {
                errors.add(new IOException("Input path does not exist: " + p));
            } else if (matches.length == 0) {
                errors.add(new IOException("Input Pattern " + p + " matches 0 files"));
            } else {
                for (FileStatus globStat: matches) {
                    if (globStat.isDir()) {
                        for(FileStatus stat: fs.listStatus(globStat.getPath())) {
                            //only add it if it isn't empty
                            if (stat.getLen() > 0) {
                                result.add(stat);
                            } else {
                                LOGGER.warn("Input path " + stat.getPath().toString() + " is empty. It will not be included in this job");
                            }
                        }
                    } else {
                        //only add it if it isn't empty
                        if (globStat.getLen() > 0) {
                            result.add(globStat);
                        } else {
                            LOGGER.warn("Input path " + globStat.getPath().toString() + " is empty. It will not be included in this job");
                        }
                    }
                }
            }
        }

        return result;
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
