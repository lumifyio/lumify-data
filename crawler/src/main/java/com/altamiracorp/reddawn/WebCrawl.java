package com.altamiracorp.reddawn;

import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

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
        ArrayList<Query> rssLinks = new ArrayList<Query>();
        ArrayList<Query> redditQueries = new ArrayList<Query>();
        Crawler crawler = new Crawler(cl.getOptionValue("directory"));

        for(String s : cl.getOptionValue("query").split(",")) {
            Map<String, ArrayList<String>> queryTerms = parseQuery(s.trim());
            Query q = new Query();

            for(String type : queryTerms.keySet()) {
                for(String term : queryTerms.get(type)) {
                    if(type.equals("optional")) q.addOptionalTerm(term);
                    else if(type.equals("excluded")) q.addExcludedTerm(term);
                    else if(type.equals("required")) q.addRequiredTerm(term);
                }
            }

            queries.add(q);

            // Checks for subreddits and sets information accordingly
            String subreddits = cl.getOptionValue("subreddit");

            if(subreddits != null) {
                for(String subreddit : subreddits.split(",")) {
                    Query redditQ = q.clone();
                    if(subreddit.trim().equals("all"))redditQ.clearSubreddit();
                    else redditQ.setSubreddit(subreddit);
                    redditQueries.add(redditQ);
                }
            } else {
                redditQueries.add(q);
            }
        }

        if(cl.getOptionValue("rss") != null) {
            for(String feed : cl.getOptionValue("rss").split(",")) {
                Query rssFeed = new Query();
                rssFeed.setRSSFeed(feed);
                rssLinks.add(rssFeed);
            }
        }

        int results = -1;
        try {
            String countParam = cl.getOptionValue("result-count");
            if(countParam != null) results = Integer.parseInt(countParam);
        } catch (NumberFormatException e) {
            System.err.println("[WebCrawl] --result-count must be a valid number");
            System.exit(1);
        }

        // Adds engines based on the provider option
        ArrayList<String> enginesAdded = new ArrayList<String>();
        for(String s : cl.getOptionValue("provider").split(",")) {
            String trimmed = s.trim();
            if(!enginesAdded.contains(trimmed.toLowerCase())) {
                SearchEngine engine = new GoogleNewsSearchEngine(crawler);
                ArrayList<Query> queryList = queries;

                if(trimmed.equalsIgnoreCase("google")) {
                    engine = new GoogleSearchEngine(crawler);
                    queryList = queries;
                } else if(trimmed.equalsIgnoreCase("news")) {
                    engine = new GoogleNewsSearchEngine(crawler);
                    queryList = queries;
                } else if(trimmed.equalsIgnoreCase("reddit")) {
                    engine = new RedditSearchEngine(crawler);
                    queryList = redditQueries;
                } else if(trimmed.equalsIgnoreCase("rss")) {
                    engine = new RSSEngine(crawler);
                    queryList = rssLinks;
                }

                for(Query q : queryList) engine.addQueryToQueue(q, results);
                engines.add(engine);

                enginesAdded.add(trimmed.toLowerCase());
            }
        }

        // Runs queries on the search engine
        for(SearchEngine engine : engines) engine.runQueue();
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
                        .withDescription("The search provider(s) to use for this query (choose one or more of google, news, reddit, and rss)")
                        .isRequired()
                        .hasArg(true)
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withArgName("q")
                        .withLongOpt("query")
                        .withDescription("The query/queries you want to perform (separate multiple with commas) - required for google and news providers, optional for reddit")
                        .hasArg(true)
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withArgName("c")
                        .withLongOpt("result-count")
                        .withDescription("The number of results to return from the query")
                        .hasArg(true)
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withArgName("r")
                        .withLongOpt("rss")
                        .withDescription("The RSS feed URL(s) to fetch for this query (separate multiple with commas)")
                        .hasArg(true)
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withArgName("s")
                        .withLongOpt("subreddit")
                        .withDescription("The subreddit(s) to fetch (optionally filtered by query)")
                        .hasArg(true)
                        .create()
        );

        return options;
    }

    /**
     * Parses a single query for required, excluded and optional terms (mark required terms with +<term> and excluded with -<term>
     *
     * @param query The query string to parse
     * @return Map of type to an ArrayList of terms that match it.  Entries are "optional", "excluded", and "required"
     */
    public static Map<String, ArrayList<String>> parseQuery(String query) {
        Map<String, ArrayList<String>> params = new TreeMap<String, ArrayList<String>>();
        params.put("optional", new ArrayList<String>());
        params.put("required", new ArrayList<String>());
        params.put("excluded", new ArrayList<String>());

        // Gets and returns each term in the query (assumes space-separated terms)
        for(String term : query.split(" ")) {
            if(term.charAt(0) == '+') params.get("required").add(term.substring(1));
            else if(term.charAt(0) == '-') params.get("excluded").add(term.substring(1));
            else params.get("optional").add(term);
        }

        return params;
    }
}
