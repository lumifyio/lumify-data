/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.structuredData.mapping.csv;

import static com.google.common.base.Preconditions.*;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermRelationship;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.storm.structuredData.mapping.DocumentMapping;
import com.altamiracorp.lumify.util.LineReader;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

/**
 * DocumentMapping for CSV files.
 */
@JsonTypeName("csv")
public class CsvDocumentMapping implements DocumentMapping {
    /**
     * The class logger.
     */
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(CsvDocumentMapping.class);

    /**
     * The CsvPreference for reading and writing.
     */
    private static final CsvPreference CSV_PREFERENCE = CsvPreference.EXCEL_PREFERENCE;

    /**
     * The CSV document mapping type.
     */
    public static final String CSV_DOCUMENT_TYPE = "csv";

    /**
     * The default subject.
     */
    public static final String DEFAULT_SUBJECT = "";

    /**
     * The default number of rows to skip.
     */
    public static final int DEFAULT_SKIP_ROWS = 0;

    /**
     * The number of rows to skip.
     */
    private final int skipRows;

    /**
     * The subject for this mapping.
     */
    private final String subject;

    /**
     * The term mappings for this CSV.
     */
    private final SortedSet<TermMapping> termMappings;

    /**
     * The relationship mappings for this CSV.
     */
    private final List<CsvRelationshipMapping> relationshipMappings;

    /**
     * Create a new CsvDocumentMapping.
     * @param subject
     * @param skipRows
     * @param terms
     * @param relationships
     */
    @JsonCreator
    public CsvDocumentMapping(@JsonProperty("subject") final String subject,
            @JsonProperty(value="skipRows",required=false) final Integer skipRows,
            @JsonProperty("terms") final Map<String, CsvTermColumnMapping> terms,
            @JsonProperty(value="relationships", required=false) final List<CsvRelationshipMapping> relationships) {
        checkArgument(subject != null && !subject.trim().isEmpty(), "Subject must be provided.");
        checkArgument(skipRows == null || skipRows >= 0, "skipRows must be >= 0 if provided.");
        checkNotNull(terms, "At least one term mapping must be provided.");
        checkArgument(!terms.isEmpty(), "At least one term mapping must be provided.");
        this.subject = subject != null ? subject.trim() : DEFAULT_SUBJECT;
        this.skipRows = skipRows != null && skipRows >= 0 ? skipRows : DEFAULT_SKIP_ROWS;
        SortedSet<TermMapping> myTerms = new TreeSet<TermMapping>();
        for (Map.Entry<String, CsvTermColumnMapping> entry : terms.entrySet()) {
            myTerms.add(new TermMapping(entry.getKey(), entry.getValue()));
        }
        this.termMappings = Collections.unmodifiableSortedSet(myTerms);
        List<CsvRelationshipMapping> myRels = new ArrayList<CsvRelationshipMapping>();
        if (relationships != null) {
            myRels.addAll(relationships);
        }
        this.relationshipMappings = Collections.unmodifiableList(myRels);
    }

    /**
     * Get the number of rows to skip.
     * @return the number of rows to skip
     */
    @JsonProperty("skipRows")
    public int getSkipRows() {
        return skipRows;
    }

    @JsonProperty("subject")
    @Override
    public String getSubject() {
        return subject;
    }

    @JsonProperty("terms")
    public Map<String, CsvTermColumnMapping> getTerms() {
        Map<String, CsvTermColumnMapping> map = new HashMap<String, CsvTermColumnMapping>();
        for (TermMapping tm : termMappings) {
            map.put(tm.getKey(), tm.getMapping());
        }
        return Collections.unmodifiableMap(map);
    }

    @JsonProperty("relationships")
    public List<CsvRelationshipMapping> getRelationships() {
        return relationshipMappings;
    }

    @Override
    public void ingestDocument(final InputStream inputDoc, final Writer outputDoc) throws IOException {
        CsvListReader csvReader = new CsvListReader(new InputStreamReader(inputDoc), CsvDocumentMapping.CSV_PREFERENCE);
        CsvListWriter csvWriter = new CsvListWriter(outputDoc, CsvDocumentMapping.CSV_PREFERENCE);

        List<String> line;
        while ((line = csvReader.read()) != null) {
            csvWriter.write(line);
        }
        csvWriter.close();
    }

    @Override
    public TermExtractionResult mapDocument(final Reader reader, final String processId) throws IOException {
        // read line-by-line, tracking the offset
        LineReader lineReader = new LineReader(reader);
        // skip the first skipRows lines and initialize the current offset
        lineReader.skipLines(skipRows);
        int offset = lineReader.getOffset();

        TermExtractionResult results = new TermExtractionResult();
        CsvListReader csvReader;
        List<String> columns;
        Map<String, TermMention> termMap;
        TermMention mention;
        TermMention tgtMention;
        int lastCol;
        int currentCol;
        boolean skipLine;
        for (String line = lineReader.readLine(); line != null && !line.isEmpty(); line = lineReader.readLine()) {
            csvReader = new CsvListReader(new StringReader(line), CsvDocumentMapping.CSV_PREFERENCE);
            columns = csvReader.read();
            if (columns == null) {
                break;
            }
            // extract all identified Terms, adding them to the results and
            // mapping them by the configured map ID for relationship discovery
            List<TermMention> mentions = new ArrayList<TermMention>();
            termMap = new HashMap<String, TermMention>();
            lastCol = 0;
            skipLine = false;
            for (TermMapping termMapping : termMappings) {
                CsvTermColumnMapping colMapping = termMapping.getMapping();
                // term mappings are ordered by column number; update offset
                // so it is set to the start of the column for the current term
                currentCol = colMapping.getColumnIndex();
                for (/* no precondition */; lastCol < currentCol; lastCol++) {
                    offset += (columns.get(lastCol) != null ? columns.get(lastCol).length() : 0) + 1;
                }
                try {
                    mention = colMapping.mapTerm(columns, offset, processId);
                    if (mention != null) {
                        // no need to update offset here, it will get updated by the block
                        // above when the next term is processed or, if this is the last term,
                        // it will be set to the proper offset for the next line
                        termMap.put(termMapping.getKey(), mention);
                        mentions.add(mention);
                    }
                } catch (Exception e) {
                    if (colMapping.isRequired()) {
                        // skip line
                        skipLine = true;
                        break;
                    }
                }
            }
            if (!skipLine) {
                // parse all configured relationships, generating the relationship only
                // if both Terms were successfully extracted
                List<TermRelationship> relationships = new ArrayList<TermRelationship>();
                for (CsvRelationshipMapping relMapping : relationshipMappings) {
                    mention = termMap.get(relMapping.getSourceTermId());
                    tgtMention = termMap.get(relMapping.getTargetTermId());
                    if (mention != null && tgtMention != null) {
                        relationships.add(new TermRelationship(mention, tgtMention, relMapping.getLabel()));
                    }
                }
                results.addAllTermMentions(mentions);
                results.addAllRelationships(relationships);
            }
            offset = lineReader.getOffset();
        }
        return results;
    }

    private static class TermMapping implements Comparable<TermMapping> {
        private final String key;
        private final CsvTermColumnMapping mapping;

        public TermMapping(String key, CsvTermColumnMapping mapping) {
            this.key = key;
            this.mapping = mapping;
        }

        public String getKey() {
            return key;
        }

        public CsvTermColumnMapping getMapping() {
            return mapping;
        }

        @Override
        public int compareTo(final TermMapping o) {
            return this.mapping.compareTo(o.mapping);
        }
    }
}
