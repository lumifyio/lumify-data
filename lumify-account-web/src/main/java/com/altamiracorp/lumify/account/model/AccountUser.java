package com.altamiracorp.lumify.account.model;

import com.altamiracorp.bigtable.model.Row;
import com.altamiracorp.bigtable.model.RowKey;

public class AccountUser extends Row<AccountUserRowKey> {
    public static final String TABLE_NAME = "account_user";

    public AccountUser(AccountUserRowKey rowKey) {
        super(TABLE_NAME, rowKey);
    }

    public AccountUser(RowKey rowKey) {
        super(TABLE_NAME, new AccountUserRowKey(rowKey.toString()));
    }

    public AccountUser() {
        super(TABLE_NAME);
    }

    @Override
    public AccountUserRowKey getRowKey() {
        AccountUserRowKey rowKey = super.getRowKey();
        if (rowKey == null) {
            rowKey = new AccountUserRowKey(getData().getEmail().toLowerCase());
        }
        return rowKey;
    }


    public AccountUserData getData() {
        AccountUserData userData = get(AccountUserData.NAME);
        if (userData == null) {
            addColumnFamily(new AccountUserData());
        }
        return get(AccountUserData.NAME);
    }

}
