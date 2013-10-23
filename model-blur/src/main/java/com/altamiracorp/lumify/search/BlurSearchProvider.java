package com.altamiracorp.lumify.search;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.blur.thirdparty.thrift_0_9_0.TException;
import org.apache.blur.thrift.BlurClient;
import org.apache.blur.thrift.generated.AnalyzerDefinition;
import org.apache.blur.thrift.generated.Blur;
import org.apache.blur.thrift.generated.BlurQuery;
import org.apache.blur.thrift.generated.BlurResult;
import org.apache.blur.thrift.generated.BlurResults;
import org.apache.blur.thrift.generated.Column;
import org.apache.blur.thrift.generated.Record;
import org.apache.blur.thrift.generated.Row;
import org.apache.blur.thrift.generated.Selector;
import org.apache.blur.thrift.generated.SimpleQuery;
import org.apache.blur.thrift.generated.TableDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.lumify.core.config.Configuration;
import com.altamiracorp.lumify.core.model.artifact.ArtifactType;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.search.ArtifactSearchResult;
import com.altamiracorp.lumify.core.model.search.SearchProvider;
import com.altamiracorp.lumify.core.user.User;

public class BlurSearchProvider extends SearchProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlurSearchProvider.class.getName());

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String BLUR_CONTROLLER_LOCATION = "search.blur.controllerLocation";
    public static final String BLUR_PATH = "search.blur.path";
    private static final String ARTIFACT_BLUR_TABLE_NAME = "artifact";
    private static final String GENERIC_COLUMN_FAMILY_NAME = "generic";
    private static final String TEXT_COLUMN_NAME = "text";
    private static final String SUBJECT_COLUMN_NAME = "subject";
    private static final String PUBLISHED_DATE_COLUMN_NAME = "publishedDate";
    private static final String GRAPH_VERTEX_ID_COLUMN_NAME = "graphVertexId";
    private static final String SOURCE_COLUMN_NAME = "source";
    private static final String RESOLVED_OBJECTS_COLUMN_NAME = "resolvedObjects";
    private static final String ARTIFACT_TYPE_COLUMN_NAME = "type";

    private Blur.Iface client;
    private String blurPath;

    public BlurSearchProvider (Configuration config, User user) {
        try {
            init(config.get(BLUR_CONTROLLER_LOCATION),config.get(BLUR_PATH),user);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    private void init(String blurControllerLocation, String blurPath, User user) throws TException {
        LOGGER.info("Connecting to blur: blurControllerLocation=" + blurControllerLocation + ", blurPath=" + blurPath);

        client = BlurClient.getClient(blurControllerLocation);

        this.blurPath = blurPath;
        initializeIndex(user);
    }

    @Override
    public void close() throws Exception {
        // noop
    }

    @Override
    public void initializeIndex(User user) {
        LOGGER.info("Creating blur tables");
        AnalyzerDefinition ad = new AnalyzerDefinition();
        try {
            List<String> tableList = client.tableList();
            String[] blurTables = new String[]{ARTIFACT_BLUR_TABLE_NAME};

            for (String blurTable : blurTables) {
                if (!tableList.contains(blurTable)) {
                    LOGGER.info("Creating table: " + blurTable);
                    createTable(client, blurPath, ad, blurTable);
                } else {
                    LOGGER.info("Skipping create table '" + blurTable + "' already exists.");
                }
            }
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTable(Blur.Iface client, String blurPath, AnalyzerDefinition ad, String tableName) throws TException {
        LOGGER.info("Creating blur table: " + tableName);

        TableDescriptor td = new TableDescriptor();
        td.setShardCount(16);
        td.setTableUri(blurPath + "/tables/" + tableName);
        td.setAnalyzerDefinition(ad);
        td.setName(tableName);

        LOGGER.info("Creating table: " + tableName);
        client.createTable(td);
    }

//    @Override
//    public void add(Artifact artifact, User user) throws Exception {
//        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        if (artifact.getContent() == null) {
//            return;
//        }
//        LOGGER.info("Adding artifact \"" + artifact.getRowKey().toString() + "\" to full text search.");
//        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
//
//        String text = artifact.getContent().getDocExtractedTextString();
//        String subject = artifact.getGenericMetadata().getSubject();
//        String id = artifact.getRowKey().toString();
//        String publishedDate = dateFormat.format(artifact.getPublishedDate());
//        String source = artifact.getGenericMetadata().getSource();
//        String graphVertexId = artifact.getGenericMetadata().getGraphVertexId();
//
//        List<String> resolvedObjects = new ArrayList<String>();
//        if (artifact.getArtifactDetectedObjects() != null) {
//            resolvedObjects = artifact.getArtifactDetectedObjects().getResolvedDetectedObjects();
//        }
//
//        if (text == null) {
//            text = "";
//        }
//        if (subject == null) {
//            subject = "";
//        }
//
//        List<Column> columns = new ArrayList<Column>();
//        columns.add(new Column(TEXT_COLUMN_NAME, text));
//        columns.add(new Column(SUBJECT_COLUMN_NAME, subject));
//        columns.add(new Column(PUBLISHED_DATE_COLUMN_NAME, publishedDate));
//        columns.add(new Column(ARTIFACT_TYPE_COLUMN_NAME, artifact.getType().toString()));
//
//        if (graphVertexId != null) {
//            columns.add(new Column(GRAPH_VERTEX_ID_COLUMN_NAME, graphVertexId));
//        }
//        if (source != null) {
//            columns.add(new Column(SOURCE_COLUMN_NAME, source));
//        }
//        if (!resolvedObjects.isEmpty()) {
//            StringBuilder resolvedObjectString = new StringBuilder();
//            for (String resolvedObject : resolvedObjects) {
//                resolvedObjectString.append(resolvedObject).append(" ");
//            }
//            columns.add(new Column(RESOLVED_OBJECTS_COLUMN_NAME, resolvedObjectString.toString().trim()));
//        }
//
//        Record record = new Record();
//        record.setRecordId(id);
//        record.setFamily(GENERIC_COLUMN_FAMILY_NAME);
//        record.setColumns(columns);
//
//        RecordMutation recordMutation = new RecordMutation();
//        recordMutation.setRecord(record);
//        recordMutation.setRecordMutationType(RecordMutationType.REPLACE_ENTIRE_RECORD);
//
//        List<RecordMutation> recordMutations = new ArrayList<RecordMutation>();
//        recordMutations.add(recordMutation);
//
//        RowMutation mutation = new RowMutation();
//        mutation.setTable(ARTIFACT_BLUR_TABLE_NAME);
//        mutation.setRowId(id);
//        mutation.setRowMutationType(RowMutationType.REPLACE_ROW);
//        mutation.setRecordMutations(recordMutations);
//
//        client.mutate(mutation);
//    }

    @Override
    public void add(GraphVertex graphVertex, InputStream textIn) {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
    }

    @Override
    public Collection<ArtifactSearchResult> searchArtifacts(String query, User user) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        BlurQuery blurQuery = new BlurQuery();
        SimpleQuery simpleQuery = new SimpleQuery();
        simpleQuery.setQueryStr(query);
        blurQuery.setSimpleQuery(simpleQuery);
        blurQuery.setSelector(new Selector());

        BlurResults blurResults = client.query(ARTIFACT_BLUR_TABLE_NAME, blurQuery);
        ArrayList<ArtifactSearchResult> results = new ArrayList<ArtifactSearchResult>();
        for (BlurResult blurResult : blurResults.getResults()) {
            Row row = blurResult.getFetchResult().getRowResult().getRow();
            String rowId = row.getId();
            String subject = "";
            assert row.getRecordCount() == 1;
            Record record = row.getRecords().get(0);
            assert record.getFamily().equals(GENERIC_COLUMN_FAMILY_NAME);
            Date publishedDate = null;
            String source = null;
            String graphVertexId = null;
            ArtifactType artifactType = ArtifactType.DOCUMENT;

            for (Column column : record.getColumns()) {
                if (column.getName().equals(SUBJECT_COLUMN_NAME)) {
                    subject = column.getValue();
                } else if (column.getName().equals(PUBLISHED_DATE_COLUMN_NAME)) {
                    publishedDate = dateFormat.parse(column.getValue());
                } else if (column.getName().equals(SOURCE_COLUMN_NAME)) {
                    source = column.getValue();
                } else if (column.getName().equals(ARTIFACT_TYPE_COLUMN_NAME)) {
                    artifactType = ArtifactType.convert(column.getValue());
                } else if (column.getName().equals(GRAPH_VERTEX_ID_COLUMN_NAME)) {
                    graphVertexId = column.getValue();
                }
            }

            ArtifactSearchResult result = new ArtifactSearchResult(rowId, subject, publishedDate, source, artifactType, graphVertexId);
            results.add(result);
        }
        return results;
    }

    @Override
    public void deleteIndex(User user) {
        deleteTable(ARTIFACT_BLUR_TABLE_NAME);
    }

    private void deleteTable(String tableName) {
        try {
            LOGGER.info("Deleting blur table: " + tableName);
            client.disableTable(tableName);
            client.removeTable(tableName, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
