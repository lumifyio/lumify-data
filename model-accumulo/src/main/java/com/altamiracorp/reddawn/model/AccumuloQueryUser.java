package com.altamiracorp.reddawn.model;

import org.apache.accumulo.core.security.Authorizations;

public class AccumuloQueryUser extends QueryUser {
    public Authorizations getAuthorizations() {
        // TODO do this for real
        return new Authorizations();
    }
}
