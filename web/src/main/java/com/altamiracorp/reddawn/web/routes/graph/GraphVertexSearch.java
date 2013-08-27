package com.altamiracorp.reddawn.web.routes.graph;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.ontology.OntologyRepository;
import com.altamiracorp.reddawn.model.ontology.Property;
import com.altamiracorp.reddawn.model.ontology.PropertyType;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GraphVertexSearch implements Handler, AppAware {
    private GraphRepository graphRepository = new GraphRepository();
    private OntologyRepository ontologyRepository = new OntologyRepository();
    private WebApp app;

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);
        String query = request.getParameter("q");
        String filter = request.getParameter("filter");
        JSONArray filterJson = new JSONArray(filter);

        resolvePropertyIds(session, filterJson);

        List<GraphVertex> vertices = graphRepository.searchVerticesByTitle(session.getGraphSession(), query, filterJson);
        JSONObject results = new JSONObject();
        results.put("vertices", GraphVertex.toJson(vertices));
        new Responder(response).respondWith(results);
    }

    private void resolvePropertyIds(RedDawnSession session, JSONArray filterJson) throws JSONException {
        for (int i = 0; i < filterJson.length(); i++) {
            JSONObject filter = filterJson.getJSONObject(i);
            if (filter.has("propertyId") && !filter.has("propertyName")) {
                int propertyId = filter.getInt("propertyId");
                Property property = ontologyRepository.getPropertyById(session.getGraphSession(), propertyId);
                if (property == null) {
                    throw new RuntimeException("Could not find property with id: " + propertyId);
                }
                filter.put("propertyName", property.getTitle());
                filter.put("propertyDataType", property.getDataType());
            }
        }
    }

    public static List<GraphVertex> filterVertices(List<GraphVertex> vertices, JSONArray filterJson) throws JSONException, ParseException {
        ArrayList<GraphVertex> results = new ArrayList<GraphVertex>();
        for (GraphVertex vertex : vertices) {
            if (!isFiltered(vertex, filterJson)) {
                results.add(vertex);
            }
        }
        return results;
    }

    public static boolean isFiltered(GraphVertex vertex, JSONArray filtersJson) throws JSONException, ParseException {
        for (int i = 0; i < filtersJson.length(); i++) {
            JSONObject filterJson = filtersJson.getJSONObject(i);
            if (isFiltered(vertex, filterJson)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isFiltered(GraphVertex vertex, JSONObject filterJson) throws JSONException, ParseException {
        PropertyType propertyDateType = PropertyType.convert(filterJson.optString("propertyDataType"));
        String propertyName = filterJson.optString("propertyName");
        if (propertyName == null) {
            throw new RuntimeException("Could not find 'propertyName' to filter on.");
        }

        switch (propertyDateType) {
            case DATE:
                return isFilteredDate(vertex, filterJson, propertyName);
            case CURRENCY:
                return isFilteredNumber(vertex, filterJson, propertyName);
            default:
                return isFilteredString(vertex, filterJson, propertyName);
        }
    }

    private static boolean isFilteredDate(GraphVertex vertex, JSONObject filterJson, String propertyName) throws JSONException, ParseException {
        String predicate = filterJson.optString("predicate");
        if (predicate == null) {
            throw new RuntimeException("'predicate' is required for data type 'date'");
        }

        JSONArray values = filterJson.optJSONArray("values");
        if (values == null) {
            throw new RuntimeException("'values' is required for data type 'date'");
        }

        Object propertyValueObj = vertex.getProperty(propertyName);
        if (propertyValueObj == null || !(propertyValueObj instanceof Date)) {
            return true;
        }
        Date propertyValue = (Date) propertyValueObj;

        if (predicate.equals("<")) {
            if (values.length() != 1) {
                throw new RuntimeException("'<' requires 1 value, found " + values.length());
            }
            Date value = Property.DATE_FORMAT.parse(values.getString(0));
            if (propertyValue.compareTo(value) < 0) {
                return false;
            }
        } else if (predicate.equals(">")) {
            if (values.length() != 1) {
                throw new RuntimeException("'>' requires 1 value, found " + values.length());
            }
            Date value = Property.DATE_FORMAT.parse(values.getString(0));
            if (propertyValue.compareTo(value) > 0) {
                return false;
            }
        } else if (predicate.equals("equal") || predicate.equals("=")) {
            if (values.length() != 1) {
                throw new RuntimeException("'=' requires 1 value, found " + values.length());
            }
            Date value = Property.DATE_FORMAT.parse(values.getString(0));
            if (propertyValue.compareTo(value) == 0) {
                return false;
            }
        } else if (predicate.equals("range")) {
            if (values.length() != 2) {
                throw new RuntimeException("'range' requires 2 values, found " + values.length());
            }
            Date value1 = Property.DATE_FORMAT.parse(values.getString(0));
            Date value2 = Property.DATE_FORMAT.parse(values.getString(1));
            if (propertyValue.compareTo(value1) >= 0 && propertyValue.compareTo(value2) <= 0) {
                return false;
            }
        } else {
            throw new RuntimeException("Invalid predicate " + predicate);
        }

        return true;
    }

    private static boolean isFilteredNumber(GraphVertex vertex, JSONObject filterJson, String propertyName) throws JSONException, ParseException {
        String predicate = filterJson.optString("predicate");
        if (predicate == null) {
            throw new RuntimeException("'predicate' is required for data type 'number'");
        }

        JSONArray values = filterJson.optJSONArray("values");
        if (values == null) {
            throw new RuntimeException("'values' is required for data type 'number'");
        }

        Object propertyValueObj = vertex.getProperty(propertyName);
        if (propertyValueObj == null) {
            return true;
        }
        double propertyValue = Double.parseDouble("" + propertyValueObj);

        if (predicate.equals("<")) {
            if (values.length() != 1) {
                throw new RuntimeException("'<' requires 1 value, found " + values.length());
            }
            double value = values.getDouble(0);
            if (propertyValue < value) {
                return false;
            }
        } else if (predicate.equals(">")) {
            if (values.length() != 1) {
                throw new RuntimeException("'>' requires 1 value, found " + values.length());
            }
            double value = values.getDouble(0);
            if (propertyValue > value) {
                return false;
            }
        } else if (predicate.equals("equal") || predicate.equals("=")) {
            if (values.length() != 1) {
                throw new RuntimeException("'=' requires 1 value, found " + values.length());
            }
            double value = values.getDouble(0);
            if (Math.abs(propertyValue - value) < 0.00001) {
                return false;
            }
        } else if (predicate.equals("range")) {
            if (values.length() != 2) {
                throw new RuntimeException("'range' requires 2 values, found " + values.length());
            }
            double value1 = values.getDouble(0);
            double value2 = values.getDouble(1);
            if (propertyValue >= value1 && propertyValue <= value2) {
                return false;
            }
        } else {
            throw new RuntimeException("Invalid predicate " + predicate);
        }

        return true;
    }

    private static boolean isFilteredString(GraphVertex vertex, JSONObject filterJson, String propertyName) throws JSONException {
        JSONArray values = filterJson.optJSONArray("values");
        if (values == null) {
            throw new RuntimeException("'values' is required for data type 'string'");
        }

        Object propertyValueObj = vertex.getProperty(propertyName);
        if (propertyValueObj == null) {
            return true;
        }
        String propertyValue = propertyValueObj.toString().toLowerCase();

        if (values.length() != 1) {
            throw new RuntimeException("'contains' requires 1 value, found " + values.length());
        }
        String value = values.getString(0).toLowerCase();

        return !propertyValue.contains(value);
    }
}
