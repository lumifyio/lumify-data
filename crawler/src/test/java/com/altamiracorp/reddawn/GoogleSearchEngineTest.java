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
public class GoogleSearchEngineTest extends TestCase {
    GoogleSearchEngine engine;
    Query q1;

    @Before
    public void setUp() throws Exception {
        Crawler c = new Crawler("sdf");
        engine = new GoogleSearchEngine(c);
        q1 = new Query();
        q1.addOptionalTerm("boston bombing");
        q1.addOptionalTerm("2013");
    }

    @Test
	public void testSearch() {

	}

	@Test
	public void testProcessQuery() {

	}
}
