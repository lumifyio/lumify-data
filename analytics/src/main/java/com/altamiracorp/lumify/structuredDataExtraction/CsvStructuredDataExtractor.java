package com.altamiracorp.lumify.structuredDataExtraction;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.ontology.VertexType;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.textExtraction.ArtifactExtractedInfo;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.util.LineReader;
import com.google.inject.Inject;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CsvStructuredDataExtractor extends StructuredDataExtractorBase {
    private Map<String, SimpleDateFormat> dateFormatCache = new HashMap<String, SimpleDateFormat>();

    private final ArtifactRepository artifactRepository;

    @Inject
    public CsvStructuredDataExtractor(ArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }

    @Override
    public ArtifactExtractedInfo extractText(Artifact artifact, User user) throws Exception {
        JSONObject mappingJson = artifact.getGenericMetadata().getMappingJson();
        InputStream raw = artifactRepository.getRaw(artifact, user);
        try {
            StringWriter writer = new StringWriter();
            CsvPreference csvPrefs = CsvPreference.EXCEL_PREFERENCE;
            CsvListReader csvReader = new CsvListReader(new InputStreamReader(raw), csvPrefs);
            CsvListWriter csvWriter = new CsvListWriter(writer, csvPrefs);
            List<String> line;
            while ((line = csvReader.read()) != null) {
                csvWriter.write(line);
            }
            csvWriter.close();

            ArtifactExtractedInfo extractedInfo = new ArtifactExtractedInfo();
            extractedInfo.setText(writer.toString());
            if (mappingJson.has("subject")) {
                artifact.getGenericMetadata().setSubject(mappingJson.getString("subject"));
            }
            return extractedInfo;
        } finally {
            raw.close();
        }
    }

    @Override
    public ExtractedData extract(Artifact artifact, String text, JSONObject mappingJson, User user) throws IOException, JSONException, ParseException {
        ExtractedData extractedData = new ExtractedData();

        int row = 0;
        int skipRows = mappingJson.getInt("skipRows");

        CsvPreference csvPrefs = CsvPreference.EXCEL_PREFERENCE;
        LineReader reader = new LineReader(new StringReader(text));
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
                processLine(extractedData, artifact, lastOffset, columns, allGraphVertex, mappingJson);
            }
            row++;
            lastOffset = reader.getOffset();
        }
        return extractedData;
    }

    private void processLine(ExtractedData extractedData, Artifact artifact, int offset, List<String> columns, Map<String, GraphVertex> allGraphVertex, JSONObject mappingJson) throws JSONException, ParseException {
        List<TermAndGraphVertex> termsAndGraphVertices = getTermsAndGraphVertices(artifact, offset, columns, allGraphVertex, mappingJson);
        extractedData.addTermAndGraphVertex(termsAndGraphVertices);

        List<StructuredDataRelationship> relationships = getRelationships(termsAndGraphVertices, mappingJson);
        extractedData.addRelationships(relationships);
    }

    private List<TermAndGraphVertex> getTermsAndGraphVertices(Artifact artifact, int offset, List<String> line, Map<String, GraphVertex> allGraphVertex, JSONObject mappingJson) throws JSONException, ParseException {
        List<TermAndGraphVertex> termsAndGraphVertices = new ArrayList<TermAndGraphVertex>();
        JSONArray mappingColumnsJson = (JSONArray) mappingJson.get("columns");
        for (int i = 0; i < line.size(); i++) {
            JSONObject columnMappingJson = mappingColumnsJson.getJSONObject(i);
            String sign = line.get(i);
            sign = sign == null ? "" : sign;
            String type = columnMappingJson.getString("type");
            TermAndGraphVertex termAndGraphVertex = null;
            if (type.equals("term")) {
                termAndGraphVertex = createTermAndGraphVertex(artifact, offset, sign, allGraphVertex, columnMappingJson);

                if (columnMappingJson.has("properties")) {
                    JSONArray propertiesMappingJson = columnMappingJson.getJSONArray("properties");
                    for (int propIndex = 0; propIndex < propertiesMappingJson.length(); propIndex++) {
                        JSONObject propertyMappingJson = propertiesMappingJson.getJSONObject(propIndex);
                        int target = propertyMappingJson.getInt("target");
                        String name = propertyMappingJson.getString("name");
                        JSONObject targetPropertyMappingJson = mappingColumnsJson.getJSONObject(target);
                        String columnData = line.get(target);
                        Object propertyValue = getPropertyValue(targetPropertyMappingJson, columnData);
                        termAndGraphVertex.getGraphVertex().setProperty(name, propertyValue);
                    }
                }
            }
            termsAndGraphVertices.add(termAndGraphVertex);

            offset += sign.length() + ",".length();
        }

        return termsAndGraphVertices;
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

    private TermAndGraphVertex createTermAndGraphVertex(Artifact artifact, int offset, String sign, Map<String, GraphVertex> allGraphVertex, JSONObject columnMappingJson) throws JSONException {
        TermAndGraphVertex termAndGraphVertex;
        String conceptLabel = columnMappingJson.getString("conceptLabel");
        boolean useExisting = columnMappingJson.optBoolean("useExisting", false);

        // TODO these offsets need to be fixed. If the csv file has quotes or other characters which CsvListReader removes this will be wrong
        int termMentionStart = offset;
        int termMentionEnd = offset + sign.length();

        TermMentionRowKey termMentionRowKey = new TermMentionRowKey(artifact.getRowKey().toString(), termMentionStart, termMentionEnd);
        TermMention termMention = new TermMention(termMentionRowKey);
        termMention.getMetadata()
                .setSign(artifact.getGenericMetadata().getSubject())
                .setSign(sign)
                .setConcept(conceptLabel);


        GraphVertex vertex = allGraphVertex.get(sign);
        if (vertex == null) {
            vertex = new InMemoryGraphVertex();
            vertex.setProperty(PropertyName.TYPE.toString(), VertexType.ENTITY.toString());
            vertex.setProperty(PropertyName.ROW_KEY.toString(), termMention.getRowKey().toString());
            vertex.setProperty(PropertyName.TITLE.toString(), sign);
            vertex.setProperty(PropertyName.SOURCE.toString(), artifact.getGenericMetadata().getSubject() == null ? "" : artifact.getGenericMetadata().getSubject());
            allGraphVertex.put(sign, vertex);
        }

        termAndGraphVertex = new TermAndGraphVertex(termMention, vertex, useExisting);
        return termAndGraphVertex;
    }

    private List<StructuredDataRelationship> getRelationships(List<TermAndGraphVertex> termAndGraphVertexes, JSONObject mappingJson) throws JSONException {
        List<StructuredDataRelationship> relationships = new ArrayList<StructuredDataRelationship>();
        JSONArray mappingColumnsJson = (JSONArray) mappingJson.get("columns");
        for (int columnIndex = 0; columnIndex < mappingColumnsJson.length(); columnIndex++) {
            JSONObject mappingColumnJson = mappingColumnsJson.getJSONObject(columnIndex);
            if (!mappingColumnJson.has("relationships")) {
                continue;
            }
            JSONArray mappingRelationshipsJson = mappingColumnJson.getJSONArray("relationships");
            TermAndGraphVertex firstTermAndGraphVertex = termAndGraphVertexes.get(columnIndex);
            for (int relationshipIndex = 0; relationshipIndex < mappingRelationshipsJson.length(); relationshipIndex++) {
                JSONObject mappingRelationshipJson = mappingRelationshipsJson.getJSONObject(relationshipIndex);
                int targetColumn = mappingRelationshipJson.getInt("target");
                TermAndGraphVertex secondTermAndGraphVertex = termAndGraphVertexes.get(targetColumn);
                relationships.add(createRelationship(firstTermAndGraphVertex, secondTermAndGraphVertex, mappingRelationshipJson));
            }
        }
        return relationships;
    }

    private StructuredDataRelationship createRelationship(TermAndGraphVertex firstTerm, TermAndGraphVertex secondTerm, JSONObject mappingRelationshipJson) throws JSONException {
        String label = mappingRelationshipJson.getString("label");
        return new StructuredDataRelationship(firstTerm, secondTerm, label);
    }
}
