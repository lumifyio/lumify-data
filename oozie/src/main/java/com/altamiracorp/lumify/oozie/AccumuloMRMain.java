package com.altamiracorp.lumify.oozie;

import org.apache.accumulo.core.client.IteratorSetting;
import org.apache.accumulo.core.client.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.core.client.mapreduce.AccumuloOutputFormat;
import org.apache.accumulo.core.iterators.user.TimestampFilter;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.oozie.action.hadoop.MapReduceMain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class AccumuloMRMain extends MapReduceMain {
    private static final String START_TIME = "timestampFilterStartTime";
    private final SimpleDateFormat dateParser = initDateParser();

    private static SimpleDateFormat initDateParser() {
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyyMMddHHmmssz");
        dateParser.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateParser;
    }

    public static void main(String[] args) throws Exception {
        run(AccumuloMRMain.class, args);
    }

    @Override
    protected void run(String[] args) throws Exception {
        System.out.println();
        System.out.println("Oozie Accumulo Map-Reduce action configuration");
        System.out.println("=======================");

        // loading action conf prepared by Oozie
        Configuration actionConf = new Configuration(false);
        actionConf.addResource(new Path("file:///", System.getProperty("oozie.action.conf.xml")));

        logMasking("Map-Reduce job configuration:", new HashSet<String>(), actionConf);

        initAccumuloFormats(actionConf);

        System.out.println("Submitting Oozie action Accumulo Map-Reduce job");
        System.out.println();
        // submitting job
        Job job = submitJobNew(actionConf);

        // propagating job id back to Oozie
        String jobId = job.getJobID().toString();
        Properties props = new Properties();
        props.setProperty("id", jobId);
        File idFile = new File(System.getProperty("oozie.action.newId.properties"));
        OutputStream os = new FileOutputStream(idFile);
        props.store(os, "");
        os.close();

        System.out.println("=======================");
        System.out.println();
    }

    public Job submitJobNew(Configuration configuration) throws Exception {
        Job job = new Job(configuration);

        // propagate delegation related props from launcher job to MR job
        if (System.getenv("HADOOP_TOKEN_FILE_LOCATION") != null) {
            job.getConfiguration().set("mapreduce.job.credentials.binary", System.getenv("HADOOP_TOKEN_FILE_LOCATION"));
        }

        job.submit();

        return job;
    }

    public void initAccumuloFormats(Configuration actionConf) throws ClassNotFoundException {
        String zookeeperInstanceName = actionConf.get("zookeeperInstanceName");
        String zookeeperServerNames = actionConf.get("zookeeperServerNames");
        String username = actionConf.get("username");
        String password = actionConf.get("password");
        String table = actionConf.get("tableName");

        AccumuloInputFormat.setZooKeeperInstance(actionConf, zookeeperInstanceName, zookeeperServerNames);
        AccumuloInputFormat.setInputInfo(actionConf, username, password.getBytes(), table, new Authorizations());

        if (actionConf.get(START_TIME) != null && !actionConf.get(START_TIME).equals("-1")) {
            System.out.println("Setting Accumulo TimeStampIterator start time to " + actionConf.get(START_TIME));
            Map<String, String> timestampIteratorProperties = new HashMap<String, String>();
            String timestampFilterStartTimeString = actionConf.get(START_TIME);
            timestampIteratorProperties.put(TimestampFilter.START, timestampFilterStartTimeString);
            timestampIteratorProperties.put(TimestampFilter.START_INCL, Boolean.toString(true));
            IteratorSetting timestampIteratorSettings = new IteratorSetting(1, TimestampFilter.class, timestampIteratorProperties);
            AccumuloInputFormat.addIterator(actionConf, timestampIteratorSettings);
        }

        AccumuloOutputFormat.setZooKeeperInstance(actionConf, zookeeperInstanceName, zookeeperServerNames);
        AccumuloOutputFormat.setOutputInfo(actionConf, username, password.getBytes(), false, table);
    }
}
