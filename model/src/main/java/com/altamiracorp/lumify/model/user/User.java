package com.altamiracorp.lumify.model.user;

import com.altamiracorp.lumify.model.Row;
import com.altamiracorp.lumify.model.RowKey;
import org.json.JSONException;
import org.json.JSONObject;

public class User extends Row<UserRowKey> {
    public static final String TABLE_NAME = "atc_user";

    public User(UserRowKey rowKey) {
        super(TABLE_NAME, rowKey);
    }

    public User(RowKey rowKey) {
        super(TABLE_NAME, new UserRowKey(rowKey.toString()));
    }

    public User() {
        super(TABLE_NAME);
    }

    @Override
    public UserRowKey getRowKey() {
        UserRowKey rowKey = super.getRowKey();
        if (rowKey == null) {
            rowKey = new UserRowKey(getMetadata().getUserName());
        }
        return rowKey;
    }

    public UserMetadata getMetadata() {
        UserMetadata userMetadata = get(UserMetadata.NAME);
        if (userMetadata == null) {
            addColumnFamily(new UserMetadata());
        }
        return get(UserMetadata.NAME);
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("rowKey", getRowKey().toString());
            json.put("userName", getMetadata().getUserName());
            json.put("status", getMetadata().getStatus().toString().toLowerCase());
            return json;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
