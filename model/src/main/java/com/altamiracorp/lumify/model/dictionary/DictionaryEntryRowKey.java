package com.altamiracorp.lumify.model.dictionary;

import com.altamiracorp.lumify.model.RowKey;
import com.altamiracorp.lumify.core.util.RowKeyHelper;

public class DictionaryEntryRowKey extends RowKey {

    public DictionaryEntryRowKey(String rowKey) {
        super(rowKey);
    }

    public DictionaryEntryRowKey(String tokens, String concept) {
        this(RowKeyHelper.buildMinor(tokens,concept));
    }
}
