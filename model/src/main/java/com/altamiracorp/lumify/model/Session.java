package com.altamiracorp.lumify.model;

import com.altamiracorp.lumify.model.artifactThumbnails.ArtifactThumbnail;
import com.altamiracorp.lumify.model.dbpedia.DBPedia;
import com.altamiracorp.lumify.model.geoNames.GeoName;
import com.altamiracorp.lumify.model.geoNames.GeoNameAdmin1Code;
import com.altamiracorp.lumify.model.geoNames.GeoNameCountryInfo;
import com.altamiracorp.lumify.model.geoNames.GeoNamePostalCode;
import com.altamiracorp.lumify.model.resources.Resource;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.user.User;
import com.altamiracorp.lumify.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.model.workspace.Workspace;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;

public abstract class Session {
    private static final Logger LOGGER = LoggerFactory.getLogger(Session.class.getName());
    private static final List<String> tables = Arrays.asList(
            Artifact.TABLE_NAME,
            ArtifactThumbnail.TABLE_NAME,
            Workspace.TABLE_NAME,
            TermMention.TABLE_NAME,
            GeoName.TABLE_NAME,
            GeoNameAdmin1Code.TABLE_NAME,
            GeoNameCountryInfo.TABLE_NAME,
            GeoNamePostalCode.TABLE_NAME,
            VideoFrame.TABLE_NAME,
            DBPedia.TABLE_NAME,
            Resource.TABLE_NAME,
            User.TABLE_NAME,
            "atc_titan");// see com.altamiracorp.lumify.model.TitanGraphSession
    private QueryUser queryUser;
    private String dbpediaSourceArtifactRowKey;

    public Session(QueryUser queryUser) {
        this.queryUser = queryUser;
    }

    abstract void save(Row row);

    abstract void saveMany(String tableName, Collection<Row> rows);

    public abstract List<Row> findByRowKeyRange(String tableName, String keyStart, String keyEnd, QueryUser queryUser);

    abstract List<Row> findByRowStartsWith(String tableName, String rowKeyPrefix, QueryUser queryUser);

    abstract List<Row> findByRowStartsWithList(String tableName, List<String> rowKeyPrefix, QueryUser queryUser);

    abstract List<Row> findByRowKeyRegex(String tableName, String rowKeyRegex, QueryUser queryUser);

    abstract Row findByRowKey(String tableName, String rowKey, QueryUser queryUser);

    abstract Row findByRowKey(String tableName, String rowKey, Map<String, String> columnsToReturn, QueryUser queryUser);

    abstract List<ColumnFamily> findByRowKeyWithColumnFamilyRegexOffsetAndLimit(String tableName, String rowKey, QueryUser queryUser,
                                                                                long colFamOffset, long colFamLimit, String colFamRegex);

    public abstract void initializeTable(String tableName);

    public abstract void deleteTable(String tableName);

    public abstract void deleteRow(String tableName, RowKey rowKey);

    public void initializeTables() {
        LOGGER.info("initializeTables");
        for (String table : tables) {
            initializeTable(table);
        }

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

    public QueryUser getQueryUser() {
        return this.queryUser;
    }

    public void deleteTables() {
        LOGGER.info("deleteTables");
        for (String table : tables) {
            deleteTable(table);
        }
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

    public abstract List<String> getTableList();

    public abstract void touchRow(String tableName, RowKey rowKey, QueryUser queryUser);
}
