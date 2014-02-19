package com.altamiracorp.lumify.account.model;

import com.altamiracorp.bigtable.model.ColumnFamily;
import com.altamiracorp.bigtable.model.Value;

import java.util.Date;

public class AccountUserData extends ColumnFamily {
    public static final String NAME = "data";
    public static final String EMAIL = "email";
    public static final String OPT_IN = "optIn";
    public static final String TOKEN = "token";
    public static final String RESET = "reset";
    public static final String TOKEN_EXPIRATION = "tokenExpiration";


    public AccountUserData() {
        super(NAME);
    }

    public String getEmail() {
        return Value.toString(get(EMAIL));
    }

    public AccountUserData setEmail(String email) {
        set(EMAIL, email);
        return this;
    }

    public Boolean getOptIn() {
        return get(OPT_IN).toBoolean();
    }

    public AccountUserData setOptIn(Boolean optIn) {
        set(OPT_IN, optIn);
        return this;
    }

    public Boolean getReset() {
        return get(RESET).toBoolean();
    }

    public AccountUserData setReset(Boolean reset) {
        set(RESET, reset);
        return this;
    }

    public String getToken() {
        return Value.toString(get(TOKEN));
    }

    public AccountUserData setToken(String token) {
        set(TOKEN, token);
        return this;
    }

    public Date getTokenExpiration() {
        return new Date(Value.toLong(get(TOKEN_EXPIRATION)));
    }

    public AccountUserData setTokenExpiration(Date tokenExpiration) {
        set(TOKEN_EXPIRATION, tokenExpiration.getTime());
        return this;
    }
}
