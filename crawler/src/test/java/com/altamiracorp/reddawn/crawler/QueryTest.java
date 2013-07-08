package com.altamiracorp.reddawn.crawler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class QueryTest {
    Query q;

    @Before
    public void setUp() throws Exception {
        q = new Query();
    }

    @Test
    public void createQuery() throws Exception {
        assertNotNull(q);
        assertNotNull(q.getOptionalTerms());
        assertNotNull(q.getRequiredTerms());
        assertNotNull(q.getExcludedTerms());
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
    public void testGetQueryInfo() throws Exception {
        q.addOptionalTerm("optionalTerm");
        q.addOptionalTerm("optionalTerm2");
        q.addExcludedTerm("excludedTerm");
        q.addRequiredTerm("requiredTerm");
        String info = q.getQueryInfo();
        assertNotNull(info);
        System.out.println(info);
        assertEquals("{optionalTerms: {optionalTerm, optionalTerm2}, requiredTerms: {requiredTerm}, " +
                "excludedTerms: {excludedTerm}}", info);
    }
}
