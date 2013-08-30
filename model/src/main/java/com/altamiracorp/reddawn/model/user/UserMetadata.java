package com.altamiracorp.reddawn.model.user;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Value;

public class UserMetadata extends ColumnFamily {
    public static final String NAME = "metadata";
    public static final String USER_NAME = "userName";
    public static final String STATUS = "status";

    public UserMetadata() {
        super(NAME);
    }

    public String getUserName() {
        return Value.toString(get(USER_NAME));
    }

    public UserMetadata setUserName(String userName) {
        set(USER_NAME, userName);
        return this;
    }

    public UserStatus getStatus() {
        String status = Value.toString(get(STATUS));
        if (status == null) {
            return UserStatus.OFFLINE;
        }
        return UserStatus.valueOf(status);
    }

    public UserMetadata setStatus(UserStatus status) {
        set(STATUS, status.toString());
        return this;
    }
}
