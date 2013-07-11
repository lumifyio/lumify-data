package com.altamiracorp.reddawn.crawler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class FlickrSearchEngineTest {
    FlickrSearchEngine engine;
    Query mockQuery;

    @Before
    public void setUp() throws Exception {
        Crawler mockCrawler = mock(Crawler.class);
        engine = new FlickrSearchEngine(mockCrawler);
    }

    @Test
    public void testCreateQueryUrl() throws Exception {
        String sampleQuery = "boston";
        int samplePageNumber = 1;
        int samplePerPage = 500;
        mockQuery = mock(Query.class);
        when(mockQuery.getExcludedTerms()).thenReturn(new ArrayList<String>());
        when(mockQuery.getRequiredTerms()).thenReturn(new ArrayList<String>());
        when(mockQuery.getOptionalTerms()).thenReturn(new ArrayList<String>(Arrays.asList(sampleQuery)));
        String result = engine.createQueryUrl(mockQuery, samplePageNumber, samplePerPage);
        String expectedQueryUrl = "http://api.flickr.com/services/rest?method=flickr.photos.search" +
                "&text=" + sampleQuery +
                "&accuracy=7" +
                "&api_key=06e4190d750d2386f81d1afde77d7b38" +
                "&content_type=7" +
                "&extras=description,license,date_upload,date_taken,owner_name,icon_server," +
                "original_format,last_update,geo,tags,machine_tags,o_dims,views,media" +
                "&license=7" +
                "&page=" + samplePageNumber +
                "&per_page=" + samplePerPage +
                "&privacy_filter=public";
        Assert.assertEquals(expectedQueryUrl, result);
    }

    @Test
    public void testProcessQuery() throws Exception {
        mockQuery = mock(Query.class);
        when(mockQuery.getExcludedTerms()).thenReturn(new ArrayList<String>(Arrays.asList("Zoey Deschanel")));
        when(mockQuery.getOptionalTerms()).thenReturn(new ArrayList<String>(Arrays.asList("bombing", "marathon")));
        when(mockQuery.getRequiredTerms()).thenReturn(new ArrayList<String>(Arrays.asList("boston")));
        TreeMap<String, String> result = engine.processQuery(mockQuery);
        assertTrue(result.containsKey("text"));
        assertTrue(result.containsValue("bombing+marathon+boston-Zoey-Deschanel"));
        assertTrue(result.get("text").equals("bombing+marathon+boston-Zoey-Deschanel"));
    }

    @Test
    public void testGetEngineName() throws Exception {
        String expectedName = "Flickr Search Engine";
        String result = engine.getEngineName();
        assertEquals(expectedName, result);
    }
}
