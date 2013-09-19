package com.altamiracorp.lumify.cmdline;

import com.altamiracorp.lumify.config.Configuration;
import com.altamiracorp.lumify.core.user.ModelAuthorizations;
import com.altamiracorp.lumify.core.user.SystemUser;
import com.altamiracorp.lumify.core.user.User;
import org.apache.commons.cli.*;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;

public abstract class CommandLineBase extends Configured implements Tool {
    private String configLocation = "file:///opt/lumify/config/configuration.properties";
    private String credentialsLocation;
    private Configuration configuration;
    private User user = new SystemUser();

    @Override
    public int run(String[] args) throws Exception {
        Options options = getOptions();
        CommandLine cmd;
        try {
            CommandLineParser parser = new GnuParser();
            cmd = parser.parse(options, args);
            if (cmd.hasOption("help")) {
                printHelp(options);
            }
            processOptions(cmd);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            printHelp(options);
            return -1;
        }
        return run(cmd);
    }

    protected void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("run", options, true);
    }

    protected abstract int run(CommandLine cmd) throws Exception;

    protected void processOptions(CommandLine cmd) throws Exception {
        if (cmd.hasOption("configLocation")) {
            configLocation = cmd.getOptionValue("configLocation");
        }
        if (cmd.hasOption("credentialsLocation")) {
            credentialsLocation = cmd.getOptionValue("credentialsLocation");
        } else {
            credentialsLocation = configLocation;
        }
    }

    protected Options getOptions() {
        Options options = new Options();

        options.addOption(
                OptionBuilder
                        .withLongOpt("help")
                        .withDescription("Print help")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("configLocation")
                        .withDescription("Configuration file location")
                        .hasArg()
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("credentialsLocation")
                        .withDescription("Credentials configuration file location")
                        .hasArg()
                        .create()
        );

        return options;
    }

    protected Configuration getConfiguration() {
        if (configuration == null) {
            configuration = Configuration.loadConfigurationFile(configLocation, credentialsLocation);
        }
        return configuration;
    }

    public ModelAuthorizations getAuthorizations() {
        return getUser().getModelAuthorizations();
    }

    protected Class loadClass(String className) {
        try {
            return this.getClass().getClassLoader().loadClass(className);
        } catch (Exception e) {
            throw new RuntimeException("Could not find class '" + className + "'", e);
        }
    }

    protected User getUser() {
        return user;
    }
}
