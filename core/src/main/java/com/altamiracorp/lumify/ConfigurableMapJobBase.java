package com.altamiracorp.lumify;

import com.altamiracorp.lumify.cmdline.CommandLineBase;
import com.altamiracorp.lumify.config.Configuration;
import com.altamiracorp.lumify.model.AccumuloModelOutputFormat;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.Tool;

public abstract class ConfigurableMapJobBase extends CommandLineBase implements Tool {
    private Class clazz;
    private String[] config;
    private boolean failOnFirstError = false;

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

        options.addOption(
                OptionBuilder
                        .withLongOpt("failOnFirstError")
                        .withDescription("Enables failing on the first error that occurs")
                        .create()
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
        if (cmd.hasOption("failOnFirstError")) {
            failOnFirstError = true;
        }
    }

    protected boolean hasConfigurableClassname() {
        return true;
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        Job job = new Job(getConf(), this.getClass().getSimpleName());
        Configuration configuration = getConfiguration();
        configuration.configureJob(job);
        job.getConfiguration().setBoolean("failOnFirstError", failOnFirstError);
        job.setJarByClass(this.getClass());

        if (this.config != null) {
            for (String config : this.config) {
                String[] parts = config.split("=", 2);
                job.getConfiguration().set(parts[0], parts[1]);
            }
        }

        job.setInputFormatClass(getInputFormatClassAndInit(job));

        job.setMapOutputKeyClass(Key.class);
        job.setMapOutputValueClass(Value.class);
        job.setMapperClass(getMapperClass(job, clazz));

        job.setNumReduceTasks(0);

        Class<? extends OutputFormat> outputFormatClass = getOutputFormatClass();
        if (outputFormatClass != null) {
            job.setOutputFormatClass(outputFormatClass);
        }
        AccumuloModelOutputFormat.init(
                job,
                configuration.getDataStoreUserName(),
                configuration.getDataStorePassword(),
                configuration.getZookeeperInstanceName(),
                configuration.getZookeeperServerNames(),
                Artifact.TABLE_NAME);

        job.waitForCompletion(true);
        return job.isSuccessful() ? 0 : 1;
    }

    protected abstract Class<? extends InputFormat> getInputFormatClassAndInit(Job job);

    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    protected abstract Class<? extends Mapper> getMapperClass(Job job, Class clazz);

    public static AppSession createAppSession(TaskInputOutputContext context) {
        return AppSession.create(context);
    }

    protected String[] getConfig() {
        return this.config;
    }
}
