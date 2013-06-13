package com.altamiracorp.reddawn.search;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import org.apache.blur.thirdparty.thrift_0_9_0.TException;
import org.apache.blur.thirdparty.thrift_0_9_0.protocol.TBinaryProtocol;
import org.apache.blur.thirdparty.thrift_0_9_0.protocol.TProtocol;
import org.apache.blur.thirdparty.thrift_0_9_0.transport.TFramedTransport;
import org.apache.blur.thirdparty.thrift_0_9_0.transport.TSocket;
import org.apache.blur.thirdparty.thrift_0_9_0.transport.TTransport;
import org.apache.blur.thrift.generated.*;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BlurSearchProvider implements SearchProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlurSearchProvider.class.getName());

    public static final String BLUR_CONTROLLER_PORT = "blurControllerPort";
    public static final String BLUR_CONTROLLER_LOCATION = "blurControllerLocation";
    public static final String BLUR_PATH = "blurPath";
    private static final String ARTIFACT_BLUR_TABLE_NAME = "artifact";
    private static final String GENERIC_COLUMN_FAMILY_NAME = "generic";
    private static final String TEXT_COLUMN_NAME = "text";
    private static final String SUBJECT_COLUMN_NAME = "subject";
    private Blur.Client client;

    @Override
    public void setup(Mapper.Context context) throws Exception {
        String blurControllerLocation = context.getConfiguration().get(BLUR_CONTROLLER_LOCATION, "192.168.33.10");
        int blurControllerPort = context.getConfiguration().getInt(BLUR_CONTROLLER_PORT, 40020);
        String blurPath = context.getConfiguration().get(BLUR_PATH, "hdfs://192.168.33.10/blur");

        init(blurControllerLocation, blurControllerPort, blurPath);
    }

    public void setup(Properties props) {
        String blurControllerLocation = props.getProperty(BLUR_CONTROLLER_LOCATION, "192.168.33.10");
        int blurControllerPort = Integer.parseInt(props.getProperty(BLUR_CONTROLLER_PORT, "40020"));
        String blurPath = props.getProperty(BLUR_PATH, "hdfs://192.168.33.10/blur");

        try {
            init(blurControllerLocation, blurControllerPort, blurPath);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    private void init(String blurControllerLocation, int blurControllerPort, String blurPath) throws TException {
        LOGGER.info("Connecting to blur: blurControllerLocation=" + blurControllerLocation + ", blurControllerPort=" + blurControllerPort + ", blurPath=" + blurPath);
        TTransport trans = new TSocket(blurControllerLocation, blurControllerPort);
        trans.open();
        TProtocol proto = new TBinaryProtocol(new TFramedTransport(trans));
        this.client = new Blur.Client.Factory().getClient(proto);

        createTables(blurPath);
    }

    private void createTables(String blurPath) throws TException {
        LOGGER.info("Creating blur tables");
        AnalyzerDefinition ad = new AnalyzerDefinition();
        List<String> tableList = this.client.tableList();

        if (!tableList.contains(ARTIFACT_BLUR_TABLE_NAME)) {
            LOGGER.info("Creating blur table: " + ARTIFACT_BLUR_TABLE_NAME);
            createTable(client, blurPath, ad, ARTIFACT_BLUR_TABLE_NAME);
        } else {
            LOGGER.info("Skipping create blur table '" + ARTIFACT_BLUR_TABLE_NAME + "' already exists.");
        }
    }

    private void createTable(Blur.Client client, String blurPath, AnalyzerDefinition ad, String tableName) throws TException {
        TableDescriptor td = new TableDescriptor();
        td.setShardCount(16);
        td.setTableUri(blurPath + "/tables/" + tableName);
        td.setAnalyzerDefinition(ad);
        td.setName(tableName);

        LOGGER.info("Creating table: " + tableName);
        client.createTable(td);
    }

    @Override
    public void add(Artifact artifact) throws Exception {
        if (artifact.getContent() == null) {
            return;
        }
        LOGGER.info("Adding artifact \"" + artifact.getRowKey().toString() + "\" to full text search.");
        String text = artifact.getContent().getDocExtractedTextString();
        String subject = artifact.getGenericMetadata().getSubject();
        String id = artifact.getRowKey().toString();

        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column(TEXT_COLUMN_NAME, text));
        columns.add(new Column(SUBJECT_COLUMN_NAME, subject));

        Record record = new Record();
        record.setRecordId(id);
        record.setFamily(GENERIC_COLUMN_FAMILY_NAME);
        record.setColumns(columns);

        RecordMutation recordMutation = new RecordMutation();
        recordMutation.setRecord(record);
        recordMutation.setRecordMutationType(RecordMutationType.REPLACE_ENTIRE_RECORD);

        List<RecordMutation> recordMutations = new ArrayList<RecordMutation>();
        recordMutations.add(recordMutation);

        RowMutation mutation = new RowMutation();
        mutation.setTable(ARTIFACT_BLUR_TABLE_NAME);
        mutation.setRowId(id);
        mutation.setRowMutationType(RowMutationType.REPLACE_ROW);
        mutation.setRecordMutations(recordMutations);

        client.mutate(mutation);
    }

    @Override
    public Collection<ArtifactSearchResult> searchArtifacts(String query) throws Exception {
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

            Iterator<Column> columns = record.getColumnsIterator();
            while (columns.hasNext()) {
                Column column = columns.next();
                if (column.getName().equals(SUBJECT_COLUMN_NAME)) {
                    subject = column.getValue();
                    break;
                }
            }

            ArtifactSearchResult result = new ArtifactSearchResult(rowId, subject);
            results.add(result);
        }
        return results;
    }
}
