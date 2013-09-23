package com.altamiracorp.lumify.model.dictionary;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.*;
import com.google.inject.Inject;

import java.util.Collection;

public class DictionaryEntryRepository extends Repository<DictionaryEntry>{

    @Inject
    public DictionaryEntryRepository(ModelSession modelSession) {
        super(modelSession);
    }

    @Override
    public DictionaryEntry fromRow(Row row) {
        DictionaryEntry dictionaryEntry = new DictionaryEntry(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(DictionaryEntryMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                dictionaryEntry.addColumnFamily(new DictionaryEntryMetadata().addColumns(columns));
            } else {
                dictionaryEntry.addColumnFamily(columnFamily);
            }
        }
        return dictionaryEntry;
    }

    @Override
    public Row toRow(DictionaryEntry dictionaryEntry) {
        return dictionaryEntry;
    }

    @Override
    public String getTableName() {
        return DictionaryEntry.TABLE_NAME;
    }

    public DictionaryEntry createNew (String tokens, String concept) {
        return createNew(tokens, concept, null);
    }

    public DictionaryEntry createNew (String tokens, String concept, String resolvedName) {
        DictionaryEntry entry = new DictionaryEntry(new DictionaryEntryRowKey(tokens,concept));
        DictionaryEntryMetadata metadata = new DictionaryEntryMetadata()
                .setTokens(tokens)
                .setConcept(concept);

        if (resolvedName != null) {
            metadata.setResolvedName(resolvedName);
        }
        entry.addColumnFamily(metadata);

        return entry;
    }

    public void saveNew (String tokens, String concept, String resolvedName, User user) {
        this.save(createNew(tokens,concept,resolvedName), user);
    }

    public void saveNew (String tokens, String concept, User user) {
        this.save(createNew(tokens,concept), user);
    }
}
