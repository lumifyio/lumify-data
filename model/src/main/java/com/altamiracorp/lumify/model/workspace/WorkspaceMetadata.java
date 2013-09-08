package com.altamiracorp.lumify.model.workspace;

import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.Value;

public class WorkspaceMetadata extends ColumnFamily {
    public static final String NAME = "metadata";
    public static final String TITLE = "title";

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
}
