package com.altamiracorp.reddawn.model.workspace;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Value;

public class WorkspaceContent extends ColumnFamily {
    public static final String NAME = "content";
    public static final String DATA = "data";

    public WorkspaceContent() {
        super(NAME);
    }

    public String getData() {
        return Value.toString(get(DATA));
    }

    public WorkspaceContent setData(String data) {
        set(DATA, data);
        return this;
    }
}
