package com.altamiracorp.reddawn.ucd.term;

import com.altamiracorp.reddawn.model.*;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndex;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndexRepository;
import org.apache.accumulo.core.client.Scanner;

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
            terms.add(term);
        }
        return terms;
    }

    public List<ColumnFamily> findMentions(Session session, String rowKey, long colFamOffset, long colFamLimit) {
        return findColFamsByRowKeyWithOffset(session, rowKey, colFamOffset, colFamLimit, "urn.*");
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
}
