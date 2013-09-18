package com.altamiracorp.lumify.model.resources;

import com.altamiracorp.lumify.model.*;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class ResourceRepository extends Repository<Resource> {
    public String importFile(ModelSession session, String fileName) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(fileName);
            String contentType;
            if (fileName.endsWith(".png")) {
                contentType = "image/png";
            } else {
                throw new RuntimeException("Unhandled content type: " + fileName);
            }
            return importFile(in, contentType);
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

    public String importFile(InputStream in, String contentType) {
        try {
            byte[] data = IOUtils.toByteArray(in);
            Resource res = new Resource(data, contentType);
            save(res);
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
