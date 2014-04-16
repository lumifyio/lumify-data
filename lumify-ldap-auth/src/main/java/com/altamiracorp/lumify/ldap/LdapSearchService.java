package com.altamiracorp.lumify.ldap;

import com.unboundid.ldap.sdk.SearchResultEntry;

import java.security.cert.X509Certificate;

public interface LdapSearchService {
    SearchResultEntry search(String dn, X509Certificate cert);

    SearchResultEntry search(String dn, byte[] cert);

    SearchResultEntry search(String dn);
}
