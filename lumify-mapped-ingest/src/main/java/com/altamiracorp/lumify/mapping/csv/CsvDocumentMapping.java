/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.csv;

import static com.google.common.base.Preconditions.*;

import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.mapping.column.AbstractColumnDocumentMapping;
import com.altamiracorp.lumify.mapping.column.ColumnEntityMapping;
import com.altamiracorp.lumify.mapping.column.ColumnRelationshipMapping;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

/**
 * DocumentMapping for CSV files.
 */
@JsonTypeName("csv")
public class CsvDocumentMapping extends AbstractColumnDocumentMapping {
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
     * The default number of rows to skip.
     */
    public static final int DEFAULT_SKIP_ROWS = 0;

    /**
     * The number of rows to skip.
     */
    private final int skipRows;

    /**
     * Create a new CsvDocumentMapping.
     * @param subject the subject for the ingested Document
     * @param skipRows the number of rows to skip
     * @param entities the entity mappings
     * @param relationships the relationship mappings
     */
    @JsonCreator
    public CsvDocumentMapping(@JsonProperty("subject") final String subject,
            @JsonProperty(value="skipRows",required=false) final Integer skipRows,
            @JsonProperty("entities") final Map<String, ColumnEntityMapping> entities,
            @JsonProperty(value="relationships", required=false) final List<ColumnRelationshipMapping> relationships) {
        super(subject, entities, relationships);
        checkArgument(skipRows == null || skipRows >= 0, "skipRows must be >= 0 if provided.");
        this.skipRows = skipRows != null && skipRows >= 0 ? skipRows : DEFAULT_SKIP_ROWS;
    }

    /**
     * Get the number of rows to skip.
     * @return the number of rows to skip
     */
    @JsonProperty("skipRows")
    public int getSkipRows() {
        return skipRows;
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
    protected Iterable<Row> getRows(final Reader reader) throws IOException {
        return new RowIterable(new CsvRowIterator(reader));
    }

    private class CsvRowIterator implements Iterator<Row> {
        /**
         * The LineReader.
         */
        private final LineReader lineReader;

        /**
         * The cached next row.
         */
        private Row nextRow;

        /**
         * Create a new CsvRowIterator.
         * @param reader the input document
         */
        public CsvRowIterator(final Reader reader) throws IOException {
            lineReader = new LineReader(reader);
            lineReader.skipLines(skipRows);
            // initialize nextRow so hasNext() returns properly
            advanceLine();
        }

        private void advanceLine() {
            try {
                // get offset for the start of the next line
                int offset = lineReader.getOffset();
                // read the next line
                String line = lineReader.readLine();
                if (line != null) {
                    List<String> columns = new CsvListReader(new StringReader(line), CsvDocumentMapping.CSV_PREFERENCE).read();
                    nextRow = new Row(offset, columns);
                } else {
                    nextRow = null;
                }
            } catch (IOException ioe) {
                LOGGER.error("Error reading input CSV.", ioe);
                nextRow = null;
            }
        }

        @Override
        public boolean hasNext() {
            return nextRow != null;
        }

        @Override
        public Row next() {
            if (nextRow == null) {
                throw new NoSuchElementException("No more lines in input document.");
            }
            Row row = nextRow;
            advanceLine();
            return row;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Rows cannot be removed.");
        }
    }
}
