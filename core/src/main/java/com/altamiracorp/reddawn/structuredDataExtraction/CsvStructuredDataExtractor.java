package com.altamiracorp.reddawn.structuredDataExtraction;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;
import com.altamiracorp.reddawn.ucd.predicate.PredicateRowKey;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceData;
import com.altamiracorp.reddawn.ucd.sentence.SentenceMetadata;
import com.altamiracorp.reddawn.ucd.sentence.SentenceTerm;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import com.altamiracorp.reddawn.ucd.statement.StatementArtifact;
import com.altamiracorp.reddawn.ucd.statement.StatementRowKey;
import com.altamiracorp.reddawn.ucd.term.Term;
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
    public ExtractedData extract(Artifact artifact, String text, JSONObject mappingJson) throws IOException, JSONException {
        ExtractedData extractedData = new ExtractedData();

        int row = 0;
        int skipRows = mappingJson.getInt("skipRows");
        String securityMarking = "U";
        if (mappingJson.has("securityMarking")) {
            securityMarking = mappingJson.getString("securityMarking");
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
                Sentence sentence = createSentence(artifact, lastOffset, line, securityMarking);
                extractedData.addSentence(sentence);

                List<Term> terms = getTerms(artifact, sentence, columns, mappingJson);
                extractedData.addTerms(terms);

                List<Statement> statements = getStatements(artifact, sentence, terms, mappingJson);
                extractedData.addStatements(statements);
            }
            row++;
            lastOffset = reader.getOffset();
        }
        return extractedData;
    }

    private List<Term> getTerms(Artifact artifact, Sentence sentence, List<String> line, JSONObject mappingJson) throws JSONException {
        JSONArray mappingColumnsJson = (JSONArray) mappingJson.get("columns");
        ArrayList<Term> terms = new ArrayList<Term>();
        long offset = sentence.getRowKey().getStartOffset();
        for (int i = 0; i < line.size(); i++) {
            JSONObject columnMappingJson = mappingColumnsJson.getJSONObject(i);
            String sign = line.get(i);
            if (columnMappingJson.has("skip") && columnMappingJson.getBoolean("skip")) {
                terms.add(null);
            } else {
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
                terms.add(term);
            }

            offset += sign.length() + ",".length();
        }
        return terms;
    }


    private void setSecurityMarking(TermMention termMention, Sentence sentence) {
        String securityMarking = sentence.getMetadata().getSecurityMarking();
        if (securityMarking != null) {
            termMention.setSecurityMarking(sentence.getMetadata().getSecurityMarking());
        }
    }

    private Sentence createSentence(Artifact artifact, int startOffset, String line, String securityMarking) {
        Sentence sentence = new Sentence();
        SentenceData data = sentence.getData();
        data.setArtifactId(artifact.getRowKey().toString());
        data.setStart(Long.valueOf(startOffset));
        data.setEnd(Long.valueOf(startOffset + line.length()));
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

    public Date getDate() {
        return new Date();
    }

    private List<Statement> getStatements(Artifact artifact, Sentence sentence, List<Term> terms, JSONObject mappingJson) throws JSONException {
        List<Statement> statements = new ArrayList<Statement>();
        JSONArray mappingColumnsJson = (JSONArray) mappingJson.get("columns");
        for (int columnIndex = 0; columnIndex < mappingColumnsJson.length(); columnIndex++) {
            JSONObject mappingColumnJson = mappingColumnsJson.getJSONObject(columnIndex);
            if (!mappingColumnJson.has("relationships")) {
                continue;
            }
            JSONArray mappingRelationshipsJson = mappingColumnJson.getJSONArray("relationships");
            Term firstTerm = terms.get(columnIndex);
            for (int relationshipIndex = 0; relationshipIndex < mappingRelationshipsJson.length(); relationshipIndex++) {
                JSONObject mappingRelationshipJson = mappingRelationshipsJson.getJSONObject(relationshipIndex);
                statements.add(createStatement(artifact, sentence, firstTerm, terms, mappingRelationshipJson));
            }
        }
        return statements;
    }

    private Statement createStatement(Artifact artifact, Sentence sentence, Term firstTerm, List<Term> terms, JSONObject mappingRelationshipJson) throws JSONException {
        String predicateModelKey = mappingRelationshipJson.getString("predicateModelKey");
        String predicateLabel = mappingRelationshipJson.getString("predicateLabel");
        int targetColumn = mappingRelationshipJson.getInt("target");
        Term secondTerm = terms.get(targetColumn);

        Statement statement = new Statement(
                new StatementRowKey(firstTerm.getRowKey(),
                        new PredicateRowKey(predicateModelKey, predicateLabel),
                        secondTerm.getRowKey())
        );

        StatementArtifact statementArtifact = new StatementArtifact()
                .setArtifactKey(artifact.getRowKey().toString())
                .setAuthor(EXTRACTOR_ID)
                .setDate(getDate().getTime())
                .setExtractorId(EXTRACTOR_ID)
                .setSecurityMarking(sentence.getMetadata().getSecurityMarking())
                .setSentence(sentence.getRowKey().toString())
                .setSentenceText(sentence.getData().getText())
                .setArtifactSubject(sentence.getMetadata().getArtifactSubject())
                .setArtifactType(ArtifactType.valueOf(sentence.getMetadata().getArtifactType()));
        statement.addStatementArtifact(statementArtifact);

        return statement;
    }
}
