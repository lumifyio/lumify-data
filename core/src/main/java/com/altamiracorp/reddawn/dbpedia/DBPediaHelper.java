package com.altamiracorp.reddawn.dbpedia;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.Column;
import com.altamiracorp.reddawn.model.Value;
import com.altamiracorp.reddawn.model.dbpedia.DBPedia;
import com.altamiracorp.reddawn.model.dbpedia.DBPediaRepository;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;
import com.altamiracorp.reddawn.ucd.predicate.PredicateRowKey;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRowKey;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import com.altamiracorp.reddawn.ucd.statement.StatementArtifact;
import com.altamiracorp.reddawn.ucd.statement.StatementRowKey;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DBPediaHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBPediaHelper.class.getName());
    private static final String EXTRACTOR_ID = "dbpedia";
    private static final Map<String, PredicateMapAction> predicateMap;
    private static DBPediaRepository dbPediaRepository = new DBPediaRepository();

    static {
        predicateMap = new HashMap<String, PredicateMapAction>();
        predicateMap.put("http://dbpedia.org/ontology/locationCity", new DefaultPredicateMapAction("dbpedia", "locationCity"));
        predicateMap.put("http://dbpedia.org/ontology/locationCountry", new DefaultPredicateMapAction("dbpedia", "locationCountry"));
        predicateMap.put("http://dbpedia.org/ontology/vicePresident", new DefaultPredicateMapAction("dbpedia", "vicePresident"));
        predicateMap.put("http://dbpedia.org/ontology/party", new DefaultPredicateMapAction("dbpedia", "party"));
        predicateMap.put("http://dbpedia.org/ontology/religion", new DefaultPredicateMapAction("dbpedia", "religion"));
        predicateMap.put("http://dbpedia.org/ontology/child", new DefaultPredicateMapAction("dbpedia", "child"));
        predicateMap.put("http://dbpedia.org/ontology/deathPlace", new DefaultPredicateMapAction("dbpedia", "deathPlace"));
        predicateMap.put("http://dbpedia.org/ontology/birthPlace", new DefaultPredicateMapAction("dbpedia", "birthPlace"));
        predicateMap.put("http://dbpedia.org/ontology/region", new DefaultPredicateMapAction("dbpedia", "region"));
        predicateMap.put("http://dbpedia.org/ontology/battle", new DefaultPredicateMapAction("dbpedia", "battle"));
        predicateMap.put("http://dbpedia.org/ontology/spouse", new DefaultPredicateMapAction("dbpedia", "spouse"));
        predicateMap.put("http://dbpedia.org/ontology/otherParty", new DefaultPredicateMapAction("dbpedia", "otherParty"));

        predicateMap.put("http://dbpedia.org/ontology/activeYearsStartYear", new CreateTermPredicateMapAction("dbpedia", "activeYearsStartYear", "date"));
        predicateMap.put("http://dbpedia.org/ontology/activeYearsStartDate", new CreateTermPredicateMapAction("dbpedia", "activeYearsStartDate", "date"));
        predicateMap.put("http://dbpedia.org/ontology/foundingDate", new CreateTermPredicateMapAction("dbpedia", "foundingDate", "date"));
        predicateMap.put("http://dbpedia.org/ontology/birthDate", new CreateTermPredicateMapAction("dbpedia", "birthDate", "date"));
        predicateMap.put("http://dbpedia.org/ontology/deathDate", new CreateTermPredicateMapAction("dbpedia", "deathDate", "date"));
        predicateMap.put("http://dbpedia.org/ontology/serviceStartYear", new CreateTermPredicateMapAction("dbpedia", "serviceStartYear", "date"));
        predicateMap.put("http://dbpedia.org/ontology/serviceEndYear", new CreateTermPredicateMapAction("dbpedia", "serviceEndYear", "date"));

        predicateMap.put("http://www.georss.org/georss/point", null);
        predicateMap.put("http://xmlns.com/foaf/0.1/name", null);
    }

    public Term createTerm(DBPedia dbpedia, String dbpediaSourceArtifactRowKey) {
        Term term = new Term(getTermRowKey(dbpedia));
        TermMention termMention = createTermMention(dbpediaSourceArtifactRowKey);

        String geoLocation = Value.toString(dbpedia.getMappingBasedProperties().get("http://www.georss.org/georss/point"));
        if (geoLocation != null) {
            String[] geoLocationParts = geoLocation.split(" ");
            Double lat = Double.parseDouble(geoLocationParts[0]);
            Double lon = Double.parseDouble(geoLocationParts[1]);
            termMention.setGeoLocation(lat, lon);
        }

        term.addTermMention(termMention);
        return term;
    }

    private TermRowKey getTermRowKey(DBPedia dbpedia) {
        String sign = dbpedia.getLabel().getLabel();
        if (sign == null || sign.length() == 0) {
            return null;
        }

        String modelKey = TermRowKey.DBPEDIA_MODEL_KEY;
        String conceptLabel = dbPediaRepository.getConceptLabel(dbpedia);
        if (conceptLabel == null) {
            return null;
        }

        return new TermRowKey(sign, modelKey, conceptLabel);
    }

    private static TermMention createTermMention(String dbpediaSourceArtifactRowKey) {
        TermMention termMention = new TermMention();
        termMention
                .setArtifactKey(dbpediaSourceArtifactRowKey)
                .setArtifactSubject("dbpedia")
                .setArtifactType(ArtifactType.DOCUMENT.toString())
                .setAuthor("dbpedia")
                .setMentionStart(0L)
                .setMentionEnd(0L)
                .setSecurityMarking("U")
                .setDate(new Date())
                .setSentenceText("")
                .setSentenceTokenOffset(0L);
        return termMention;
    }

    public class CreateStatementsResult {
        public List<Statement> statements;
        public List<Term> terms;
    }

    public CreateStatementsResult createStatements(RedDawnSession session, Term dbpediaTerm, DBPedia dbpedia, String dbpediaSourceArtifactRowKey) {
        CreateStatementsResult createStatementsResult = new CreateStatementsResult();
        createStatementsResult.statements = new ArrayList<Statement>();
        createStatementsResult.terms = new ArrayList<Term>();

        for (Column prop : dbpedia.getMappingBasedProperties().getColumns()) {
            if (!predicateMap.containsKey(prop.getName())) {
                LOGGER.warn("Unknown predicate: " + prop.getName() + " (value: " + prop.getValue() + ")");
                continue;
            }
            PredicateMapAction predicateMapAction = predicateMap.get(prop.getName());
            if (predicateMapAction == null) {
                continue;
            }

            TermRowKey targetRowKey = predicateMapAction.getTargetRowKey(session, prop);
            if (targetRowKey == null) {
                continue;
            }

            Statement statement = new Statement(new StatementRowKey(dbpediaTerm.getRowKey(), predicateMapAction.getPredicateRowKey(), targetRowKey));
            StatementArtifact statementArtifact = new StatementArtifact();
            statementArtifact
                    .setArtifactSubject("dbpedia")
                    .setArtifactKey(dbpediaSourceArtifactRowKey)
                    .setSentenceText("dbpedia")
                    .setSentence(new SentenceRowKey(dbpediaSourceArtifactRowKey, 0, 0))
                    .setDate(new Date())
                    .setExtractorId(EXTRACTOR_ID)
                    .setSecurityMarking("U");
            statement.addStatementArtifact(statementArtifact);
            createStatementsResult.statements.add(statement);

            List<Term> terms = predicateMapAction.createTerms(session, dbpediaTerm, dbpedia, prop, dbpediaSourceArtifactRowKey);
            if (terms != null) {
                createStatementsResult.terms.addAll(terms);
            }
        }

        return createStatementsResult;
    }

    private static abstract class PredicateMapAction {
        private PredicateRowKey predicateRowKey;

        public PredicateMapAction(String modelKey, String predicateLabel) {
            predicateRowKey = new PredicateRowKey(modelKey, predicateLabel);
        }

        public PredicateRowKey getPredicateRowKey() {
            return this.predicateRowKey;
        }

        public List<Term> createTerms(RedDawnSession session, Term dbpediaTerm, DBPedia dbpedia, Column prop, String dbpediaSourceArtifactRowKey) {
            return null;
        }

        public abstract TermRowKey getTargetRowKey(RedDawnSession session, Column column);
    }

    private static class DefaultPredicateMapAction extends PredicateMapAction {
        public DefaultPredicateMapAction(String modelKey, String predicateLabel) {
            super(modelKey, predicateLabel);
        }

        @Override
        public TermRowKey getTargetRowKey(RedDawnSession session, Column column) {
            return dbPediaRepository.findTermRowKeyByDBPediaRowKey(session.getModelSession(), column.getValue().toString());
        }
    }

    private static class CreateTermPredicateMapAction extends PredicateMapAction {
        private final String termConceptLabel;

        public CreateTermPredicateMapAction(String modelKey, String predicateLabel, String termConceptLabel) {
            super(modelKey, predicateLabel);
            this.termConceptLabel = termConceptLabel;
        }

        @Override
        public List<Term> createTerms(RedDawnSession session, Term dbpediaTerm, DBPedia dbpedia, Column column, String dbpediaSourceArtifactRowKey) {
            List<Term> terms = super.createTerms(session, dbpediaTerm, dbpedia, column, dbpediaSourceArtifactRowKey);
            if (terms == null) {
                terms = new ArrayList<Term>();
            }

            TermRowKey termRowKey = getTargetRowKey(session, column);
            Term term = new Term(termRowKey);
            TermMention termMention = createTermMention(dbpediaSourceArtifactRowKey);
            term.addTermMention(termMention);
            terms.add(term);

            return terms;
        }

        @Override
        public TermRowKey getTargetRowKey(RedDawnSession session, Column column) {
            return new TermRowKey(column.getValue().toString(), TermRowKey.DBPEDIA_MODEL_KEY, termConceptLabel);
        }
    }
}
