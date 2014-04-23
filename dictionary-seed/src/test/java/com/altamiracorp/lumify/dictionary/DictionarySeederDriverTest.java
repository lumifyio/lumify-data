package io.lumify.dictionary;

import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class DictionarySeederDriverTest {

    DictionarySeederDriver driver;
    CommandLine mockCL;

    @Before
    public void setUp() {
        driver = new DictionarySeederDriver();
        mockCL = mock(CommandLine.class);
    }

    @Test
    public void testGetTypesNormal() throws Exception {
        when(mockCL.getOptionValue("types")).thenReturn("place,person,work");
        String[] results = driver.getTypes(mockCL);
        assertEquals("A standard set of types returned the incorrect number of results", 3, results.length);
        assertEquals("A standard set of types was not parsed correctly", "place", results[0]);
        assertEquals("A standard set of types was not parsed correctly", "person", results[1]);
        assertEquals("A standard set of types was not parsed correctly", "work", results[2]);
    }

    @Test
    public void testGetTypesNull() throws Exception {
        when(mockCL.getOptionValue("types")).thenReturn(null);
        String[] results = driver.getTypes(mockCL);
        assertEquals("A null types entry returned the wrong number of types", 5, results.length);
    }

    @Test
    public void testGetTypesEmpty() throws Exception {
        when(mockCL.getOptionValue("types")).thenReturn("");
        String[] results = driver.getTypes(mockCL);
        assertEquals("An empty types entry returned the wrong number of types", 5, results.length);
    }

    @Test
    public void testGetSearchCategoryNormal() throws Exception {
        assertEquals("A normal search type returned the wrong category", DictionarySearcher.PLACE,
                driver.getSearchCategory("place"));
    }

    @Test(expected = RuntimeException.class)
    public void testGetSearchCategoryInvalid() throws Exception {
        driver.getSearchCategory("badtype");
    }
}
