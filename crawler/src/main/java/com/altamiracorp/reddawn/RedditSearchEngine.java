package com.altamiracorp.reddawn;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: swoloszy
 * Date: 6/7/13
 * Time: 3:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class RedditSearchEngine extends SearchEngine{

	public RedditSearchEngine(Crawler crawler)
	{
		super(crawler);
	}

	@Override
	protected ArrayList<String> search(Query q, int maxResults) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
