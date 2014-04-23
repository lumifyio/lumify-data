package io.lumify.dictionary;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.URLEncoder;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class DictionarySearcherTest {

    private DictionarySearcher searcher;

    @Before
    public void setUp() throws Exception {
        searcher = new DictionarySearcher();
    }

    @Test(expected = RuntimeException.class)
    public void testHttpRequestMalformedURL() throws Exception {
        searcher.httpRequest("bad url");
    }

    @Test(expected = RuntimeException.class)
    public void testHttpRequestNonexistentPage() throws Exception {
        searcher.httpRequest("http://thisdoesnotexist.com/sjklaherliu");
    }

    @Test
    public void testProcessJsonNormal() throws Exception {
        int resultCount = searcher.processJson("{ \"head\": { \"link\": [], \"vars\": [\"name\"] },\n" +
                "\"results\": { \"distinct\": false, \"ordered\": true, \"bindings\": [\n" +
                "{ \"name\": { \"type\": \"literal\", \"xml:lang\": \"en\", \"value\": \"\\u00C1kos R\\u00E1thonyi\" }},\n" +
                "{ \"name\": { \"type\": \"literal\", \"xml:lang\": \"en\", \"value\": \"Alvaro Arzu\" }},\n" +
                "{ \"name\": { \"type\": \"literal\", \"xml:lang\": \"en\", \"value\": \"\\u00C1lvaro Arz\\u00FA\" }}\n" +
                "]}}");

        assertEquals("A normal JSON object returned the wrong number of results", 3, resultCount);
    }

    @Test
    public void testProcessJsonEmpty() throws Exception {
        int resultCount = searcher.processJson("{ \"head\": { \"link\": [], \"vars\": [\"name\"] },\n" +
                "\"results\": { \"distinct\": false, \"ordered\": true, \"bindings\": [\n" +
                "]}}");

        assertEquals("A JSON object with no results returned the wrong number of results", 0, resultCount);
    }

    @Test(expected = JSONException.class)
    public void testProcessJsonInvalid() throws Exception {
        searcher.processJson("i'm not JSON");
    }

    @Test
    public void testGetUrl() throws Exception {
        String url = searcher.getUrl("Place", 0);
        assertEquals("A normal query URL was returned incorrectly encoded", "http://dbpedia.org/sparql/?format=json&query=" +
                URLEncoder.encode("PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                        "SELECT ?name WHERE{?place a dbo:Place;rdfs:label ?name.}\n" +
                        "LIMIT 50000\nOFFSET 0", "UTF-8"), url);
    }
}
