package com.altamiracorp.lumify.model;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.artifactThumbnails.ArtifactThumbnail;
import com.altamiracorp.lumify.model.dbpedia.DBPedia;
import com.altamiracorp.lumify.model.geoNames.GeoName;
import com.altamiracorp.lumify.model.geoNames.GeoNameAdmin1Code;
import com.altamiracorp.lumify.model.geoNames.GeoNameCountryInfo;
import com.altamiracorp.lumify.model.geoNames.GeoNamePostalCode;
import com.altamiracorp.lumify.model.resources.Resource;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.model.workspace.Workspace;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
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
            GeoName.TABLE_NAME,
            GeoNameAdmin1Code.TABLE_NAME,
            GeoNameCountryInfo.TABLE_NAME,
            GeoNamePostalCode.TABLE_NAME,
            VideoFrame.TABLE_NAME,
            DBPedia.TABLE_NAME,
            Resource.TABLE_NAME,
            com.altamiracorp.lumify.model.user.User.TABLE_NAME,
            GeoName.TABLE_NAME,
            GeoNameAdmin1Code.TABLE_NAME,
            GeoNameCountryInfo.TABLE_NAME,
            GeoNamePostalCode.TABLE_NAME,
            "atc_titan");// see com.altamiracorp.lumify.model.TitanGraphSession

    public ModelSession() {
    }

    abstract void save(Row row, User user);

    abstract void saveMany(String tableName, Collection<Row> rows, User user);

    public abstract List<Row> findByRowKeyRange(String tableName, String keyStart, String keyEnd, User user);

    abstract List<Row> findByRowStartsWith(String tableName, String rowKeyPrefix, User user);

    abstract List<Row> findByRowKeyRegex(String tableName, String rowKeyRegex, User user);

    abstract Row findByRowKey(String tableName, String rowKey, User user);

    abstract Row findByRowKey(String tableName, String rowKey, Map<String, String> columnsToReturn, User user);

    public abstract void initializeTable(String tableName, User user);

    public abstract void deleteTable(String tableName, User user);

    public abstract void deleteRow(String tableName, RowKey rowKey, User user);

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
