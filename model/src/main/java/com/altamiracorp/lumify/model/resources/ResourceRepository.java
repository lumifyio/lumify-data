package com.altamiracorp.lumify.model.resources;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.apache.commons.io.IOUtils;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.Column;
import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.ModelSession;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.Row;
import com.google.inject.Inject;

public class ResourceRepository extends Repository<Resource> {
    @Inject
    public ResourceRepository(final ModelSession modelSession) {
        super(modelSession);
    }

    public String importFile(ModelSession session, String fileName, User user) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(fileName);
            String contentType;
            if (fileName.endsWith(".png")) {
                contentType = "image/png";
            } else {
                throw new RuntimeException("Unhandled content type: " + fileName);
            }
            return importFile(in, contentType, user);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not import file: " + fileName, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new RuntimeException("Could not import file: " + fileName, e);
                }
            }
        }
    }

    public String importFile(InputStream in, String contentType, User user) {
        try {
            byte[] data = IOUtils.toByteArray(in);
            Resource res = new Resource(data, contentType);
            save(res, user);
            return res.getRowKey().toString();
        } catch (IOException e) {
            throw new RuntimeException("Could not import file", e);
        }
    }

    @Override
    public Resource fromRow(Row row) {
        Resource resource = new Resource(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(ResourceContent.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                resource.addColumnFamily(new ResourceContent().addColumns(columns));
            } else {
                resource.addColumnFamily(columnFamily);
            }
        }
        return resource;
    }

    @Override
    public Row toRow(Resource obj) {
        return obj;
    }

    @Override
    public String getTableName() {
        return Resource.TABLE_NAME;
    }
}
