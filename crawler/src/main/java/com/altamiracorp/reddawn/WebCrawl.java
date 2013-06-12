package com.altamiracorp.reddawn;

import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class WebCrawl {

    private GnuParser parser;
    private CommandLine cl;
    private ArrayList<SearchEngine> engines;
    private ArrayList<Query> queries, rssLinks, redditQueries;
    private Crawler crawler;
    private int results;
	public static final int DEFAULT_RESULT_COUNT = 20;

	public WebCrawl() {
		parser = new GnuParser();

		engines = new ArrayList<SearchEngine>();
		queries = new ArrayList<Query>();
		rssLinks = new ArrayList<Query>();
		redditQueries = new ArrayList<Query>();

		results = -1;
	}

	protected void setCommandLine(CommandLine cl) {
		this.cl = cl;
	}

	protected void setCrawler(Crawler crawler) {
		this.crawler = crawler;
	}

	public void prepare(String[] args) {
		loadCommandLine(args);

		if(getQueryParam() != null) {
			for(String s : getQueryParam().split(",")) {
				Query search = addSearchQuery(s);
				addRedditQueries(search);
			}
		} else {
			addRedditQueries(new Query());
		}

		addRSSLinks();
		setResultCount();
		addEngines();
	}

    protected void loadCommandLine(String[] args) {
        try {
            cl = parser.parse(createOptions(), args);
        } catch(ParseException e) {
            System.err.println("The options could not be parsed, please try again");
            System.exit(1);
        }

        crawler = new Crawler(cl.getOptionValue("directory"));
    }

    protected Query addSearchQuery(String queryString) {
        Map<String, ArrayList<String>> queryTerms = parseQuery(queryString.trim());
        Query q = new Query();

        for(String type : queryTerms.keySet()) {
            for(String term : queryTerms.get(type)) {
                if(type.equals("optional")) q.addOptionalTerm(term);
                else if(type.equals("excluded")) q.addExcludedTerm(term);
                else if(type.equals("required")) q.addRequiredTerm(term);
            }
        }
		if(!q.getQueryString().equals("")) queries.add(q);

        return q;
    }

    protected void addRedditQueries(Query q) {
        String subreddits = cl.getOptionValue("subreddit");

        if(subreddits != null && subreddits.trim().length() > 0) {
            for(String subreddit : subreddits.split(",")) {
				if(subreddit.trim().length() == 0) continue;
                Query redditQ = q.clone();
                if(subreddit.trim().equals("all"))redditQ.clearSubreddit();
                else redditQ.setSubreddit(subreddit);
                redditQueries.add(redditQ);
            }
        } else {
            redditQueries.add(q);
        }
    }

    protected void addRSSLinks() {
        if(cl.getOptionValue("rss") != null) {
            for(String feed : cl.getOptionValue("rss").split(",")) {
				if(feed.trim().length() == 0) continue;
                Query rssFeed = new Query();
                rssFeed.setRSSFeed(feed);
                rssLinks.add(rssFeed);
            }
        }
    }

    protected void addEngines() {
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
    }

    protected void setResultCount() {
        try {
            String countParam = cl.getOptionValue("result-count");
            if(countParam != null) {
				results = Integer.parseInt(countParam);
				if(results < 1) results = DEFAULT_RESULT_COUNT;
			}
        } catch (NumberFormatException e) {
            results = DEFAULT_RESULT_COUNT;
        }
    }

    public void run() {
        // Runs queries on the search engine
        for(SearchEngine engine : engines) engine.runQueue();
    }

    public String getQueryParam() {
        return cl.getOptionValue("query");
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

    public static Map<String, ArrayList<String>> parseQuery(String query) {
		Map<String, ArrayList<String>> params = new TreeMap<String, ArrayList<String>>();
        params.put("optional", new ArrayList<String>());
        params.put("required", new ArrayList<String>());
        params.put("excluded", new ArrayList<String>());

        // Gets and returns each term in the query (assumes space-separated terms)
        for(String term : query.split(" ")) {
			if(term.trim().length() == 0) continue;
            if(term.charAt(0) == '+') params.get("required").add(term.substring(1));
            else if(term.charAt(0) == '-') params.get("excluded").add(term.substring(1));
            else params.get("optional").add(term);
        }

        return params;
    }

	public ArrayList<SearchEngine> getEngines() {
		return engines;
	}

	public ArrayList<Query> getQueries() {
		return queries;
	}

	public ArrayList<Query> getRssLinks() {
		return rssLinks;
	}

	public ArrayList<Query> getRedditQueries() {
		return redditQueries;
	}

	public int getResults() {
		return results;
	}

	public static void main(String[] args) {
		WebCrawl driver = new WebCrawl();
		driver.prepare(args);
		driver.run();
	}
}
