package com.altamiracorp.reddawn.structuredDataExtraction;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.graph.GraphVertexImpl;
import com.altamiracorp.reddawn.model.ontology.PropertyName;
import com.altamiracorp.reddawn.model.ontology.VertexType;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceData;
import com.altamiracorp.reddawn.ucd.sentence.SentenceMetadata;
import com.altamiracorp.reddawn.ucd.sentence.SentenceTerm;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermAndTermMention;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import com.altamiracorp.reddawn.util.LineReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CsvStructuredDataExtractor extends StructuredDataExtractorBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvStructuredDataExtractor.class.getName());
    private static final String EXTRACTOR_ID = "CsvStructuredData";

    @Override
    public ExtractedData extract(RedDawnSession session, Artifact artifact, String text, JSONObject mappingJson) throws IOException, JSONException {
        ExtractedData extractedData = new ExtractedData();

        int row = 0;
        int skipRows = mappingJson.getInt("skipRows");
        String securityMarking = "U";
        if (mappingJson.has("securityMarking")) {
            securityMarking = mappingJson.getString("securityMarking");
        }

        if (mappingJson.has("subject")) {
            artifact.getGenericMetadata().setSubject(mappingJson.getString("subject"));
        }

        CsvPreference csvPrefs = CsvPreference.EXCEL_PREFERENCE;
        LineReader reader = new LineReader(new StringReader(text));
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
                processLine(extractedData, artifact, lastOffset, line, columns, securityMarking, mappingJson);
            }
            row++;
            lastOffset = reader.getOffset();
        }
        return extractedData;
    }

    private void processLine(ExtractedData extractedData, Artifact artifact, int offset, String line, List<String> columns, String securityMarking, JSONObject mappingJson) throws JSONException {
        Sentence sentence = createSentence(artifact, offset, line, securityMarking);
        extractedData.addSentence(sentence);

        List<TermAndGraphVertex> termsAndGraphVertices = getTermsAndGraphVertices(artifact, sentence, columns, mappingJson);
        extractedData.addTermAndGraphVertex(termsAndGraphVertices);

        List<StructuredDataRelationship> relationships = getRelationships(artifact, sentence, termsAndGraphVertices, mappingJson);
        extractedData.addRelationships(relationships);
    }

    private Sentence createSentence(Artifact artifact, int startOffset, String line, String securityMarking) {
        Sentence sentence = new Sentence();
        SentenceData data = sentence.getData();
        data.setArtifactId(artifact.getRowKey().toString());
        data.setStart((long) startOffset);
        data.setEnd((long) (startOffset + line.length()));
        data.setText(line);

        SentenceMetadata metaData = sentence.getMetadata();
        metaData.setContentHash(data.getText())
                .setDate(getDate().getTime())
                .setExtractorId(EXTRACTOR_ID)
                .setArtifactSubject(artifact.getGenericMetadata().getSubject())
                .setArtifactType(artifact.getType())
                .setSecurityMarking(securityMarking);
        return sentence;
    }

    private Date getDate() {
        return new Date();
    }

    private List<TermAndGraphVertex> getTermsAndGraphVertices(Artifact artifact, Sentence sentence, List<String> line, JSONObject mappingJson) throws JSONException {
        List<TermAndGraphVertex> termsAndGraphVertices = new ArrayList<TermAndGraphVertex>();
        JSONArray mappingColumnsJson = (JSONArray) mappingJson.get("columns");
        long offset = sentence.getRowKey().getStartOffset();
        for (int i = 0; i < line.size(); i++) {
            JSONObject columnMappingJson = mappingColumnsJson.getJSONObject(i);
            String sign = line.get(i);
            String type = columnMappingJson.getString("type");
            TermAndGraphVertex termAndGraphVertex = null;
            if (type.equals("term")) {
                termAndGraphVertex = createTermAndGraphVertex(artifact, offset, sentence, sign, columnMappingJson);

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

    private Object getPropertyValue(JSONObject propertyMappingJson, String columnData) {
        // TODO: convert data types
        return columnData;
    }

    private TermAndGraphVertex createTermAndGraphVertex(Artifact artifact, long offset, Sentence sentence, String sign, JSONObject columnMappingJson) throws JSONException {
        TermAndGraphVertex termAndGraphVertex;
        String modelKey = columnMappingJson.getString("modelKey");
        String conceptLabel = columnMappingJson.getString("conceptLabel");

        // TODO these offsets need to be fixed. If the csv file has quotes or other characters which CsvListReader removes this will be wrong
        Long termMentionStart = offset;
        Long termMentionEnd = offset + sign.length();

        TermRowKey termKey = new TermRowKey(sign, modelKey, conceptLabel);
        TermMention termMention = new TermMention()
                .setArtifactKey(artifact.getRowKey().toString())
                .setArtifactKeySign(artifact.getRowKey().toString())
                .setAuthor(EXTRACTOR_ID)
                .setMentionStart(termMentionStart)
                .setMentionEnd(termMentionEnd)
                .setSentenceText(sentence.getData().getText())
                .setSentenceTokenOffset(sentence.getRowKey().getStartOffset())
                .setArtifactSubject(sentence.getMetadata().getArtifactSubject())
                .setArtifactType(sentence.getMetadata().getArtifactType());
        setSecurityMarking(termMention, sentence);
        Term term = new Term(termKey)
                .addTermMention(termMention);
        SentenceTerm sentenceTerm = new SentenceTerm(termMention)
                .setTermId(term);
        sentence.addSentenceTerm(sentenceTerm);

        GraphVertex vertex = new GraphVertexImpl();
        vertex.setProperty(PropertyName.TYPE.toString(), VertexType.TERM_MENTION.toString());
        vertex.setProperty(PropertyName.ROW_KEY.toString(), term.getRowKey().toString());
        vertex.setProperty(PropertyName.COLUMN_FAMILY_NAME.toString(), termMention.getColumnFamilyName());
        vertex.setProperty(PropertyName.TITLE.toString(), term.getRowKey().getSign());
        vertex.setProperty(PropertyName.SOURCE.toString(), termMention.getArtifactSubject() == null ? "" : termMention.getArtifactSubject());

        termAndGraphVertex = new TermAndGraphVertex(new TermAndTermMention(term, termMention), vertex);
        return termAndGraphVertex;
    }

    private void setSecurityMarking(TermMention termMention, Sentence sentence) {
        String securityMarking = sentence.getMetadata().getSecurityMarking();
        if (securityMarking != null) {
            termMention.setSecurityMarking(sentence.getMetadata().getSecurityMarking());
        }
    }

    private List<StructuredDataRelationship> getRelationships(Artifact artifact, Sentence sentence, List<TermAndGraphVertex> termAndGraphVertexes, JSONObject mappingJson) throws JSONException {
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
