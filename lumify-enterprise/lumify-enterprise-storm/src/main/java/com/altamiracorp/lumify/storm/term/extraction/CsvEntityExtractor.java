package com.altamiracorp.lumify.storm.term.extraction;

import static com.google.common.base.Preconditions.*;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermRelationship;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.storm.structuredData.MappingProperties;
import com.altamiracorp.lumify.storm.structuredData.mapping.DocumentMapping;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.type.GeoPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class CsvEntityExtractor {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(CsvEntityExtractor.class);
    private final Map<String, SimpleDateFormat> dateFormatCache = new HashMap<String, SimpleDateFormat>();

    public TermExtractionResult extract(Vertex artifactVertex, User user) throws IOException, ParseException {
        checkNotNull(artifactVertex);
        checkNotNull(user);
//        TermExtractionResult termExtractionResult = new TermExtractionResult();
        TermExtractionResult termExtractionResult = null;
        String artifactRowKey = (String) artifactVertex.getPropertyValue(PropertyName.ROW_KEY.toString(), 0);
        LOGGER.debug("Processing graph vertex [%s] for artifact: %s", artifactVertex.getId(), artifactRowKey);

        String mappingJsonString = (String) artifactVertex.getPropertyValue(PropertyName.MAPPING_JSON.toString(), 0);
        if (mappingJsonString != null) {
            DocumentMapping mapping = new ObjectMapper().readValue(mappingJsonString, DocumentMapping.class);
            String text = (String) artifactVertex.getPropertyValue(PropertyName.TEXT.toString(), 0);
            termExtractionResult = mapping.mapDocument(new StringReader(text), getClass().getName());
//            JSONObject mappingJson = new JSONObject(artifact.getMetadata().getMappingJson());
//            int row = 0;
//            int skipRows = mappingJson.getInt(MappingProperties.SKIP_ROWS);
//
//            CsvPreference csvPrefs = CsvPreference.EXCEL_PREFERENCE;
//            LineReader reader = new LineReader(new StringReader(artifact.getMetadata().getText()));
//            String line;
//            int lastOffset = 0;
//            while ((line = reader.readLine()) != null) {
//                if (line.length() == 0) {
//                    break;
//                }
//                CsvListReader csvReader = new CsvListReader(new StringReader(line), csvPrefs);
//                List<String> columns = csvReader.read();
//                if (columns == null) {
//                    break;
//                }
//
//                if (row >= skipRows) {
//                    processLine(termExtractionResult, lastOffset, columns, mappingJson);
//                }
//                row++;
//                lastOffset = reader.getOffset();
//            }
        }
        return termExtractionResult;
    }


    private void processLine(TermExtractionResult termExtractionResult, int offset, List<String> columns, JSONObject mappingJson) throws ParseException {
        List<TermMention> termMentions = getTermsWithGraphVertices(offset, columns, mappingJson);
        termExtractionResult.addAllTermMentions(termMentions);
        termExtractionResult.addAllRelationships(getRelationships(termMentions, mappingJson));
    }

    private List<TermRelationship> getRelationships(List<TermMention> termMentions, JSONObject mappingJson) {
        List<TermRelationship> relationships = new ArrayList<TermRelationship>();
        JSONArray mappingColumnsJson = (JSONArray) mappingJson.get("columns");
        for (int columnIndex = 0; columnIndex < mappingColumnsJson.length(); columnIndex++) {
            JSONObject columnMappingJson = mappingColumnsJson.getJSONObject(columnIndex);
            JSONArray relationshipsJson = columnMappingJson.optJSONArray("relationships");
            if (relationshipsJson != null) {
                for (int relationshipIndex = 0; relationshipIndex < relationshipsJson.length(); relationshipIndex++) {
                    JSONObject relationshipJson = relationshipsJson.getJSONObject(relationshipIndex);
                    int targetIndex = relationshipJson.getInt("target");
                    String label = relationshipJson.getString("label");
                    TermMention sourceTermMention = termMentions.get(columnIndex);
                    TermMention destTermMention = termMentions.get(targetIndex);
                    relationships.add(new TermRelationship(sourceTermMention, destTermMention, label));
                }
            }
        }
        return relationships;
    }

    private List<TermMention> getTermsWithGraphVertices(int offset, List<String> columns, JSONObject mappingJson) throws ParseException {
        List<TermMention> termMentions = new ArrayList<TermMention>();
        JSONArray mappingColumnsJson = (JSONArray) mappingJson.get(MappingProperties.COLUMNS);
        String process = getClass().getName();
        for (int i = 0; i < columns.size(); i++) {
            JSONObject columnMappingJson = mappingColumnsJson.getJSONObject(i);
            String sign = columns.get(i);
            sign = sign == null ? "" : sign;
            String type = columnMappingJson.getString(MappingProperties.TYPE);
            if (type.equals(MappingProperties.TERM)) {
                String ontologyClassUri = columnMappingJson.getString(MappingProperties.CONCEPT_LABEL);
                boolean useExisting = columnMappingJson.optBoolean(MappingProperties.USE_EXISTING, false);

                // Checking to see if any properties were specified
                Map<String, Object> properties = null;
                if (columnMappingJson.has(MappingProperties.PROPERTIES)) {
                    properties = new HashMap<String, Object>();
                    JSONArray propertiesMappingJson = columnMappingJson.getJSONArray(MappingProperties.PROPERTIES);
                    for (int propIndex = 0; propIndex < propertiesMappingJson.length(); propIndex++) {
                        JSONObject propertyMappingJson = propertiesMappingJson.getJSONObject(propIndex);
                        int target = propertyMappingJson.getInt(MappingProperties.TARGET);
                        String name = propertyMappingJson.getString(MappingProperties.NAME);

                        JSONObject targetPropertyMappingJson = mappingColumnsJson.getJSONObject(target);
                        String columnData = columns.get(target);
                        Object propertyValue = getPropertyValue(targetPropertyMappingJson, columnData);
                        properties.put(name, propertyValue);
                    }
                }

                termMentions.add(new TermMention.Builder()
                        .start(offset)
                        .end(offset + sign.length())
                        .sign(sign)
                        .ontologyClassUri(ontologyClassUri)
                        .resolved(true)
                        .properties(properties)
                        .useExisting(useExisting)
                        .process(process)
                        .build());
            } else {
                termMentions.add(null);
            }
            offset = offset + sign.length() + 1;
        }
        return termMentions;
    }

    private Object getPropertyValue(JSONObject propertyMappingJson, String columnData) throws ParseException {
        String dataType = propertyMappingJson.getString(MappingProperties.DATA_TYPE);
        if (columnData == null) {
            return dataType.equals(MappingProperties.DATE) ? new Date() : "";
        }
        if (dataType.equals(MappingProperties.DATE)) {
            return getPropertyValueDate(propertyMappingJson, columnData);
        } else if (dataType.equals(MappingProperties.GEO_LOCATION)) {
            String[] latlong = columnData.split(",");
            return new GeoPoint(Float.valueOf(latlong[0]), Float.valueOf(latlong[1]));
        } else {
            return columnData;
        }
    }

    private Object getPropertyValueDate(JSONObject propertyMappingJson, String columnData) throws ParseException {
        String format = propertyMappingJson.getString(MappingProperties.FORMAT);
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
}
