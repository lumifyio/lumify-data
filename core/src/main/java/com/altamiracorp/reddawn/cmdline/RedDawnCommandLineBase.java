package com.altamiracorp.reddawn.cmdline;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloSession;
import com.altamiracorp.reddawn.model.TitanGraphSession;
import com.altamiracorp.reddawn.search.BlurSearchProvider;
import com.altamiracorp.reddawn.search.ElasticSearchProvider;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.commons.cli.*;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;

import java.util.Properties;

public abstract class RedDawnCommandLineBase extends Configured implements Tool {
    private String zookeeperInstanceName;
    private String zookeeperServerNames;
    private String username;
    private byte[] password;
    private String blurHdfsPath;
    private String blurControllerLocation;
    private String hadoopUrl;
    private String graphStorageIndexSearchHostname;
    private String searchProvider;
    private String elasticSearchLocations;

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
        this.zookeeperInstanceName = cmd.getOptionValue("zookeeperInstanceName");
        this.zookeeperServerNames = cmd.getOptionValue("zookeeperServerNames");
        this.hadoopUrl = cmd.getOptionValue("hadoopUrl");
        this.username = cmd.getOptionValue("username");
        this.password = cmd.getOptionValue("password").getBytes();
        this.blurHdfsPath = cmd.getOptionValue("blurPath");
        this.blurControllerLocation = cmd.getOptionValue("blurControllerLocation");
        this.graphStorageIndexSearchHostname = cmd.getOptionValue("graph.storage.index.search.hostname");
        this.searchProvider = cmd.getOptionValue("search.provider");
        this.elasticSearchLocations = cmd.getOptionValue("elasticsearch.locations");
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
                        .withLongOpt("zookeeperInstanceName")
                        .withDescription("The name of the Zoo Keeper Instance")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("name")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("zookeeperServerNames")
                        .withDescription("Comma seperated list of Zoo Keeper servers")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("servers")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("hadoopUrl")
                        .withDescription("Hadoop URL. Example: hdfs://192.168.33.10:8020")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("url")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("username")
                        .withDescription("The name of the user to connect with")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("username")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("password")
                        .withDescription("The password of the user to connect with")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("password")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("blurPath")
                        .withDescription("The path to blur")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("blurPath")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("blurControllerLocation")
                        .withDescription("The path to blur")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("blurControllerLocation")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("graph.storage.index.search.hostname")
                        .withDescription("The hostname of elastic search")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("graph.storage.index.search.hostname")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("search.provider")
                        .withDescription("The class name of the SearchProvider implementation")
                        .hasArg(true)
                        .withArgName("search.provider")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("elasticsearch.locations")
                        .withDescription("Comma-separated list hostname:port for each Elastic Search server")
                        .hasArg(true)
                        .withArgName("elasticsearch.locations")
                        .create()
        );

        return options;
    }

    public RedDawnSession createRedDawnSession() {
        Properties properties = new Properties();
        properties.setProperty(AccumuloSession.HADOOP_URL, getHadoopUrl());
        properties.setProperty(AccumuloSession.ZOOKEEPER_INSTANCE_NAME, getZookeeperInstanceName());
        properties.setProperty(AccumuloSession.ZOOKEEPER_SERVER_NAMES, getZookeeperServerNames());
        properties.setProperty(AccumuloSession.USERNAME, getUsername());
        properties.setProperty(AccumuloSession.PASSWORD, new String(getPassword()));
        if (getBlurControllerLocation() != null) {
            properties.setProperty(BlurSearchProvider.BLUR_CONTROLLER_LOCATION, getBlurControllerLocation());
        }
        if (getBlurHdfsPath() != null) {
            properties.setProperty(BlurSearchProvider.BLUR_PATH, getBlurHdfsPath());
        }
        if (getGraphStorageIndexSearchHostname() != null) {
            properties.setProperty(TitanGraphSession.STORAGE_INDEX_SEARCH_HOSTNAME, getGraphStorageIndexSearchHostname());
        }
        if (getSearchProvider() != null) {
            properties.setProperty(RedDawnSession.SEARCH_PROVIDER_PROP_KEY, getSearchProvider());
        }
        if (getElasticSearchLocations() != null) {
            properties.setProperty(ElasticSearchProvider.ES_LOCATIONS_PROP_KEY, getElasticSearchLocations());
        }
        return RedDawnSession.create(properties, null);
    }

    public String getZookeeperInstanceName() {
        return zookeeperInstanceName;
    }

    public String getHadoopUrl() {
        return hadoopUrl;
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

    public String getGraphStorageIndexSearchHostname() {
        return graphStorageIndexSearchHostname;
    }

    public void setGraphStorageIndexSearchHostname(String graphStorageIndexSearchHostname) {
        this.graphStorageIndexSearchHostname = graphStorageIndexSearchHostname;
    }

    public String getBlurHdfsPath() {
        return blurHdfsPath;
    }

    public void setBlurHdfsPath(String blurHdfsPath) {
        this.blurHdfsPath = blurHdfsPath;
    }

    public String getSearchProvider() {
        return searchProvider;
    }

    public void setSearchProvider(String searchProvider) {
        this.searchProvider = searchProvider;
    }

    public String getElasticSearchLocations() {
        return this.elasticSearchLocations;
    }

    public void setElasticSearchLocations(String elasticSearchLocations) {
        this.elasticSearchLocations = elasticSearchLocations;
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
