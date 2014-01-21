/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.structuredData.mapping;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.storm.structuredData.mapping.csv.CsvDocumentMapping;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * An interface for DocumentMappings.
 */
@JsonTypeInfo(include=As.PROPERTY, property="type", use=Id.NAME)
@JsonSubTypes({
    @Type(CsvDocumentMapping.class)
})
public interface DocumentMapping {
    /**
     * Get the subject of the document.  This will be used as the
     * document title on ingest.
     * @return the document subject or an empty string if none is provided
     */
    @JsonProperty("subject")
    String getSubject();

    /**
     * Read the contents of a document that is the target of this mapping and
     * write them to the provided OutputStream in a format that can later be
     * processed by this mapping.
     * @param inputDoc the document to read
     * @param outputDoc the writer to write the document to
     * @throws IOException if errors occur while reading or writing the document
     */
    void ingestDocument(final InputStream inputDoc, final Writer outputDoc) throws IOException;

    /**
     * Execute this mapping against the provided document, extracting all Term mentions
     * and relationships found.
     * @param inputDoc the document to read; typically reading from output Writer provided to
     * <code>ingestDocument()</code>
     * @param processId the ID of the process reading this document
     * @return the Term mentions and relationships found in the provided document as indicated by
     * this mapping
     * @throws IOException if an error occurs while applying the mapping
     */
    TermExtractionResult mapDocument(final Reader inputDoc, final String processId) throws IOException;
}
