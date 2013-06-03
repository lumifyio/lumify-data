package com.altamiracorp.reddawn.ucd;

import org.apache.accumulo.core.security.Authorizations;

public class QueryUser<A extends AuthorizationLabel> {
  public QueryUser(String user, A authorizationLabel) {

  }

  public Authorizations getAuthorizations() {
    // TODO
    return new Authorizations();
  }
}
