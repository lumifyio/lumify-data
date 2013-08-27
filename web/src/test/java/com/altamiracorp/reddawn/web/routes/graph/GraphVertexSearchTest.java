package com.altamiracorp.reddawn.web.routes.graph;

import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.graph.GraphVertexImpl;
import com.altamiracorp.reddawn.model.ontology.Property;
import com.altamiracorp.reddawn.model.ontology.PropertyName;
import com.altamiracorp.reddawn.web.routes.RouteTestBase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class GraphVertexSearchTest extends RouteTestBase {
    private ArrayList<GraphVertex> vertices;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        vertices = new ArrayList<GraphVertex>();

        GraphVertex person1 = new GraphVertexImpl();
        person1.setProperty(PropertyName.TITLE, "person1");
        person1.setProperty("birthDate", Property.DATE_FORMAT.parse("1978-10-30"));
        person1.setProperty("alias", "Joe");
        person1.setProperty("income", 500.12);
        vertices.add(person1);

        GraphVertex person2 = new GraphVertexImpl();
        person2.setProperty(PropertyName.TITLE, "person2");
        person2.setProperty("birthDate", Property.DATE_FORMAT.parse("1977-01-30"));
        person2.setProperty("alias", "Bob Smith");
        person2.setProperty("income", 300.5);
        vertices.add(person2);
    }

    @Test
    public void testFilter_date_isBefore() throws Exception {
        JSONArray filterJson = new JSONArray();
        JSONObject propertyFilter = new JSONObject();
        propertyFilter.put("propertyName", "birthDate");
        propertyFilter.put("propertyDataType", "date");
        propertyFilter.put("predicate", "<");
        propertyFilter.put("values", new JSONArray("['1977-10-30']"));
        filterJson.put(propertyFilter);
        List<GraphVertex> filteredVertices = GraphVertexSearch.filterVertices(vertices, filterJson);
        assertEquals(1, filteredVertices.size());
        assertEquals("person2", filteredVertices.get(0).getProperty(PropertyName.TITLE));
    }

    @Test
    public void testFilter_date_isAfter() throws Exception {
        JSONArray filterJson = new JSONArray();
        JSONObject propertyFilter = new JSONObject();
        propertyFilter.put("propertyName", "birthDate");
        propertyFilter.put("propertyDataType", "date");
        propertyFilter.put("predicate", ">");
        propertyFilter.put("values", new JSONArray("['1977-10-30']"));
        filterJson.put(propertyFilter);
        List<GraphVertex> filteredVertices = GraphVertexSearch.filterVertices(vertices, filterJson);
        assertEquals(1, filteredVertices.size());
        assertEquals("person1", filteredVertices.get(0).getProperty(PropertyName.TITLE));
    }

    @Test
    public void testFilter_date_isEqual() throws Exception {
        JSONArray filterJson = new JSONArray();
        JSONObject propertyFilter = new JSONObject();
        propertyFilter.put("propertyName", "birthDate");
        propertyFilter.put("propertyDataType", "date");
        propertyFilter.put("predicate", "equal");
        propertyFilter.put("values", new JSONArray("['1977-01-30']"));
        filterJson.put(propertyFilter);
        List<GraphVertex> filteredVertices = GraphVertexSearch.filterVertices(vertices, filterJson);
        assertEquals(1, filteredVertices.size());
        assertEquals("person2", filteredVertices.get(0).getProperty(PropertyName.TITLE));
    }

    @Test
    public void testFilter_date_range() throws Exception {
        JSONArray filterJson = new JSONArray();
        JSONObject propertyFilter = new JSONObject();
        propertyFilter.put("propertyName", "birthDate");
        propertyFilter.put("propertyDataType", "date");
        propertyFilter.put("predicate", "range");
        propertyFilter.put("values", new JSONArray("['1977-01-28', '1977-02-15']"));
        filterJson.put(propertyFilter);
        List<GraphVertex> filteredVertices = GraphVertexSearch.filterVertices(vertices, filterJson);
        assertEquals(1, filteredVertices.size());
        assertEquals("person2", filteredVertices.get(0).getProperty(PropertyName.TITLE));
    }

    @Test
    public void testFilter_string_contains() throws Exception {
        JSONArray filterJson = new JSONArray();
        JSONObject propertyFilter = new JSONObject();
        propertyFilter.put("propertyName", "alias");
        propertyFilter.put("propertyDataType", "string");
        propertyFilter.put("values", new JSONArray("['bob']"));
        filterJson.put(propertyFilter);
        List<GraphVertex> filteredVertices = GraphVertexSearch.filterVertices(vertices, filterJson);
        assertEquals(1, filteredVertices.size());
        assertEquals("person2", filteredVertices.get(0).getProperty(PropertyName.TITLE));
    }

    @Test
    public void testFilter_number_lessThan() throws Exception {
        JSONArray filterJson = new JSONArray();
        JSONObject propertyFilter = new JSONObject();
        propertyFilter.put("propertyName", "income");
        propertyFilter.put("propertyDataType", "currency");
        propertyFilter.put("predicate", "<");
        propertyFilter.put("values", new JSONArray("[400]"));
        filterJson.put(propertyFilter);
        List<GraphVertex> filteredVertices = GraphVertexSearch.filterVertices(vertices, filterJson);
        assertEquals(1, filteredVertices.size());
        assertEquals("person2", filteredVertices.get(0).getProperty(PropertyName.TITLE));
    }

    @Test
    public void testFilter_number_greaterThan() throws Exception {
        JSONArray filterJson = new JSONArray();
        JSONObject propertyFilter = new JSONObject();
        propertyFilter.put("propertyName", "income");
        propertyFilter.put("propertyDataType", "currency");
        propertyFilter.put("predicate", ">");
        propertyFilter.put("values", new JSONArray("['400']"));
        filterJson.put(propertyFilter);
        List<GraphVertex> filteredVertices = GraphVertexSearch.filterVertices(vertices, filterJson);
        assertEquals(1, filteredVertices.size());
        assertEquals("person1", filteredVertices.get(0).getProperty(PropertyName.TITLE));
    }

    @Test
    public void testFilter_number_isEqual() throws Exception {
        JSONArray filterJson = new JSONArray();
        JSONObject propertyFilter = new JSONObject();
        propertyFilter.put("propertyName", "income");
        propertyFilter.put("propertyDataType", "currency");
        propertyFilter.put("predicate", "equal");
        propertyFilter.put("values", new JSONArray("[300.5]"));
        filterJson.put(propertyFilter);
        List<GraphVertex> filteredVertices = GraphVertexSearch.filterVertices(vertices, filterJson);
        assertEquals(1, filteredVertices.size());
        assertEquals("person2", filteredVertices.get(0).getProperty(PropertyName.TITLE));
    }

    @Test
    public void testFilter_number_range() throws Exception {
        JSONArray filterJson = new JSONArray();
        JSONObject propertyFilter = new JSONObject();
        propertyFilter.put("propertyName", "income");
        propertyFilter.put("propertyDataType", "currency");
        propertyFilter.put("predicate", "range");
        propertyFilter.put("values", new JSONArray("[400, 600]"));
        filterJson.put(propertyFilter);
        List<GraphVertex> filteredVertices = GraphVertexSearch.filterVertices(vertices, filterJson);
        assertEquals(1, filteredVertices.size());
        assertEquals("person1", filteredVertices.get(0).getProperty(PropertyName.TITLE));
    }
}
