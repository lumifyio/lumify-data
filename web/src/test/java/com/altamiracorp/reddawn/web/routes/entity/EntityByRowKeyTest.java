package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import com.altamiracorp.reddawn.web.routes.RouteTestBase;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.powermock.reflect.Whitebox;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class EntityByRowKeyTest extends RouteTestBase {
    private EntityByRowKey entityByRowKey;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        entityByRowKey = new EntityByRowKey();
        entityByRowKey.setApp(mockApp);
        Whitebox.setInternalState(entityByRowKey, TermRepository.class, mockTermRepository);
    }

    @Test
    public void testHandle() throws Exception {
        TermRowKey termRowKey = new TermRowKey("Joe Ferner", "manual", "person");
        Term term = new Term(termRowKey);

        when(mockRequest.getAttribute("rowKey")).thenReturn(termRowKey.toString());

        entityByRowKey.handle(mockRequest, mockResponse, mockHandlerChain);

        JSONObject responseJson = new JSONObject(responseStringWriter.getBuffer().toString());
        assertEquals(termRowKey.getSign(), responseJson.getJSONObject("key").getString("sign"));
        assertEquals(termRowKey.getConceptLabel(), responseJson.getJSONObject("key").getString("conceptLabel"));
        assertEquals(termRowKey.getModelKey(),responseJson.getJSONObject("key").getString("modelKey"));
    }
}
