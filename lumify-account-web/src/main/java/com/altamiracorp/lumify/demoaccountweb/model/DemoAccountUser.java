package com.altamiracorp.lumify.demoaccountweb.model;

import com.altamiracorp.bigtable.model.Row;
import com.altamiracorp.bigtable.model.RowKey;

public class DemoAccountUser extends Row<DemoAccountUserRowKey> {
    public static final String TABLE_NAME = "demo_account_user";

    public DemoAccountUser(DemoAccountUserRowKey rowKey) {
        super(TABLE_NAME, rowKey);
    }

    public DemoAccountUser(RowKey rowKey) {
        super(TABLE_NAME, new DemoAccountUserRowKey(rowKey.toString()));
    }

    public DemoAccountUser() {
        super(TABLE_NAME);
    }

    @Override
    public DemoAccountUserRowKey getRowKey() {
        DemoAccountUserRowKey rowKey = super.getRowKey();
        if (rowKey == null) {
            rowKey = new DemoAccountUserRowKey(getMetadata().getEmail().toLowerCase());
        }
        return rowKey;
    }


    public DemoAccountUserMetadata getMetadata() {
        DemoAccountUserMetadata userMetadata = get(DemoAccountUserMetadata.NAME);
        if (userMetadata == null) {
            addColumnFamily(new DemoAccountUserMetadata());
        }
        return get(DemoAccountUserMetadata.NAME);
    }

}
