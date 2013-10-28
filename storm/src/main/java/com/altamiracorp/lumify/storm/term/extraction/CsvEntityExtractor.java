package com.altamiracorp.lumify.storm.term.extraction;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.storm.structuredData.MappingProperties;
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

import static com.google.common.base.Preconditions.checkNotNull;

public class CsvEntityExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvEntityExtractor.class);
    private Map<String, SimpleDateFormat> dateFormatCache = new HashMap<String, SimpleDateFormat>();
    private ArtifactRepository artifactRepository;

    public TermExtractionResult extract(GraphVertex graphVertex, User user) throws IOException, JSONException, ParseException {
        checkNotNull(graphVertex);
        checkNotNull(user);
        TermExtractionResult termExtractionResult = new TermExtractionResult();
        String artifactRowKey = (String) graphVertex.getProperty(PropertyName.ROW_KEY);
        LOGGER.info(String.format("Processing graph vertex [%s] for artifact: %s", graphVertex.getId(), artifactRowKey));

        Artifact artifact = artifactRepository.findByRowKey(artifactRowKey, user.getModelUserContext());
        if (artifact.getMetadata().getMappingJson() != null) {
            JSONObject mappingJson = new JSONObject(artifact.getMetadata().getMappingJson());
            int row = 0;
            int skipRows = mappingJson.getInt(MappingProperties.SKIP_ROWS);

            CsvPreference csvPrefs = CsvPreference.EXCEL_PREFERENCE;
            LineReader reader = new LineReader(new StringReader(artifact.getMetadata().getText()));
            String line;
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
                    processLine(termExtractionResult, lastOffset, columns, mappingJson);
                }
                row++;
                lastOffset = reader.getOffset();
            }
        }
        return termExtractionResult;
    }


    private void processLine(TermExtractionResult termExtractionResult, int offset, List<String> columns, JSONObject mappingJson) throws JSONException, ParseException {
        List<TermExtractionResult.TermMention> termMentions = getTermsWithGraphVertices(offset, columns, mappingJson);
        termExtractionResult.addAllTermMentions(termMentions);
        termExtractionResult.addAllRelationships(getRelationships(termMentions, mappingJson));
    }

    private List<TermExtractionResult.Relationship> getRelationships(List<TermExtractionResult.TermMention> termMentions, JSONObject mappingJson) {
        List<TermExtractionResult.Relationship> relationships = new ArrayList<TermExtractionResult.Relationship>();
        JSONArray mappingColumnsJson = (JSONArray) mappingJson.get("columns");
        for (int columnIndex = 0; columnIndex < mappingColumnsJson.length(); columnIndex++) {
            JSONObject columnMappingJson = mappingColumnsJson.getJSONObject(columnIndex);
            JSONArray relationshipsJson = columnMappingJson.optJSONArray("relationships");
            if (relationshipsJson != null) {
                for (int relationshipIndex = 0; relationshipIndex < relationshipsJson.length(); relationshipIndex++) {
                    JSONObject relationshipJson = relationshipsJson.getJSONObject(relationshipIndex);
                    int targetIndex = relationshipJson.getInt("target");
                    String label = relationshipJson.getString("label");
                    TermExtractionResult.TermMention sourceTermMention = termMentions.get(columnIndex);
                    TermExtractionResult.TermMention destTermMention = termMentions.get(targetIndex);
                    relationships.add(new TermExtractionResult.Relationship(sourceTermMention, destTermMention, label));
                }
            }
        }
        return relationships;
    }

    private List<TermExtractionResult.TermMention> getTermsWithGraphVertices(int offset, List<String> columns, JSONObject mappingJson) throws JSONException, ParseException {
        List<TermExtractionResult.TermMention> termMentions = new ArrayList<TermExtractionResult.TermMention>();
        JSONArray mappingColumnsJson = (JSONArray) mappingJson.get(MappingProperties.COLUMNS);
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

                termMentions.add(new TermExtractionResult.TermMention(offset, offset + sign.length(), sign, ontologyClassUri, true, properties, null, useExisting));
            } else {
                termMentions.add(null);
            }
            offset = offset + sign.length() + 1;
        }
        return termMentions;
    }

    private Object getPropertyValue(JSONObject propertyMappingJson, String columnData) throws JSONException, ParseException {
        String dataType = propertyMappingJson.getString(MappingProperties.DATA_TYPE);
        if (columnData == null) {
            return dataType.equals(MappingProperties.DATE) ? new Date() : "";
        }
        if (dataType.equals(MappingProperties.DATE)) {
            return getPropertyValueDate(propertyMappingJson, columnData);
        } else if (dataType.equals(MappingProperties.GEO_LOCATION)) {
            String[] latlong = columnData.split(",");
            return Geoshape.point(Float.valueOf(latlong[0]), Float.valueOf(latlong[1]));
        } else {
            return columnData;
        }
    }

    private Object getPropertyValueDate(JSONObject propertyMappingJson, String columnData) throws JSONException, ParseException {
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

    @Inject
    public void setArtifactRepository(ArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }
}
