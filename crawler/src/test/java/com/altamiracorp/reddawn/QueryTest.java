package com.altamiracorp.reddawn;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: swoloszy
 * Date: 6/5/13
 * Time: 5:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueryTest extends TestCase {

    @Test
    public void createQuery() throws Exception {
        Query q = new Query("US", "Monday", "Friday", "Here", "100", "1000");
        assertNotNull(q);

    }

    @Test
    public void testAddExcludedTerm() throws Exception {

    }

    @Test
    public void testAddRequiredTerm() throws Exception {

    }

    @Test
    public void testAddOptionalTerm() throws Exception {

    }

    @Test
    public void testGetSearchItems() throws Exception {

    }

    @Test
    public void testGetExcludedTerms() throws Exception {

    }

    @Test
    public void testGetRequiredTerms() throws Exception {

    }

    @Test
    public void testGetOptionalTerms() throws Exception {

    }
}
