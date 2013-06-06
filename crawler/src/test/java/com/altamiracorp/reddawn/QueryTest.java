package com.altamiracorp.reddawn;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: swoloszy
 * Date: 6/5/13
 * Time: 5:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueryTest extends TestCase {
    Query q;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        q = new Query();
    }

    @Test
    public void createQuery() throws Exception {
        assertNotNull(q);
        assertNotNull(q.getOptionalTerms());
        assertNotNull(q.getRequiredTerms());
        assertNotNull(q.getExcludedTerms());
        assertNotNull(q.getSearchItems());
    }

    @Test
    public void testAddExcludedTerm() throws Exception {
        q.addExcludedTerm("excluded1");
        q.addExcludedTerm("excluded2");
        assertTrue(q.getExcludedTerms().contains("excluded1"));
        assertTrue(q.getExcludedTerms().contains("excluded2"));
    }

    @Test
    public void testAddRequiredTerm() throws Exception {
        q.addExcludedTerm("required1");
        q.addExcludedTerm("required2");
        assertTrue(q.getExcludedTerms().contains("required1"));
        assertTrue(q.getExcludedTerms().contains("required2"));
    }

    @Test
    public void testAddOptionalTerm() throws Exception {
        q.addExcludedTerm("optional1");
        q.addExcludedTerm("optional2");
        assertTrue(q.getExcludedTerms().contains("optional1"));
        assertTrue(q.getExcludedTerms().contains("optional2"));
    }

    @Test
    public void testGetSearchItems() throws Exception {
        assertNotNull(q.getSearchItems());
    }

    @Test
    public void testGetExcludedTerms() throws Exception {
        assertNotNull(q.getExcludedTerms());
    }

    @Test
    public void testGetRequiredTerms() throws Exception {
        assertNotNull(q.getRequiredTerms());
    }

    @Test
    public void testGetOptionalTerms() throws Exception {
        assertNotNull(q.getOptionalTerms());
    }

    @Test
    public void testSetStartDate() {
        assertFalse(q.setStartDate("Monday"));
        assertTrue(q.setStartDate("2013-06-06"));
    }
    @Test
     public void testSetEndDate() {
        assertFalse(q.setStartDate("Friday"));
        assertFalse(q.setStartDate("06.06.2013"));
        assertTrue(q.setStartDate("2013-07-06"));
    }

    @Test
    public void testSetCountry() {
        assertTrue(q.setCountry("af"));
        assertTrue(q.setCountry("AF"));
        assertFalse(q.setCountry("United States"));
    }
}
