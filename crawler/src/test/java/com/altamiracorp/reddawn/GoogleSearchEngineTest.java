package com.altamiracorp.reddawn;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: jprincip
 * Date: 6/5/13
 * Time: 4:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleSearchEngineTest extends TestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();

    }

    @Test
    public void testRunQuery() throws Exception {
        GoogleSearchEngine engine = new GoogleSearchEngine();
        assertNotNull("The URL returned no results", engine.runQuery(null, 20));
    }
}
