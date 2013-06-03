package com.altamiracorp.reddawn;

import com.altamiracorp.reddawn.cmdline.UcdCommandLineBase;
import com.altamiracorp.reddawn.ucd.*;
import com.altamiracorp.reddawn.ucd.inputFormats.UCDArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.model.Term;
import com.altamiracorp.reddawn.ucd.outputFormats.UCDOutputFormat;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.Tool;

import java.util.Properties;

public abstract class ConfigurableMapJobBase extends UcdCommandLineBase implements Tool {

    private static final String CONFIG_ZOOKEEPER_INSTANCE_NAME = "ZookeeperInstanceName";
    private static final String CONFIG_ZOOKEEPER_SERVER_NAMES = "ZookeeperServerNames";
    private static final String CONFIG_USERNAME = "Username";
    private static final String CONFIG_PASSWORD = "Password";
    private Class clazz;
    private String[] config;

    @Override
    protected Options getOptions() {
        Options options = super.getOptions();

        if (hasConfigurableClassname()) {
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
        }

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
        job.getConfiguration().set(CONFIG_ZOOKEEPER_INSTANCE_NAME, getZookeeperInstanceName());
        job.getConfiguration().set(CONFIG_ZOOKEEPER_SERVER_NAMES, getZookeeperServerNames());
        job.getConfiguration().set(CONFIG_USERNAME, getUsername());
        job.getConfiguration().set(CONFIG_PASSWORD, new String(getPassword()));
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

    public static UcdClient<AuthorizationLabel> createUcdClient(Mapper.Context context) throws AccumuloSecurityException, AccumuloException {
        ConnectionConfiguration config = new ConnectionConfiguration();
        config.setInstanceName(context.getConfiguration().get(CONFIG_ZOOKEEPER_INSTANCE_NAME));
        config.setZookeepers(context.getConfiguration().get(CONFIG_ZOOKEEPER_SERVER_NAMES));
        config.setUsername(context.getConfiguration().get(CONFIG_USERNAME));
        config.setPassword(context.getConfiguration().get(CONFIG_PASSWORD).getBytes());
        config.setPoolBatchThreadCount(1);

        Properties properties = new Properties();
        return UcdFactory.createUcdClient(config, properties);
    }

    public static QueryUser<AuthorizationLabel> getQueryUser(Mapper.Context context) {
        AuthorizationLabel auth = new SimpleAuthorizationLabel("U"); // TODO fill this in
        return new QueryUser<AuthorizationLabel>(context.getConfiguration().get(CONFIG_USERNAME), auth);
    }
}
