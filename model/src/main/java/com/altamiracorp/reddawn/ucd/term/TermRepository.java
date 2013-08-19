package com.altamiracorp.reddawn.ucd.term;

import com.altamiracorp.reddawn.model.*;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.graph.GraphVertexImpl;
import com.altamiracorp.reddawn.model.graph.GraphRelationship;
import com.altamiracorp.reddawn.model.ontology.PropertyName;
import com.altamiracorp.reddawn.model.ontology.VertexType;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndex;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndexRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class TermRepository extends Repository<Term> {
    private ArtifactTermIndexRepository artifactTermIndexRepository = new ArtifactTermIndexRepository();

    @Override
    public Term fromRow(Row row) {
        Term term = new Term(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().startsWith("urn")) {
                Collection<Column> columns = columnFamily.getColumns();
                term.addColumnFamily(new TermMention(columnFamily.getColumnFamilyName()).addColumns(columns));
            } else {
                term.addColumnFamily(columnFamily);
            }
        }
        return term;
    }

    @Override
    public Row toRow(Term term) {
        return term;
    }

    @Override
    public String getTableName() {
        return Term.TABLE_NAME;
    }

    public Collection<Term> findByArtifactRowKey(Session session, String artifactRowKey) {
        ArrayList<Term> terms = new ArrayList<Term>();
        ArtifactTermIndex artifactTermIndex = artifactTermIndexRepository.findByRowKey(session, artifactRowKey);
        if (artifactTermIndex == null) {
            return terms;
        }
        for (TermRowKey termRowKey : artifactTermIndex.getTermMentions()) {
            Term term = findByRowKey(session, termRowKey.toString());
            if (term == null) {
                throw new RuntimeException("Could not find term with id " + termRowKey.toString());
            }
            terms.add(term);
        }
        return terms;
    }

    public List<ColumnFamily> findMentions(Session session, String rowKey, long colFamOffset, long colFamLimit) {
        return findByRowKeyWithColumnFamilyRegexOffsetAndLimit(session, rowKey, colFamOffset, colFamLimit, "urn.*");
    }

    @Override
    public void save(Session session, Term term) {
        super.save(session, term);

        HashMap<String, ArtifactTermIndex> artifactTermIndexHashMap = new HashMap<String, ArtifactTermIndex>();
        for (TermMention termMention : term.getTermMentions()) {
            String artifactKey = termMention.getArtifactKey();
            ArtifactTermIndex artifactTermIndex = artifactTermIndexHashMap.get(artifactKey);
            if (artifactTermIndex == null) {
                artifactTermIndex = new ArtifactTermIndex(artifactKey);
                artifactTermIndexHashMap.put(artifactKey, artifactTermIndex);
            }
            artifactTermIndex.addTermMention(term.getRowKey(), termMention);
        }

        for (ArtifactTermIndex artifactTermIndex : artifactTermIndexHashMap.values()) {
            artifactTermIndexRepository.save(session, artifactTermIndex);
        }
    }

    @Override
    public void saveMany(Session session, Collection<Term> terms) {
        for (Term term : terms) {
            if (term == null) {
                continue;
            }
            save(session, term);
        }
    }

    public void saveToGraph(Session session, GraphSession graphSession, Term term, TermMention termMention, String conceptId) {
        String oldGraphVertexId = termMention.getGraphVertexId();
        GraphVertex vertex = new GraphVertexImpl();
        vertex.setProperty(PropertyName.TYPE.toString(), VertexType.TERM_MENTION.toString());
        vertex.setProperty(PropertyName.SUBTYPE.toString(), conceptId);
        vertex.setProperty(PropertyName.ROW_KEY.toString(), term.getRowKey().toString());
        vertex.setProperty(PropertyName.COLUMN_FAMILY_NAME.toString(), termMention.getColumnFamilyName());
        vertex.setProperty(PropertyName.TITLE.toString(), term.getRowKey().getSign());
        vertex.setProperty(PropertyName.SOURCE.toString(), termMention.getArtifactSubject() == null ? "" : termMention.getArtifactSubject());

        String vertexId = graphSession.save(vertex);
        if (!vertexId.equals(oldGraphVertexId) || !termMention.getGraphSubTypeVertexeId().equals(conceptId)) {
            termMention.setGraphSubTypeVertexId(conceptId);
            termMention.setGraphVertexId(vertexId);
            this.save(session, term);
        }

        List<GraphVertex> artifactVertices = graphSession.findBy(PropertyName.ROW_KEY.toString(), termMention.getArtifactKey());
        if (artifactVertices.size() == 0) {
            throw new RuntimeException("Could not find artifact \"" + termMention.getArtifactKey() + "\" to link term mention to");
        }
        if (artifactVertices.size() > 1) {
            throw new RuntimeException("Multiple artifact vertices found \"" + termMention.getArtifactKey() + "\"");
        }

        GraphRelationship artifactRelationship = new GraphRelationship(null, artifactVertices.get(0).getId(), vertexId, "hasTermMention");
        graphSession.save(artifactRelationship);
    }

    public TermAndTermMention findMention(Session session, TermRowKey termRowKey, String artifactKey, long mentionStart, long mentionEnd) {
        Term term = findByRowKey(session, termRowKey.toString());
        if (term == null) {
            return null;
        }
        for (TermMention termMention : term.getTermMentions()) {
            if (!termMention.getArtifactKey().equals(artifactKey)) {
                continue;
            }
            if (termMention.getMentionStart() != mentionStart) {
                continue;
            }
            if (termMention.getMentionEnd() != mentionEnd) {
                continue;
            }
            return new TermAndTermMention(term, termMention);
        }
        return null;
    }
}
