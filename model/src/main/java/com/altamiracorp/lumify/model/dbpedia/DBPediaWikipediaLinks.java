package com.altamiracorp.lumify.model.dbpedia;

import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.Value;

public class DBPediaWikipediaLinks extends ColumnFamily {
    public static final String NAME = "WikipediaLinks";
    public static final String URL_COLUMN = "Url";

    public DBPediaWikipediaLinks() {
        super(NAME);
    }

    public String getUrl() {
        return Value.toString(get(URL_COLUMN));
    }

    public DBPediaWikipediaLinks setUrl(String url) {
        set(URL_COLUMN, url);
        return this;
    }

}
