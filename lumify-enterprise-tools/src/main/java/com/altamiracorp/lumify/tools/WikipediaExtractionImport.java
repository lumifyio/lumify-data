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

    public static String OUTPUT_PATH = "/lumify/data/unknown";

    public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, NullWritable, NullWritable> {

        public void map(LongWritable key, Text value, OutputCollector outputCollector, Reporter reporter) throws IOException {
            Configuration conf = new Configuration();
            FileSystem fs = FileSystem.get(conf);

            String[] tabSeparatedValues = value.toString().split("\t");
            if (tabSeparatedValues.length >= 5) {
                String title = tabSeparatedValues[1];
                String body = tabSeparatedValues[4];

                reporter.setStatus(title + " (" + body.length() + ")");

                String filename = title.replaceAll("[^A-Za-z0-9]", "_");
                Path tempPath = new Path("./" + filename);
                Path ingestPath = new Path(OUTPUT_PATH + "/" + filename);

                OutputStream os = fs.create(tempPath);
                os.write(body.replaceAll("\\\\n", "\n").getBytes());
                os.close();

                fs.rename(tempPath, ingestPath);
            } else {
                reporter.setStatus("key " + key.toString() + " has < 5 fields");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        JobConf conf = new JobConf(WikipediaExtractionImport.class);
        conf.setJobName("wex-import");
        conf.setMapperClass(Map.class);
        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(NullOutputFormat.class);
        conf.setNumReduceTasks(0);
        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        JobClient.runJob(conf);
    }
}
