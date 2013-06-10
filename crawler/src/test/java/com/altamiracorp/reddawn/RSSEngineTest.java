package com.altamiracorp.reddawn;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: swoloszy
 * Date: 6/10/13
 * Time: 12:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class RSSEngineTest extends TestCase {

	RSSEngine engine;

	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		Crawler crawler = new Crawler();
		engine = new RSSEngine(crawler);
	}

	@Test
	public void testSearch() throws Exception {
	  git
	}
}
