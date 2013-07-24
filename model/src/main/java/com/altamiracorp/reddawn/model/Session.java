package com.altamiracorp.reddawn.model;

import com.altamiracorp.reddawn.model.dbpedia.DBPedia;
import com.altamiracorp.reddawn.model.geoNames.GeoName;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrame;
import com.altamiracorp.reddawn.model.workspace.Workspace;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndex;
import com.altamiracorp.reddawn.ucd.concept.Concept;
import com.altamiracorp.reddawn.ucd.concept.ConceptRepository;
import com.altamiracorp.reddawn.ucd.concept.ConceptRowKey;
import com.altamiracorp.reddawn.ucd.object.UcdObject;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.source.Source;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import com.altamiracorp.reddawn.ucd.term.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class Session {
    private static final Logger LOGGER = LoggerFactory.getLogger(Session.class.getName());
    private QueryUser queryUser;
    private String dbpediaSourceArtifactRowKey;

    public Session(QueryUser queryUser) {
        this.queryUser = queryUser;
    }

    abstract void save(Row row);

    abstract void saveMany(String tableName, Collection<Row> rows);

    public abstract List<Row> findByRowKeyRange(String tableName, String keyStart, String keyEnd, QueryUser queryUser);

    abstract List<Row> findByRowStartsWith(String tableName, String rowKeyPrefix, QueryUser queryUser);

    abstract List<Row> findByRowStartsWithList (String tableName, List<String> rowKeyPrefix, QueryUser queryUser);

    abstract List<Row> findByRowKeyRegex(String tableName, String rowKeyRegex, QueryUser queryUser);

    abstract Row findByRowKey(String tableName, String rowKey, QueryUser queryUser);

    abstract Row findByRowKey(String tableName, String rowKey, Map<String, String> columnsToReturn, QueryUser queryUser);

    abstract List<ColumnFamily> findByRowKeyWithColumnFamilyRegexOffsetAndLimit(String tableName, String rowKey, QueryUser queryUser,
                                                                                long colFamOffset, long colFamLimit, String colFamRegex);

    abstract void initializeTable(String tableName);

    public abstract void deleteTable(String tableName);

    public abstract void deleteRow(String tableName, RowKey rowKey);

    public void initializeTables() {
        LOGGER.info("initializeTables");
        initializeTable(Artifact.TABLE_NAME);
        initializeTable(Term.TABLE_NAME);
        initializeTable(Source.TABLE_NAME);
        initializeTable(Sentence.TABLE_NAME);
        initializeTable(ArtifactTermIndex.TABLE_NAME);
        initializeTable(Statement.TABLE_NAME);
        initializeTable(UcdObject.TABLE_NAME);
        initializeTable(Concept.TABLE_NAME);

        initializeTable(Workspace.TABLE_NAME);
        initializeTable(GeoName.TABLE_NAME);
        initializeTable(VideoFrame.TABLE_NAME);
        initializeTable(DBPedia.TABLE_NAME);

        addDefaultConcepts();
        addDbpediaSourceArtifact();
    }

    protected void addDbpediaSourceArtifact() {
        ArtifactRepository artifactRepository = new ArtifactRepository();
        Date date = new Date(0);

        Artifact artifact = new Artifact();
        artifact.getContent()
                .setDocArtifactBytes("DBPedia".getBytes());
        artifact.getGenericMetadata()
                .setMimeType("text/plain")
                .setSubject("DBPedia")
                .setAuthor("system")
                .setFileName("dbpedia")
                .setFileExtension("txt")
                .setDocumentDtg(date);
        artifactRepository.save(this, artifact);

        dbpediaSourceArtifactRowKey = artifact.getRowKey().toString();
    }

    protected void addDefaultConcepts() {
        ConceptRepository conceptRepository = new ConceptRepository();

        conceptRepository.save(this, new ConceptRowKey("system", "person"), "Person");
        conceptRepository.save(this, new ConceptRowKey("system", "location"), "Location");
        conceptRepository.save(this, new ConceptRowKey("system", "organization"), "Organization");
        conceptRepository.save(this, new ConceptRowKey("system", "date"), "Date");
        conceptRepository.save(this, new ConceptRowKey("system", "money"), "Money");
        conceptRepository.save(this, new ConceptRowKey("system", "percentage"), "Percentage");
        conceptRepository.save(this, new ConceptRowKey("system", "time"), "Time");
    }

    public QueryUser getQueryUser() {
        return this.queryUser;
    }

    public void deleteTables() {
        LOGGER.info("deleteTables");
        deleteTable(Artifact.TABLE_NAME);
        deleteTable(Term.TABLE_NAME);
        deleteTable(Source.TABLE_NAME);
        deleteTable(Sentence.TABLE_NAME);
        deleteTable(ArtifactTermIndex.TABLE_NAME);
        deleteTable(Statement.TABLE_NAME);
        deleteTable(UcdObject.TABLE_NAME);
        deleteTable(Concept.TABLE_NAME);

        deleteTable(Workspace.TABLE_NAME);
        deleteTable(GeoName.TABLE_NAME);
        deleteTable(VideoFrame.TABLE_NAME);
        deleteTable(DBPedia.TABLE_NAME);
    }

    public abstract SaveFileResults saveFile(InputStream in);

    public abstract InputStream loadFile(String path);

    public abstract long getFileLength(String path);

    public String getDbpediaSourceArtifactRowKey() {
        if (dbpediaSourceArtifactRowKey == null) {
            addDbpediaSourceArtifact();
        }
        return dbpediaSourceArtifactRowKey;
    }
}
