package com.altamiracorp.reddawn;

import com.altamiracorp.reddawn.model.Workspace;
import com.altamiracorp.reddawn.model.WorkspaceKey;
import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.QueryUser;
import com.altamiracorp.reddawn.ucd.UCDIOException;
import org.apache.accumulo.core.client.*;
import org.apache.accumulo.core.data.Range;

import java.util.Collection;
import java.util.List;

public class RedDawnClient {
    private final Connector connection;
    private long batchWriterMemBuf = 1000000L; // TODO duplicate of UcdClient
    private long batchWriterTimeout = 1000L; // TODO duplicate of UcdClient
    private int batchWriterNumThreads = 10; // TODO duplicate of UcdClient

    public RedDawnClient(Connector connection) {
        this.connection = connection;
    }

    public void initializeTables() throws TableExistsException, AccumuloSecurityException, AccumuloException {
        initializeTable(Workspace.TABLE_NAME);
    }

    // TODO duplicate of UcdClient
    private void initializeTable(String tableName) throws TableExistsException, AccumuloSecurityException, AccumuloException {
        if (!connection.tableOperations().exists(tableName)) {
            connection.tableOperations().create(tableName);
        }
    }

    public void deleteTables() throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
        deleteTable(Workspace.TABLE_NAME);
    }

    // TODO duplicate of UcdClient
    private void deleteTable(String tableName) throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
        if (connection.tableOperations().exists(tableName)) {
            connection.tableOperations().delete(tableName);
        }
    }

    public void saveWorkspace(Workspace workspace, QueryUser<AuthorizationLabel> queryUser) throws UCDIOException {
        try {
            BatchWriter writer = this.connection.createBatchWriter(Workspace.TABLE_NAME, this.batchWriterMemBuf, this.batchWriterTimeout, this.batchWriterNumThreads);
            writer.addMutation(workspace.getMutation());
            writer.close();
        } catch (TableNotFoundException e) {
            throw new UCDIOException(e);
        } catch (MutationsRejectedException e) {
            throw new UCDIOException(e);
        }
    }

    public Workspace queryWorkspaceByRowKey(WorkspaceKey workspaceKey, QueryUser<AuthorizationLabel> queryUser) throws UCDIOException {
        try {
            Scanner scan = this.connection.createScanner(Workspace.TABLE_NAME, queryUser.getAuthorizations());
            scan.setRange(new Range(workspaceKey.toString()));

            List<Workspace> workspaces = Workspace.buildFromScanner(scan);
            if (workspaces.isEmpty()) {
                return null;
            }
            if (workspaces.size() != 1) {
                throw new UCDIOException("Multiple rows returned for workspace with key: " + workspaceKey.toString());
            }
            return workspaces.get(0);
        } catch (TableNotFoundException e) {
            throw new UCDIOException(e);
        }
    }

    public Collection<Workspace> queryWorkspaceByUserId(String userId, QueryUser<AuthorizationLabel> queryUser) throws UCDIOException {
        try {
            Scanner scan = this.connection.createScanner(Workspace.TABLE_NAME, queryUser.getAuthorizations());
            scan.setRange(new Range(userId, userId + "Z")); // TODO: is there a better way instead of appending "Z"

            return Workspace.buildFromScanner(scan);
        } catch (TableNotFoundException e) {
            throw new UCDIOException(e);
        }
    }
}
