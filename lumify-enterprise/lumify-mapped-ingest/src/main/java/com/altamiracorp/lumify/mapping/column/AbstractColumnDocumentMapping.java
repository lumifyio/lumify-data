/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.column;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermRelationship;
import com.altamiracorp.lumify.mapping.DocumentMapping;
import com.altamiracorp.securegraph.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base class for Columnar document mappings.
 */
public abstract class AbstractColumnDocumentMapping implements DocumentMapping {
    /**
     * The subject for this mapping.
     */
    private final String subject;

    /**
     * The entity mappings for this CSV.
     */
    private final SortedSet<EntityMapping> entityMappings;

    /**
     * The relationship mappings for this CSV.
     */
    private final List<ColumnRelationshipMapping> relationshipMappings;

    /**
     * Create a new AbstractColumnDocumentMapping.
     *
     * @param subject       the subject for the ingested Document
     * @param entities      the entity mappings
     * @param relationships the relationship mappings
     */
    protected AbstractColumnDocumentMapping(final String subject, final Map<String, ColumnEntityMapping> entities,
                                            final List<ColumnRelationshipMapping> relationships) {
        checkArgument(subject != null && !subject.trim().isEmpty(), "Subject must be provided.");
        checkNotNull(entities, "At least one entity mapping must be provided.");
        checkArgument(!entities.isEmpty(), "At least one entity mapping must be provided.");
        this.subject = subject.trim();
        SortedSet<EntityMapping> myEntities = new TreeSet<EntityMapping>();
        for (Map.Entry<String, ColumnEntityMapping> entry : entities.entrySet()) {
            myEntities.add(new EntityMapping(entry.getKey(), entry.getValue()));
        }
        this.entityMappings = Collections.unmodifiableSortedSet(myEntities);
        List<ColumnRelationshipMapping> myRels = new ArrayList<ColumnRelationshipMapping>();
        if (relationships != null) {
            myRels.addAll(relationships);
        }
        this.relationshipMappings = Collections.unmodifiableList(myRels);
    }

    @JsonProperty("subject")
    @Override
    public final String getSubject() {
        return subject;
    }

    @JsonProperty("entities")
    public final Map<String, ColumnEntityMapping> getEntities() {
        Map<String, ColumnEntityMapping> map = new HashMap<String, ColumnEntityMapping>();
        for (EntityMapping tm : entityMappings) {
            map.put(tm.getKey(), tm.getMapping());
        }
        return Collections.unmodifiableMap(map);
    }

    @JsonProperty("relationships")
    public final List<ColumnRelationshipMapping> getRelationships() {
        return relationshipMappings;
    }

    /**
     * Get an Iterable that returns the rows of the input document
     * that will be processed.
     *
     * @param reader a Reader over the input document
     * @return an Iterable over the rows of the input document
     */
    protected abstract Iterable<Row> getRows(final Reader reader) throws IOException;

    @Override
    public final TermExtractionResult mapDocument(final Reader reader, final String processId, String propertyKey, Visibility visibility) throws IOException {
        TermExtractionResult results = new TermExtractionResult();
        Iterable<Row> rows = getRows(reader);
        for (Row row : rows) {
            int offset = row.getOffset();
            List<String> columns = row.getColumns();
            Map<String, TermMention> termMap;
            TermMention mention;
            TermMention tgtMention;
            int lastCol;
            int currentCol;
            boolean skipLine;
            // if columns are null, stop processing
            if (columns == null) {
                break;
            }
            // extract all identified Terms, adding them to the results and
            // mapping them by the configured map ID for relationship discovery
            List<TermMention> mentions = new ArrayList<TermMention>();
            termMap = new HashMap<String, TermMention>();
            lastCol = 0;
            skipLine = false;
            for (EntityMapping termMapping : entityMappings) {
                ColumnEntityMapping colMapping = termMapping.getMapping();
                // term mappings are ordered by column number; update offset
                // so it is set to the start of the column for the current term
                currentCol = colMapping.getSortColumn();
                for (/* no precondition */; lastCol < currentCol; lastCol++) {
                    offset += (columns.get(lastCol) != null ? columns.get(lastCol).length() : 0) + 1;
                }
                try {
                    mention = colMapping.mapTerm(columns, offset, processId, propertyKey, visibility);
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
                for (ColumnRelationshipMapping relMapping : relationshipMappings) {
                    TermRelationship rel = relMapping.createRelationship(termMap, columns, visibility);
                    if (rel != null) {
                        relationships.add(rel);
                    }
                }
                results.addAllTermMentions(mentions);
                results.addAllRelationships(relationships);
            }
        }
        return results;
    }

    private static class EntityMapping implements Comparable<EntityMapping> {
        private final String key;
        private final ColumnEntityMapping mapping;

        public EntityMapping(String key, ColumnEntityMapping mapping) {
            this.key = key;
            this.mapping = mapping;
        }

        public String getKey() {
            return key;
        }

        public ColumnEntityMapping getMapping() {
            return mapping;
        }

        @Override
        public int compareTo(final EntityMapping o) {
            return this.mapping.compareTo(o.mapping);
        }
    }

    public static final class Row {
        private final int offset;
        private final List<String> columns;

        public Row(final int offset, final List<String> columns) {
            this.offset = offset;
            this.columns = Collections.unmodifiableList(new ArrayList<String>(columns));
        }

        public int getOffset() {
            return offset;
        }

        public List<String> getColumns() {
            return columns;
        }

        @Override
        public String toString() {
            return String.format("[%d]: %s", offset, columns);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 13 * hash + this.offset;
            hash = 13 * hash + (this.columns != null ? this.columns.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Row other = (Row) obj;
            if (this.offset != other.offset) {
                return false;
            }
            if (this.columns != other.columns && (this.columns == null || !this.columns.equals(other.columns))) {
                return false;
            }
            return true;
        }
    }

    protected static class RowIterable implements Iterable<Row> {
        private Iterator<Row> iterator;

        public RowIterable(final Iterator<Row> iter) {
            this.iterator = iter;
        }

        @Override
        public Iterator<Row> iterator() {
            return iterator;
        }
    }
}
