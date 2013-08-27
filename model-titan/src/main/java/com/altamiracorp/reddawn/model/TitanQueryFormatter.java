package com.altamiracorp.reddawn.model;

import com.altamiracorp.reddawn.model.ontology.Property;
import com.altamiracorp.reddawn.model.ontology.PropertyType;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.Tokens;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class TitanQueryFormatter {
    private static final Map<String, Tokens.T> tokenMap = Maps.newHashMap();

    static {
        tokenMap.put("<", Tokens.T.lt);
        tokenMap.put("<=", Tokens.T.lte);
        tokenMap.put(">", Tokens.T.gt);
        tokenMap.put(">=", Tokens.T.gte);
        tokenMap.put("equal", Tokens.T.eq);
        tokenMap.put("=", Tokens.T.eq);
    }

    private static GremlinPipeline filterDate(GremlinPipeline pipeline, JSONObject filterJson, String propertyName) throws JSONException, ParseException {
        String predicate = filterJson.optString("predicate");
        if (predicate == null) {
            throw new RuntimeException("'predicate' is required for data type 'date'");
        }

        JSONArray values = filterJson.optJSONArray("values");
        if (values == null) {
            throw new RuntimeException("'values' is required for data type 'date'");
        }

        Date value = Property.DATE_FORMAT.parse(values.getString(0));
        if (predicate.equals("range")) {
            if (values.length() != 2) {
                throw new RuntimeException("'range' requires 2 values, found " + values.length());
            }
            Date value1 = Property.DATE_FORMAT.parse(values.getString(1));
            return pipeline.interval(propertyName, value, value1);
        } else {
            if (values.length() != 1) {
                throw new RuntimeException(String.format("'%s' requires 1 value, found %d", predicate, values.length()));
            }
            Tokens.T comparison = tokenMap.get(predicate);
            if (comparison != null)
                return pipeline.has(propertyName, comparison, value);
            throw new RuntimeException("Invalid predicate " + predicate);
        }
    }

    private static GremlinPipeline filterNumber(GremlinPipeline pipeline, JSONObject filterJson, String propertyName) throws JSONException, ParseException {
        String predicate = filterJson.optString("predicate");
        if (predicate == null) {
            throw new RuntimeException("'predicate' is required for data type 'number'");
        }

        JSONArray values = filterJson.optJSONArray("values");
        if (values == null) {
            throw new RuntimeException("'values' is required for data type 'number'");
        }
        double value = values.getDouble(0);

        if (predicate.equals("range")) {
            if (values.length() != 2) {
                throw new RuntimeException("'range' requires 2 values, found " + values.length());
            }

            double value1 = values.getDouble(1);
            return pipeline.interval(propertyName, value, value1);
        } else {
            if (values.length() != 1) {
                throw new RuntimeException(String.format("'%s' requires 1 value, found %d", predicate, values.length()));
            }

            Tokens.T comparison = tokenMap.get(predicate);
            if (comparison != null)
                return pipeline.has(propertyName, comparison, value);
            throw new RuntimeException("Invalid predicate " + predicate);
        }
    }

    private static GremlinPipeline filterString(GremlinPipeline<Vertex, Vertex> pipeline, JSONObject filterJson, final String propertyName) throws JSONException {
        JSONArray values = filterJson.optJSONArray("values");
        if (values == null) {
            throw new RuntimeException("'values' is required for data type 'string'");
        }

        if (values.length() != 1) {
            throw new RuntimeException("'contains' requires 1 value, found " + values.length());
        }
        final String value = values.getString(0).toLowerCase();

        return pipeline.filter(new PipeFunction<Vertex, Boolean>() {
            @Override
            public Boolean compute(Vertex argument) {
                String property = argument.getProperty(propertyName);
                return StringUtils.containsIgnoreCase(property, value);
            }
        });
    }

    private GremlinPipeline<Vertex, Vertex> addFilter(JSONObject filterJson, GremlinPipeline<Vertex, Vertex> pipeline) {
        PropertyType propertyDateType = PropertyType.convert(filterJson.optString("propertyDataType"));
        String propertyName = filterJson.optString("propertyName");
        if (propertyName == null) {
            throw new RuntimeException("Could not find 'propertyName' to filter on.");
        }

        try {
            switch (propertyDateType) {
                case DATE:
                    return filterDate(pipeline, filterJson, propertyName);
                case CURRENCY:
                    return filterNumber(pipeline, filterJson, propertyName);
                default:
                    return filterString(pipeline, filterJson, propertyName);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a Gremlin pipeline from a JSON array of JSON objects.
     *
     * @param vertices    Vertices to start the pipeline on
     * @param filtersJson JSON array of objects to filter
     * @return Gremlin pipeline for filtering
     */
    public GremlinPipeline<Vertex, Vertex> createQueryPipeline(Iterable<Vertex> vertices, JSONArray filtersJson) {
        checkNotNull(vertices, "Vertices cannot be null");
        checkNotNull(filtersJson, "JSON filter cannot be null");
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<Vertex, Vertex>(vertices);
        for (int i = 0; i < filtersJson.length(); i++) {
            JSONObject filterJson;
            try {
                filterJson = filtersJson.getJSONObject(i);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            pipeline = addFilter(filterJson, pipeline);
        }
        return pipeline;
    }
}
