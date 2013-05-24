package com.altamiracorp.reddawn.cmdline;

import com.altamiracorp.reddawn.search.BlurSearchProvider;
import com.altamiracorp.reddawn.search.SearchProvider;
import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.ConnectionConfiguration;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.UcdFactory;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.commons.cli.*;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;

import java.util.Properties;

public abstract class UcdCommandLineBase extends Configured implements Tool {
    private String zookeeperInstanceName;
    private String zookeeperServerNames;
    private String username;
    private byte[] password;
    private String blurControllerLocation;
    private Integer blurControllerPort;
    private String blurHdfsPath;

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

    protected void processOptions(CommandLine cmd) {
        this.zookeeperInstanceName = cmd.getOptionValue("zookeeperInstanceName");
        this.zookeeperServerNames = cmd.getOptionValue("zookeeperServerNames");
        this.username = cmd.getOptionValue("username");
        this.password = cmd.getOptionValue("password").getBytes();
    }

    protected Options getOptions() {
        Options options = new Options();

        options.addOption(
                OptionBuilder
                        .withArgName("h")
                        .withLongOpt("help")
                        .withDescription("Print help")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withArgName("zi")
                        .withLongOpt("zookeeperInstanceName")
                        .withDescription("The name of the Zoo Keeper Instance")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("name")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withArgName("zs")
                        .withLongOpt("zookeeperServerNames")
                        .withDescription("Comma seperated list of Zoo Keeper servers")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("servers")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withArgName("u")
                        .withLongOpt("username")
                        .withDescription("The name of the user to connect with")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("username")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withArgName("p")
                        .withLongOpt("password")
                        .withDescription("The password of the user to connect with")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("password")
                        .create()
        );

        return options;
    }

    public UcdClient<AuthorizationLabel> createUcdClient() throws AccumuloSecurityException, AccumuloException {
        ConnectionConfiguration config = new ConnectionConfiguration();
        config.setInstanceName(getZookeeperInstanceName());
        config.setZookeepers(getZookeeperServerNames());
        config.setUsername(getUsername());
        config.setPassword(getPassword());
        config.setPoolBatchThreadCount(1);

        Properties properties = new Properties();
        return UcdFactory.createUcdClient(config, properties);
    }

    public SearchProvider createSearchProvider() throws Exception {
        BlurSearchProvider provider = new BlurSearchProvider();
        Properties props = new Properties();
        if (getBlurControllerLocation() != null) {
            props.setProperty(BlurSearchProvider.BLUR_CONTROLLER_LOCATION, getBlurControllerLocation());
        }
        if (getBlurControllerPort() != null) {
            props.setProperty(BlurSearchProvider.BLUR_CONTROLLER_PORT, getBlurControllerPort().toString());
        }
        if (getBlurHdfsPath() != null) {
            props.setProperty(BlurSearchProvider.BLUR_PATH, getBlurHdfsPath());
        }
        provider.setup(props);
        return provider;
    }

    public String getZookeeperInstanceName() {
        return zookeeperInstanceName;
    }

    public void setZookeeperInstanceName(String zookeeperInstanceName) {
        this.zookeeperInstanceName = zookeeperInstanceName;
    }

    public String getZookeeperServerNames() {
        return zookeeperServerNames;
    }

    public void setZookeeperServerNames(String zookeeperServerNames) {
        this.zookeeperServerNames = zookeeperServerNames;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }

    public String getBlurControllerLocation() {
        return blurControllerLocation;
    }

    public void setBlurControllerLocation(String blurControllerLocation) {
        this.blurControllerLocation = blurControllerLocation;
    }

    public Integer getBlurControllerPort() {
        return blurControllerPort;
    }

    public void setBlurControllerPort(Integer blurControllerPort) {
        this.blurControllerPort = blurControllerPort;
    }

    public String getBlurHdfsPath() {
        return blurHdfsPath;
    }

    public void setBlurHdfsPath(String blurHdfsPath) {
        this.blurHdfsPath = blurHdfsPath;
    }

    public Authorizations getAuthorizations() {
        return new Authorizations(); // TODO configurable
    }

    protected Class loadClass(String className) {
        try {
            return this.getClass().getClassLoader().loadClass(className);
        } catch (Exception e) {
            throw new RuntimeException("Could not find class '" + className + "'", e);
        }
    }
}
