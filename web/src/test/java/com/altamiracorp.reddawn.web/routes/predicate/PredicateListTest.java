package com.altamiracorp.reddawn.web.routes.predicate;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.ucd.predicate.Predicate;
import com.altamiracorp.reddawn.ucd.predicate.PredicateRepository;
import com.altamiracorp.reddawn.ucd.predicate.PredicateRowKey;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class PredicateListTest {
    private PredicateList predicateList;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private HandlerChain mockHandlerChain;
    private WebApp mockApp;
    private PredicateRepository mockPredicateRepository;
    private RedDawnSession mockRedDawnSessionSession;
    private Session mockSession;
    private StringWriter responseStringWriter;

    @Before
    public void setUp() throws IOException {
        responseStringWriter = new StringWriter();

        mockApp = Mockito.mock(WebApp.class);
        mockRequest = Mockito.mock(HttpServletRequest.class);
        mockResponse = Mockito.mock(HttpServletResponse.class);
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseStringWriter));
        mockHandlerChain = Mockito.mock(HandlerChain.class);
        mockPredicateRepository = Mockito.mock(PredicateRepository.class);
        mockRedDawnSessionSession = Mockito.mock(RedDawnSession.class);
        mockSession = Mockito.mock(Session.class);
        when(mockRedDawnSessionSession.getModelSession()).thenReturn(mockSession);

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
