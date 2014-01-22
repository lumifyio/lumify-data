package com.altamiracorp.lumify.demoaccountweb.model;

import com.altamiracorp.bigtable.model.Value;
import com.altamiracorp.bigtable.model.accumulo.AccumuloColumnFamily;

import java.util.Date;

public class DemoAccountUserMetadata extends AccumuloColumnFamily {
    public static final String NAME = "metadata";
    public static final String EMAIL = "email";
    public static final String OPT_IN = "optIn";
    public static final String TOKEN = "token";
    public static final String TOKEN_EXPIRATION = "tokenExpiration";


    public DemoAccountUserMetadata() {
        super(NAME);
    }

    public String getEmail() {
        return Value.toString(get(EMAIL));
    }

    public DemoAccountUserMetadata setEmail(String email) {
        set(EMAIL, email);
        return this;
    }

    public Boolean getOptIn() {
        return get(OPT_IN).toBoolean();
    }

    public DemoAccountUserMetadata setOptIn(Boolean optIn) {
        set(OPT_IN, optIn);
        return this;
    }

    public String getToken() {
        return Value.toString(get(TOKEN));
    }

    public DemoAccountUserMetadata setToken(String token) {
        set(TOKEN, token);
        return this;
    }

    public Date getTokenExpiration() {
        return new Date(Value.toLong(get(TOKEN_EXPIRATION)));
    }

    public DemoAccountUserMetadata setTokenExpiration(Date tokenExpiration) {
        set(TOKEN_EXPIRATION, tokenExpiration.getTime());
        return this;
    }
}
