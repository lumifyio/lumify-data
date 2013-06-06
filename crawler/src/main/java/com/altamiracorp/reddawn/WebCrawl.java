package com.altamiracorp.reddawn;

import org.apache.commons.cli.*;

/**
 * Created with IntelliJ IDEA.
 * User: jprincip
 * Date: 6/6/13
 * Time: 1:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class WebCrawl extends GnuParser {
    private String directory, provider, query;
    private CommandLine cl;

    public static void main(String[] args) throws Exception {
        WebCrawl parser = new WebCrawl();
        parser.parse(args);

    }

    public Options getOptions() {
        Options options = super.getOptions();

        options.addOption(
                OptionBuilder
                        .withArgName("d")
                        .withLongOpt("directory")
                        .withDescription("The directory to import")
                        .isRequired()
                        .hasArg(true)
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withArgName("p")
                        .withLongOpt("provider")
                        .withDescription("The search provider to use for this query")
                        .isRequired()
                        .hasArg(true)
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withArgName("q")
                        .withLongOpt("query")
                        .withDescription("The query you want to perform")
                        .isRequired()
                        .hasArg(true)
                        .create()
        );

        return options;
    }

    public boolean parse(String[] args) {
        try {
            cl = super.parse(getOptions(), args);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    public String getDirectory() {
        return cl.getOptionValue("directory");
    }

    public String getProvider() {
        return cl.getOptionValue("provider");
    }

    public String getQuery() {
        return cl.getOptionValue("query");
    }
}

