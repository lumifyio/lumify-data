package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.ucd.predicate.PredicateRowKey;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import com.altamiracorp.reddawn.ucd.statement.StatementRepository;
import com.altamiracorp.reddawn.ucd.statement.StatementRowKey;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import com.altamiracorp.reddawn.web.routes.RouteTestBase;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class EntityByRelatedEntitiesTest extends RouteTestBase {
    private EntityByRelatedEntities entityByRelatedEntities;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        entityByRelatedEntities = new EntityByRelatedEntities();
        entityByRelatedEntities.setApp(mockApp);
        Whitebox.setInternalState(entityByRelatedEntities, StatementRepository.class, mockStatementRepository);
    }

    @Test
    public void testHandle() throws Exception {
        TermRowKey prefix = new TermRowKey("Joe Ferner", "manual", "person");
        StatementRowKey statementRowKey = new StatementRowKey(
                prefix,
                new PredicateRowKey("manual", "is BFFLs with"),
                new TermRowKey("Sam Wolo", "manual", "person"));
        Statement statement = new Statement(statementRowKey);

        when(mockRequest.getAttribute("rowKey")).thenReturn(prefix.toString());
        when(mockStatementRepository.findByRowStartsWith(mockModelSession, prefix.toString())).thenReturn(new ArrayList<Statement>(Arrays.asList(new Statement[]{statement})));

        entityByRelatedEntities.handle(mockRequest, mockResponse, mockHandlerChain);

        JSONArray responseJson = new JSONArray(responseStringWriter.getBuffer().toString());
        assertEquals(responseJson.length(), 1);
        JSONObject statementJson = responseJson.getJSONObject(0);
        TermRowKey objectTermRowKey = new TermRowKey(statementRowKey.getObjectRowKey());
        assertEquals(statementJson.getString("rowKey"), statementRowKey.getObjectRowKey());
        assertEquals(statementJson.getString("subType"), objectTermRowKey.getConceptLabel());
        assertEquals(statementJson.getString("title"), objectTermRowKey.getSign());
        assertEquals(statementJson.getString("type"), "entity");
    }
}
