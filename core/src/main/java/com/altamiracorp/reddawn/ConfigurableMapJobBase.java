package com.altamiracorp.reddawn;

import com.altamiracorp.reddawn.cmdline.RedDawnCommandLineBase;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.model.AccumuloSession;
import com.altamiracorp.reddawn.search.BlurSearchProvider;
import com.altamiracorp.reddawn.ucd.term.Term;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.Tool;

import java.util.Map;
import java.util.Properties;

public abstract class ConfigurableMapJobBase extends RedDawnCommandLineBase implements Tool {
    private Class clazz;
    private String[] config;

    @Override
    protected Options getOptions() {
        Options options = super.getOptions();

        if (hasConfigurableClassname()) {
            options.addOption(
                    OptionBuilder
                            .withLongOpt("classname")
                            .withDescription("The class to run")
                            .withArgName("name")
                            .isRequired()
                            .hasArg()
                            .create()
            );
        }

        options.addOption(
                OptionBuilder
                        .withLongOpt("config")
                        .withDescription("Configuration for the class")
                        .withArgName("name=value")
                        .hasArg()
                        .create('D')
        );

        return options;
    }

    @Override
    protected void processOptions(CommandLine cmd) throws Exception {
        super.processOptions(cmd);

        if (hasConfigurableClassname()) {
            String pluginClassName = cmd.getOptionValue("classname");
            if (pluginClassName == null) {
                throw new RuntimeException("'class' parameter is required");
            }
            clazz = loadClass(pluginClassName);
        }

        config = cmd.getOptionValues("config");
    }

    protected boolean hasConfigurableClassname() {
        return true;
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        Job job = new Job(getConf(), this.getClass().getSimpleName());
        job.getConfiguration().set(AccumuloSession.HADOOP_URL, getHadoopUrl());
        job.getConfiguration().set(AccumuloSession.ZOOKEEPER_INSTANCE_NAME, getZookeeperInstanceName());
        job.getConfiguration().set(AccumuloSession.ZOOKEEPER_SERVER_NAMES, getZookeeperServerNames());
        job.getConfiguration().set(AccumuloSession.USERNAME, getUsername());
        job.getConfiguration().set(AccumuloSession.PASSWORD, new String(getPassword()));
        if (getBlurControllerLocation() != null) {
            job.getConfiguration().set(BlurSearchProvider.BLUR_CONTROLLER_LOCATION, getBlurControllerLocation());
        }
        if (getBlurHdfsPath() != null) {
            job.getConfiguration().set(BlurSearchProvider.BLUR_PATH, getBlurHdfsPath());
        }
        job.setJarByClass(this.getClass());

        job.setInputFormatClass(getInputFormatClassAndInit(job));

        if (this.config != null) {
            for (String config : this.config) {
                String[] parts = config.split("=", 2);
                job.getConfiguration().set(parts[0], parts[1]);
            }
        }

        job.setMapOutputKeyClass(Key.class);
        job.setMapOutputValueClass(Value.class);
        job.setMapperClass(getMapperClass(job, clazz));

        job.setNumReduceTasks(0);

        Class<? extends OutputFormat> outputFormatClass = getOutputFormatClass();
        if (outputFormatClass != null) {
            job.setOutputFormatClass(outputFormatClass);
        }
        AccumuloModelOutputFormat.init(job, getUsername(), getPassword(), getZookeeperInstanceName(), getZookeeperServerNames(), Term.TABLE_NAME);

        job.waitForCompletion(true);
        return job.isSuccessful() ? 0 : 1;
    }

    protected abstract Class<? extends InputFormat> getInputFormatClassAndInit(Job job);

    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    protected abstract Class<? extends Mapper> getMapperClass(Job job, Class clazz);

    public static RedDawnSession createRedDawnSession(Mapper.Context context) {
        return RedDawnSession.create(context);
    }
}
