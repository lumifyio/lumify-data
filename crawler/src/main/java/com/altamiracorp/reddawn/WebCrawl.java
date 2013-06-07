package com.altamiracorp.reddawn;

import org.apache.commons.cli.*;

/**
 * Created with IntelliJ IDEA.
 * User: jprincip
 * Date: 6/6/13
 * Time: 1:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class WebCrawl {

    public static void main(String[] args) throws Exception {
        GnuParser parser = new GnuParser();
        CommandLine cl = parser.parse(createOptions(), args);

        Query q = new Query(cl.getOptionValue("query"));

        String provider = cl.getOptionValue("provider");
        SearchEngine engine;

        if(provider.equals("google")) engine = new GoogleSearchEngine();
        else if(provider.equals("news")) engine = new GoogleNewsSearchEngine();
        else engine = new GoogleNewsSearchEngine();

        int results = 10;
        try {
            results = Integer.parseInt(cl.getOptionValue("result-count"));
        } catch (NumberFormatException e) {
            System.err.println("[WebCrawl] --result-count must be a valid number");
            System.exit(1);
        }

        System.out.println("[WebCrawl] Search Result Links: " + engine.runQuery(q, results));

    }

    public static Options createOptions() {
        Options options = new Options();

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

        options.addOption(
                OptionBuilder
                        .withArgName("c")
                        .withLongOpt("result-count")
                        .withDescription("The number of results to return from the query")
                        .isRequired()
                        .hasArg(true)
                        .create()
        );

        return options;
    }
}

