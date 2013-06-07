package com.altamiracorp.reddawn;

import org.apache.commons.cli.*;

import java.util.ArrayList;

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

        ArrayList<SearchEngine> engines = new ArrayList<SearchEngine>();
        ArrayList<Query> queries = new ArrayList<Query>();
        Crawler crawler = new Crawler(cl.getOptionValue("directory"));

        for(String s : cl.getOptionValue("query").split(",")) {
            queries.add(new Query(s.trim()));
        }

        int results = 0;
        try {
            results = Integer.parseInt(cl.getOptionValue("result-count"));
        } catch (NumberFormatException e) {
            System.err.println("[WebCrawl] --result-count must be a valid number");
            System.exit(1);
        }

        // Adds engines based on the provider option
        ArrayList<String> enginesAdded = new ArrayList<String>();
        for(String s : cl.getOptionValue("provider").split(",")) {
            String trimmed = s.trim();
            if(!enginesAdded.contains(trimmed.toLowerCase())) {
                if(trimmed.equalsIgnoreCase("google")) engines.add(new GoogleSearchEngine(crawler));
                else if(trimmed.equalsIgnoreCase("news")) engines.add(new GoogleNewsSearchEngine(crawler));
                //else if(trimmed.equalsIgnoreCase("reddit")) engines.add(new RedditSearchEngine(crawler));

                // Adds the queries listed to the search engine created
                //SearchEngine current = engines.get(engines.size() - 1);
                //for(Query q : queries) current.addQueryToQueue(q, results);

                enginesAdded.add(trimmed.toLowerCase());
            }
        }

        // Runs queries on the search engine
        for(SearchEngine engine : engines) {
            for(Query q : queries) {
                System.out.println("\n\033[1mRunning Query \"" + q.getQueryString() + "\" on " + engine.getClass() + "\033[0m");

                engine.runQuery(q, results);
            }
        }
    }

    /**
     * Generates the list of command line options for the program
     *
     * @return The options for the command line program
     */
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
                        .withDescription("The search provider(s) to use for this query (choose one or more of google, news, and reddit)")
                        .isRequired()
                        .hasArg(true)
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withArgName("q")
                        .withLongOpt("query")
                        .withDescription("The query/queries you want to perform (separate multiple with commas)")
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

