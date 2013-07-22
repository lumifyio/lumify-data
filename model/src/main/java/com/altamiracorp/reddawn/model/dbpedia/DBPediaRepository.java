package com.altamiracorp.reddawn.model.dbpedia;

import com.altamiracorp.reddawn.model.*;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DBPediaRepository extends Repository<DBPedia> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBPediaRepository.class.getName());
    private static final Map<String, String> conceptLabelMap;

    static {
        conceptLabelMap = new HashMap<String, String>();
        conceptLabelMap.put("http://www.w3.org/2002/07/owl#Thing", "");
        conceptLabelMap.put("http://dbpedia.org/ontology/Software", "");
        conceptLabelMap.put("http://schema.org/CreativeWork", "");
        conceptLabelMap.put("http://dbpedia.org/ontology/Organisation", "organization");
        conceptLabelMap.put("http://schema.org/Organization", "organization");
        conceptLabelMap.put("http://dbpedia.org/ontology/Company", "organization");
        conceptLabelMap.put("http://dbpedia.org/ontology/Agent", "");
        conceptLabelMap.put("http://dbpedia.org/ontology/Person", "person");
        conceptLabelMap.put("http://schema.org/Person", "person");
        conceptLabelMap.put("http://dbpedia.org/ontology/Place", "location");
        conceptLabelMap.put("http://schema.org/Place", "location");
    }

    @Override
    public DBPedia fromRow(Row row) {
        DBPedia dbPedia = new DBPedia(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(DBPediaGeoCoordinates.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                dbPedia.addColumnFamily(new DBPediaGeoCoordinates().addColumns(columns));
            } else if (columnFamily.getColumnFamilyName().equals(DBPediaImage.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                dbPedia.addColumnFamily(new DBPediaImage().addColumns(columns));
            } else if (columnFamily.getColumnFamilyName().equals(DBPediaInstanceTypes.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                dbPedia.addColumnFamily(new DBPediaInstanceTypes().addColumns(columns));
            } else if (columnFamily.getColumnFamilyName().equals(DBPediaLabel.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                dbPedia.addColumnFamily(new DBPediaLabel().addColumns(columns));
            } else if (columnFamily.getColumnFamilyName().equals(DBPediaMappingBasedProperties.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                dbPedia.addColumnFamily(new DBPediaMappingBasedProperties().addColumns(columns));
            } else if (columnFamily.getColumnFamilyName().equals(DBPediaSpecificMappingBasedProperties.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                dbPedia.addColumnFamily(new DBPediaSpecificMappingBasedProperties().addColumns(columns));
            } else {
                dbPedia.addColumnFamily(columnFamily);
            }
        }
        return dbPedia;
    }

    @Override
    public Row toRow(DBPedia dbpedia) {
        return dbpedia;
    }

    @Override
    public String getTableName() {
        return DBPedia.TABLE_NAME;
    }

    public String getConceptLabel(DBPedia dbpedia) {
        for (Column column : dbpedia.getInstanceTypes().getColumns()) {
            String dbpediaConceptLabel = column.getValue().toString();
            String conceptLabel = conceptLabelMap.get(dbpediaConceptLabel);
            if (conceptLabel == null) {
                LOGGER.warn("Unknown concept label: " + dbpediaConceptLabel);
                continue;
            }
            if (conceptLabel.length() > 0) {
                return conceptLabel;
            }
        }
        return null;
    }

    public TermRowKey findTermRowKeyByDBPediaRowKey(Session session, String dbpediaRowKey) {
        Map<String, String> columnsToReturn = new HashMap<String, String>();
        columnsToReturn.put(DBPediaLabel.NAME, DBPediaLabel.LABEL_COLUMN);
        columnsToReturn.put(DBPediaInstanceTypes.NAME, "*");

        DBPedia dbpedia = findByRowKey(session, dbpediaRowKey, columnsToReturn);
        if (dbpedia == null) {
            return null;
        }

        String sign = dbpedia.getLabel().getLabel();
        if (sign == null || sign.length() == 0) {
            return null;
        }

        String conceptLabel = getConceptLabel(dbpedia);
        if (conceptLabel == null || conceptLabel.length() == 0) {
            return null;
        }

        return new TermRowKey(sign, TermRowKey.DBPEDIA_MODEL_KEY, conceptLabel);
    }
}
