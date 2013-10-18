package com.altamiracorp.lumify.core.model;

import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.altamiracorp.lumify.core.model.artifactThumbnails.ArtifactThumbnail;
import com.altamiracorp.lumify.core.model.dbpedia.DBPedia;
import com.altamiracorp.lumify.core.model.dictionary.DictionaryEntry;
import com.altamiracorp.lumify.core.model.geoNames.GeoName;
import com.altamiracorp.lumify.core.model.geoNames.GeoNameAdmin1Code;
import com.altamiracorp.lumify.core.model.geoNames.GeoNameCountryInfo;
import com.altamiracorp.lumify.core.model.geoNames.GeoNamePostalCode;
import com.altamiracorp.lumify.core.model.resources.Resource;
import com.altamiracorp.lumify.core.model.termMention.TermMention;
import com.altamiracorp.lumify.core.model.user.UserRow;
import com.altamiracorp.lumify.core.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.core.model.workspace.Workspace;
import com.altamiracorp.lumify.core.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class ModelSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelSession.class.getName());
    private static final List<String> tables = Arrays.asList(
            Artifact.TABLE_NAME,
            ArtifactThumbnail.TABLE_NAME,
            Workspace.TABLE_NAME,
            TermMention.TABLE_NAME,
            VideoFrame.TABLE_NAME,
            DBPedia.TABLE_NAME,
            Resource.TABLE_NAME,
            UserRow.TABLE_NAME,
            GeoName.TABLE_NAME,
            GeoNameAdmin1Code.TABLE_NAME,
            GeoNameCountryInfo.TABLE_NAME,
            GeoNamePostalCode.TABLE_NAME,
            DictionaryEntry.TABLE_NAME,
            "atc_titan");// TODO refactor see com.altamiracorp.lumify.model.TitanGraphSession

    public abstract void save(Row row, User user);

    public abstract void saveMany(String tableName, Collection<Row> rows, User user);

    public abstract List<Row> findByRowKeyRange(String tableName, String keyStart, String keyEnd, User user);

    public abstract List<Row> findByRowStartsWith(String tableName, String rowKeyPrefix, User user);

    public abstract List<Row> findByRowKeyRegex(String tableName, String rowKeyRegex, User user);

    public abstract Row findByRowKey(String tableName, String rowKey, User user);

    public abstract Row findByRowKey(String tableName, String rowKey, Map<String, String> columnsToReturn, User user);

    public abstract void initializeTable(String tableName, User user);

    public abstract void deleteTable(String tableName, User user);

    public abstract void deleteRow(String tableName, RowKey rowKey, User user);

    public abstract void deleteColumn(Row row, String tableName, String columnFamily, String columnQualifier, User user);

    public void initializeTables(User user) {
        LOGGER.info("initializeTables");
        for (String table : tables) {
            initializeTable(table, user);
        }
    }

    public void deleteTables(User user) {
        LOGGER.info("deleteTables");
        for (String table : tables) {
            deleteTable(table, user);
        }
    }

    public abstract SaveFileResults saveFile(InputStream in, User user);

    public abstract InputStream loadFile(String path, User user);

    public abstract long getFileLength(String path, User user);

    public abstract List<String> getTableList(User user);

    public abstract void close();
}
