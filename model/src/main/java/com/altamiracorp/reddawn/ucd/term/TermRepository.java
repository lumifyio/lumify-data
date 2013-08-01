package com.altamiracorp.reddawn.ucd.term;

import com.altamiracorp.reddawn.model.*;
import com.altamiracorp.reddawn.model.graph.GraphNode;
import com.altamiracorp.reddawn.model.graph.GraphNodeImpl;
import com.altamiracorp.reddawn.model.graph.GraphRelationship;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndex;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndexRepository;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class TermRepository extends Repository<Term> {
    private ArtifactTermIndexRepository artifactTermIndexRepository = new ArtifactTermIndexRepository();
    private SentenceRepository sentenceRepository = new SentenceRepository();

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

    public void saveToGraph(Session session, GraphSession graphSession, Term term, TermMention termMention) {
        String suggestedNodeId = termMention.getGraphNodeId(term);
        GraphNode node = new GraphNodeImpl(suggestedNodeId);
        node.setProperty("type", "termMention");
        node.setProperty("rowKey", term.getRowKey().toString());
        node.setProperty("columnFamilyName", termMention.getColumnFamilyName());
        node.setProperty("sign", term.getRowKey().getSign());
        node.setProperty("modelKey", term.getRowKey().getModelKey());
        node.setProperty("conceptLabel", term.getRowKey().getConceptLabel());

        String nodeId = graphSession.save(node);
        if (!nodeId.equals(suggestedNodeId)) {
            termMention.setGraphNodeId(nodeId);
            this.save(session, term);
        }

        List<GraphNode> artifactNodes = graphSession.findBy("rowKey", termMention.getArtifactKey());
        if (artifactNodes.size() == 0) {
            throw new RuntimeException("Could not find artifact \"" + termMention.getArtifactKey() + "\" to link term mention to");
        }
        if (artifactNodes.size() > 1) {
            throw new RuntimeException("Multiple artifact nodes found \"" + termMention.getArtifactKey() + "\"");
        }

        GraphRelationship artifactRelationship = new GraphRelationship(null, artifactNodes.get(0), node, "artifactToTermMention");
        graphSession.save(artifactRelationship);
    }
}
