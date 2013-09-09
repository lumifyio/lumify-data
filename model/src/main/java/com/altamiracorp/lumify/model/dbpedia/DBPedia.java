package com.altamiracorp.lumify.model.dbpedia;

import com.altamiracorp.lumify.model.Row;
import com.altamiracorp.lumify.model.RowKey;

public class DBPedia extends Row<DBPediaRowKey> {
    public static final String TABLE_NAME = "atc_dbpedia";

    public DBPedia(DBPediaRowKey rowKey) {
        super(TABLE_NAME, rowKey);
    }

    public DBPedia(String id) {
        super(TABLE_NAME, new DBPediaRowKey(id));
    }

    public DBPedia(RowKey rowKey) {
        super(TABLE_NAME, new DBPediaRowKey(rowKey.toString()));
    }

    public DBPediaLabel getLabel() {
        DBPediaLabel metadata = get(DBPediaLabel.NAME);
        if (metadata == null) {
            addColumnFamily(new DBPediaLabel());
        }
        return get(DBPediaLabel.NAME);
    }

    public DBPediaMappingBasedProperties getMappingBasedProperties() {
        DBPediaMappingBasedProperties metadata = get(DBPediaMappingBasedProperties.NAME);
        if (metadata == null) {
            addColumnFamily(new DBPediaMappingBasedProperties());
        }
        return get(DBPediaMappingBasedProperties.NAME);
    }

    public DBPediaSpecificMappingBasedProperties getSpecificMappingBasedProperties() {
        DBPediaSpecificMappingBasedProperties metadata = get(DBPediaSpecificMappingBasedProperties.NAME);
        if (metadata == null) {
            addColumnFamily(new DBPediaSpecificMappingBasedProperties());
        }
        return get(DBPediaSpecificMappingBasedProperties.NAME);
    }

    public DBPediaGeoCoordinates getGeoCoordinates() {
        DBPediaGeoCoordinates metadata = get(DBPediaGeoCoordinates.NAME);
        if (metadata == null) {
            addColumnFamily(new DBPediaGeoCoordinates());
        }
        return get(DBPediaGeoCoordinates.NAME);
    }

    public DBPediaImage getImage() {
        DBPediaImage metadata = get(DBPediaImage.NAME);
        if (metadata == null) {
            addColumnFamily(new DBPediaImage());
        }
        return get(DBPediaImage.NAME);
    }

    public DBPediaInstanceTypes getInstanceTypes() {
        DBPediaInstanceTypes metadata = get(DBPediaInstanceTypes.NAME);
        if (metadata == null) {
            addColumnFamily(new DBPediaInstanceTypes());
        }
        return get(DBPediaInstanceTypes.NAME);
    }

    public DBPediaWikipediaLinks getWikipediaLinks() {
        DBPediaWikipediaLinks metadata = get(DBPediaWikipediaLinks.NAME);
        if (metadata == null) {
            addColumnFamily(new DBPediaWikipediaLinks());
        }
        return get(DBPediaWikipediaLinks.NAME);
    }

}
