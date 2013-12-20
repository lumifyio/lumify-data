package com.altamiracorp.lumify.tools;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.lib.NullOutputFormat;

import java.io.IOException;
import java.io.OutputStream;

public class WikipediaExtractionImport {
    private static final String OUTPUT_PATH_KEY = "outputPath";

    public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, NullWritable, NullWritable> {
        private String outputPath;
        private FileSystem fs;

        @Override
        public void configure(JobConf job) {
            try {
                super.configure(job);
                Configuration conf = new Configuration();
                outputPath = job.get(OUTPUT_PATH_KEY);
                fs = FileSystem.get(conf);
            } catch (IOException ex) {
                throw new RuntimeException("Could not configure", ex);
            }
        }

        public void map(LongWritable key, Text value, OutputCollector outputCollector, Reporter reporter) throws IOException {
            try {
                LineData lineData = processLine(value.toString());
                reporter.setStatus(lineData.title + " (" + lineData.body.length() + ")");
                writeFile(lineData);
            } catch (Exception ex) {
                reporter.setStatus("Could not process key: " + key.toString() + ": " + ex.getMessage());
            }
        }

        private void writeFile(LineData lineData) throws IOException {
            Path tempFileName = new Path(outputPath, lineData.filename + "_COPYING_");
            Path outputFileName = new Path(outputPath, lineData.filename);
            OutputStream os = fs.create(tempFileName);
            try {
                os.write(lineData.body.getBytes());
            } finally {
                os.close();
            }

            fs.rename(tempFileName, outputFileName);
        }
    }

    private static LineData processLine(String line) {
        String[] tabSeparatedValues = line.split("\t");
        if (tabSeparatedValues.length < 5) {
            throw new RuntimeException("line has < 5 fields");
        }

        LineData lineData = new LineData();
        lineData.title = tabSeparatedValues[1];
        lineData.body = tabSeparatedValues[4].replaceAll("\\\\n", "\n");
        lineData.filename = lineData.title.replaceAll("[^A-Za-z0-9]", "_");
        return lineData;
    }

    private static class LineData {
        public String title;
        public String body;
        public String filename;
    }

    public static void main(String[] args) throws Exception {
        JobConf conf = new JobConf(WikipediaExtractionImport.class);
        conf.set(OUTPUT_PATH_KEY, args[1]);
        conf.setJobName("wex-import");
        conf.setMapperClass(Map.class);
        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(NullOutputFormat.class);
        conf.setNumReduceTasks(0);
        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        JobClient.runJob(conf);
    }
}
