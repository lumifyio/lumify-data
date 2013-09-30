package com.altamiracorp.lumify.model.workspace;

import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.Value;

public class WorkspaceMetadata extends ColumnFamily {
    public static final String NAME = "metadata";
    public static final String TITLE = "title";
    public static final String CREATOR = "creator";

    public WorkspaceMetadata() {
        super(NAME);
    }

    public String getTitle() {
        return Value.toString(get(TITLE));
    }

    public WorkspaceMetadata setTitle(String title) {
        set(TITLE, title);
        return this;
    }

    public String getCreator() {
        return Value.toString(get(CREATOR));
    }

    public WorkspaceMetadata setCreator(String creator) {
        set(CREATOR, creator);
        return this;
    }
}
