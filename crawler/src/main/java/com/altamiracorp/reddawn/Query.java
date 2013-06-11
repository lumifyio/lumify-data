package com.altamiracorp.reddawn;

import java.util.ArrayList;
import java.util.Arrays;

public class Query {
	private ArrayList<String> excludedTerms;
	private ArrayList<String> requiredTerms;
	private ArrayList<String> optionalTerms;
	private String rss;
	private String subreddit;

	public Query() {
		excludedTerms = new ArrayList<String>();
		requiredTerms = new ArrayList<String>();
		optionalTerms = new ArrayList<String>();
		rss = "";
		subreddit = "";
	}

	public Query(String query) {
		this();
		rss = "";
		subreddit = "";
		optionalTerms = new ArrayList(Arrays.asList(query.split(" ")));
	}

	public void addExcludedTerm(String term) {
		excludedTerms.add(term);
	}

	public void addRequiredTerm(String term) {
		requiredTerms.add(term);
	}

	public void addOptionalTerm(String term) {
		optionalTerms.add(term);
	}

	public void setRSSFeed(String url) {
		rss = url;
	}

	public ArrayList<String> getExcludedTerms() {
		return excludedTerms;
	}

	public ArrayList<String> getRequiredTerms() {
		return requiredTerms;
	}

	public ArrayList<String> getOptionalTerms() {
		return optionalTerms;
	}

	public String getRss() {
		return rss;
	}

	public String getQueryString() {
		return ((optionalTerms.size() > 0) ? EngineFunctions.concatenate(optionalTerms, " ") : "")
				+ ((requiredTerms.size() > 0) ? " +" + EngineFunctions.concatenate(requiredTerms, " +") : "")
				+ ((excludedTerms.size() > 0) ? " -" + EngineFunctions.concatenate(excludedTerms, " -") : "");
	}

	public String getQueryInfo() {
		StringBuilder info = new StringBuilder();
		info.append("{");
		info.append("optionalTerms: ");
		info.append("{" + EngineFunctions.concatenate(optionalTerms, ", ") + "}");
		info.append(", ");
		info.append("requiredTerms: ");
		info.append("{" + EngineFunctions.concatenate(requiredTerms, ", ") + "}");
		info.append(", ");
		info.append("excludedTerms: ");
		info.append("{" + EngineFunctions.concatenate(excludedTerms, ", ") + "}");
		info.append("}");
		return info.toString();
	}

	public void setSubreddit(String subreddit_) {
		subreddit = subreddit_;
	}

	public void clearSubreddit() {
		subreddit = "";
	}

	public String getSubreddit() {
		return subreddit;
	}

	public Query clone() {
		Query clone = new Query();
		try {
			for (String s : optionalTerms) clone.addOptionalTerm(s);
			for (String s : excludedTerms) clone.addExcludedTerm(s);
			for (String s : requiredTerms) clone.addRequiredTerm(s);

			clone.setRSSFeed(rss);
			clone.setSubreddit(subreddit);

		} catch (Exception e) {
			System.out.println("The query was not successfully copied");
			e.printStackTrace();
			// Question: Should this throw an exception instead?
		}
		return clone;
	}
}
