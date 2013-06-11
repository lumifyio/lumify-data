package com.altamiracorp.reddawn;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: jprincip
 * Date: 6/6/13
 * Time: 11:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class SearchEngineTest extends TestCase {
	SearchEngine engine;
	Query q1;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		Crawler c = new Crawler("sdf");
		engine = new GoogleSearchEngine(c);
		q1 = new Query();
		q1.addOptionalTerm("boston bombing");
		q1.addOptionalTerm("2013");
	}

	@Test
	public void testAddQueryToQueue() throws Exception {
		// Properly formatted entry
		boolean first = engine.addQueryToQueue(q1, 7);
		assertTrue("Properly formatted queries are not added correctly", first);
		assertEquals("Properly formatted queries are causing the queues to become unbalanced",
				engine.getMaxResultQueue().size(), engine.getQueryQueue().size());

		// Improperly formatted entry (negative result count)
		boolean second = engine.addQueryToQueue(q1, -1);
		assertFalse("Negative result counts are added when they shouldn't be", second);
		assertEquals("Negative result counts are causing the queues to become unbalanced",
				engine.getMaxResultQueue().size(), engine.getQueryQueue().size());
	}
}