package com.altamiracorp.reddawn.web.routes.predicate;

import com.altamiracorp.reddawn.ucd.predicate.Predicate;
import com.altamiracorp.reddawn.ucd.predicate.PredicateRowKey;
import com.altamiracorp.reddawn.web.routes.RouteTestBase;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class PredicateListTest extends RouteTestBase {
    private PredicateList predicateList;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        predicateList = new PredicateList();
        predicateList.setApp(mockApp);
        predicateList.setPredicateRepository(mockPredicateRepository);
    }

    @Test
    public void testHandle() throws Exception {
        ArrayList<Predicate> predicates = new ArrayList<Predicate>();

        Predicate predicate1 = new Predicate(new PredicateRowKey("testModel", "testPredicate1"));
        predicate1.getPredicateElements()
                .setLabelUi("Test Predicate 1");
        predicates.add(predicate1);

        Predicate predicate2 = new Predicate(new PredicateRowKey("testModel", "testPredicate2"));
        predicate2.getPredicateElements()
                .setLabelUi("Test Predicate 2");
        predicates.add(predicate2);

        when(mockApp.getRedDawnSession(mockRequest)).thenReturn(mockRedDawnSessionSession);
        when(mockPredicateRepository.findAll(mockRedDawnSessionSession.getModelSession()))
                .thenReturn(predicates);

        predicateList.handle(mockRequest, mockResponse, mockHandlerChain);

        JSONArray responseJson = new JSONArray(responseStringWriter.getBuffer().toString());
        assertEquals(2, responseJson.length());

        assertEquals("testModel\u001ftestPredicate1", responseJson.getJSONObject(0).getJSONObject("rowKey").getString("value"));
        assertEquals("Test Predicate 1", responseJson.getJSONObject(0).getString("labelUi"));

        assertEquals("testModel\u001ftestPredicate2", responseJson.getJSONObject(1).getJSONObject("rowKey").getString("value"));
        assertEquals("Test Predicate 2", responseJson.getJSONObject(1).getString("labelUi"));
    }
}
