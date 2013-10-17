package com.altamiracorp.lumify.storm.term.extraction;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.util.LineReader;
import com.google.inject.Inject;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import org.codehaus.jettison.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CsvEntityExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvEntityExtractor.class);
    private Map<String, SimpleDateFormat> dateFormatCache = new HashMap<String, SimpleDateFormat>();
    private ArtifactRepository artifactRepository;


    public TermExtractionResult extract(GraphVertex graphVertex, User user) throws IOException, JSONException, ParseException {
        TermExtractionResult termExtractionResult = new TermExtractionResult();

        if (graphVertex != null) {
            String artifactRowKey = (String) graphVertex.getProperty(PropertyName.ROW_KEY);
            LOGGER.info(String.format("Processing graph vertex [%s] for artifact: %s", graphVertex.getId(), artifactRowKey));

            Artifact artifact = artifactRepository.findByRowKey(artifactRowKey, user);
            if (artifact.getMetadata().getMappingJson() != null) {
                JSONObject mappingJson = new JSONObject(artifact.getMetadata().getMappingJson());
                int row = 0;
                int skipRows = mappingJson.getInt("skipRows");

                CsvPreference csvPrefs = CsvPreference.EXCEL_PREFERENCE;
                LineReader reader = new LineReader(new StringReader(artifact.getMetadata().getText()));
                String line;
                Map<String, GraphVertex> allGraphVertex = new HashMap<String, GraphVertex>();
                int lastOffset = 0;
                while ((line = reader.readLine()) != null) {
                    if (line.length() == 0) {
                        break;
                    }
                    CsvListReader csvReader = new CsvListReader(new StringReader(line), csvPrefs);
                    List<String> columns = csvReader.read();
                    if (columns == null) {
                        break;
                    }

                    if (row >= skipRows) {
                        termExtractionResult.addAll(processLine(artifact, lastOffset, columns, allGraphVertex, mappingJson));
                    }
                    row++;
                    lastOffset = reader.getOffset();
                }
            }
        } else {
            LOGGER.warn("Could not find vertex with id: " + graphVertex.getId());
        }

        return termExtractionResult;
    }

    private List<TermExtractionResult.TermMention> processLine(Artifact artifact, int offset, List<String> columns, Map<String, GraphVertex> allGraphVertex, JSONObject mappingJson) throws JSONException, ParseException {
        List<TermExtractionResult.TermMention> termMentions = getTermsWithGraphVertices(artifact, offset, columns, allGraphVertex, mappingJson);
        return termMentions;
    }

    private List<TermExtractionResult.TermMention> getTermsWithGraphVertices(Artifact artifact, int offset, List<String> columns, Map<String, GraphVertex> allGraphVertex, JSONObject mappingJson) throws JSONException, ParseException {
        List<TermExtractionResult.TermMention> termMentions = new ArrayList<TermExtractionResult.TermMention>();
        JSONArray mappingColumnsJson = (JSONArray) mappingJson.get("columns");
        for (int i = 0; i < columns.size(); i++) {
            JSONObject columnMappingJson = mappingColumnsJson.getJSONObject(i);
            String sign = columns.get(i);
            sign = sign == null ? "" : sign;
            String type = columnMappingJson.getString("type");
            if (type.equals("term")) {
                String ontologyClassUri = columnMappingJson.getString("conceptLabel");
                boolean useExisting = columnMappingJson.optBoolean("useExisting", false);

                // Checking to see if any properties were specified
                Map<String, Object> properties = null;
                if (columnMappingJson.has("properties")) {
                    properties = new HashMap<String, Object>();
                    JSONArray propertiesMappingJson = columnMappingJson.getJSONArray("properties");
                    for (int propIndex = 0; propIndex < propertiesMappingJson.length(); propIndex++) {
                        JSONObject propertyMappingJson = propertiesMappingJson.getJSONObject(propIndex);
                        int target = propertyMappingJson.getInt("target");
                        String name = propertyMappingJson.getString("name");

                        JSONObject targetPropertyMappingJson = mappingColumnsJson.getJSONObject(target);
                        String columnData = columns.get(target);
                        Object propertyValue = getPropertyValue(targetPropertyMappingJson, columnData);
                        properties.put(name, propertyValue);
                    }
                }

                termMentions.add(new TermExtractionResult.TermMention(offset, offset + sign.length(), sign, ontologyClassUri, true, properties, null, useExisting));
            }
        }
        return termMentions;
    }

    private Object getPropertyValue(JSONObject propertyMappingJson, String columnData) throws JSONException, ParseException {
        String dataType = propertyMappingJson.getString("dataType");
        if (columnData == null) {
            return dataType.equals("date") ? new Date() : "";
        }
        if (dataType.equals("date")) {
            return getPropertyValueDate(propertyMappingJson, columnData);
        } else if (dataType.equals("geoLocation")) {
            String[] latlong = columnData.split(",");
            return Geoshape.point(Float.valueOf(latlong[0]), Float.valueOf(latlong[1]));
        } else {
            return columnData;
        }
    }

    private Object getPropertyValueDate(JSONObject propertyMappingJson, String columnData) throws JSONException, ParseException {
        String format = propertyMappingJson.getString("format");
        SimpleDateFormat sdf;
        if (format != null) {
            sdf = dateFormatCache.get(format);
            if (sdf == null) {
                sdf = new SimpleDateFormat(format);
                dateFormatCache.put(format, sdf);
            }
        } else {
            sdf = dateFormatCache.get("<default>");
            if (sdf == null) {
                sdf = new SimpleDateFormat();
                dateFormatCache.put("<default>", sdf);
            }
        }

        return sdf.parse(columnData).getTime();
    }

    @Inject
    public void setArtifactRepository(ArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }
}
