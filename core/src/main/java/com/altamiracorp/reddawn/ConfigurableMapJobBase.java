package com.altamiracorp.reddawn;

import com.altamiracorp.reddawn.cmdline.UcdCommandLineBase;
import com.altamiracorp.reddawn.ucd.inputFormats.UCDArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.models.Term;
import com.altamiracorp.reddawn.ucd.outputFormats.UCDOutputFormat;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.Tool;

public abstract class ConfigurableMapJobBase extends UcdCommandLineBase implements Tool {

    private Class clazz;
    private String[] config;

    @Override
    protected Options getOptions() {
        Options options = super.getOptions();

        options.addOption(
                OptionBuilder
                        .withArgName("c")
                        .withLongOpt("classname")
                        .withDescription("The class to run")
                        .withArgName("name")
                        .isRequired()
                        .hasArg()
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withArgName("D")
                        .withLongOpt("config")
                        .withDescription("Configuration for the class")
                        .withArgName("name=value")
                        .hasArg()
                        .create()
        );

        return options;
    }

    @Override
    protected void processOptions(CommandLine cmd) {
        super.processOptions(cmd);

        String textExtractorClassName = cmd.getOptionValue("classname");
        if (textExtractorClassName == null) {
            throw new RuntimeException("'class' parameter is required");
        }
        clazz = loadClass(textExtractorClassName);
        config = cmd.getOptionValues("config");
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        Job job = new Job(getConf(), this.getClass().getSimpleName());
        job.setJarByClass(this.getClass());

        job.setInputFormatClass(UCDArtifactInputFormat.class);
        UCDArtifactInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());

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
        UCDOutputFormat.init(job, getUsername(), getPassword(), getZookeeperInstanceName(), getZookeeperServerNames(), Term.TABLE_NAME);

        job.waitForCompletion(true);
        return job.isSuccessful() ? 0 : 1;
    }

    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return UCDOutputFormat.class;
    }

    protected abstract Class<? extends Mapper> getMapperClass(Job job, Class clazz);
}
