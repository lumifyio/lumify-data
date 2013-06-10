package com.altamiracorp.reddawn;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: swoloszy
 * Date: 6/10/13
 * Time: 12:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class RSSEngine extends SearchEngine {
	private String url;

	public RSSEngine(Crawler c) {
		super(c);
	}

	@Override
	protected ArrayList<String> search(Query q, int maxResults) {
		return null;
	}


}
