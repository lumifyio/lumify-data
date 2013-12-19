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
            String[] tabSeparatedValues = value.toString().split("\t");
            String title = tabSeparatedValues[1];
            String body = tabSeparatedValues[4];

            reporter.setStatus(title + " (" + body.length() + ")");

            Configuration conf = new Configuration();
            FileSystem fs = FileSystem.get(conf);
            Path path = new Path(OUTPUT_PATH + "/" + title.replaceAll("[^A-Za-z0-9]", "_"));
            OutputStream os = fs.create(path);
            os.write(body.replaceAll("\\\\n", "\n").getBytes());
            os.close();
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
