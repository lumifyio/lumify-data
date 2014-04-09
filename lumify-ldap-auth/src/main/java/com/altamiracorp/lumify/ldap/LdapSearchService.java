package com.altamiracorp.lumify.ldap;


import com.altamiracorp.lumify.core.exception.LumifyException;
import com.google.common.collect.Lists;
import com.unboundid.ldap.sdk.*;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import org.apache.commons.codec.binary.Base64;

import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

public class LdapSearchService {
    public static final int NO_SUCH_OBJECT_RESULT_CODE = 32;
    private static final int DEFAULT_MAX_CONNECTIONS = 10;
    private static final String DEFAULT_CERTIFICATE_ATTRIBUTE = "userCertificate;binary";

    private LDAPConnectionPool pool;
    private SearchConfiguration searchConfiguration = new SearchConfiguration();

    public static void main(String[] args) throws LDAPException, GeneralSecurityException, IOException {
        LdapSearchService ldapSearchService = new LdapSearchService();

        ldapSearchService.initializePool("192.168.33.10", 636, "192.168.33.10", 636, DEFAULT_MAX_CONNECTIONS, "cn=root,dc=lumify,dc=io", "lumify");

        ldapSearchService.searchConfiguration.searchBase = "dc=lumify,dc=io";
        ldapSearchService.searchConfiguration.searchScope = SearchScope.SUB;
        ldapSearchService.searchConfiguration.certificateAttributeName = DEFAULT_CERTIFICATE_ATTRIBUTE;
        ldapSearchService.searchConfiguration.attributeNames = Lists.newArrayList("displayName", "employeeNumber", "telephoneNumber");

        SearchResultEntry result;

        if (args.length == 1) {
            byte[] cert = org.apache.commons.io.FileUtils.readFileToByteArray(new File(args[0]));
            result = ldapSearchService.search("cn=Alice", cert);
        } else {
            result = ldapSearchService.search("cn=Alice");
        }
        System.out.println();
        System.out.println(result.getDN());
        for (Attribute attribute : result.getAttributes()) {
            System.out.println(ldapSearchService.attributeToString(attribute));
        }

        result = ldapSearchService.search("cn=Bob");
        System.out.println();
        System.out.println(result.getDN());
        for (Attribute attribute : result.getAttributes()) {
            System.out.println(ldapSearchService.attributeToString(attribute));
        }
    }

    private void initializePool(String primaryLdapServerHostname, int primaryLdapServerPort, String failoverLdapServerHostname, int failoverLadpServerPort, int maxConnections, String bindDn, String bindPassword) throws LDAPException, GeneralSecurityException {
        String[] addresses = {primaryLdapServerHostname, failoverLdapServerHostname};
        int[] ports = {primaryLdapServerPort, failoverLadpServerPort};

        SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
        SSLSocketFactory socketFactory = sslUtil.createSSLSocketFactory();

        FailoverServerSet failoverSet = new FailoverServerSet(addresses, ports, socketFactory);
        SimpleBindRequest bindRequest = new SimpleBindRequest(bindDn, bindPassword);
        pool = new LDAPConnectionPool(failoverSet, bindRequest, maxConnections);
    }

    private SearchResultEntry search(String dn, byte[] certificate) throws LDAPException {
        if (pool == null) {
            throw new LumifyException("the LDAP connection pool in uninitialized");
        }
        if (!searchConfiguration.isValid()) {
            throw new LumifyException("the LDAP search configuration is invalid");
        }
        if (certificate != null && !searchConfiguration.certificateAttributeNameConfigured()) {
            throw new LumifyException("certificate matching requested but certificate attribute name is not configured");
        }

        // TODO: Filter.createEqualityFilter("dn", dn);
        Filter filter = Filter.create("(" + dn + ")");

        List<String> attributeNames = searchConfiguration.attributeNames;
        if (certificate != null) {
            attributeNames.add(searchConfiguration.certificateAttributeName);
        }

        SearchResult results;
        try {
            results = pool.search(searchConfiguration.searchBase, searchConfiguration.searchScope, filter, attributeNames.toArray(new String[attributeNames.size()]));
        } catch (LDAPSearchException lse) {
            if (lse.getResultCode().intValue() == NO_SUCH_OBJECT_RESULT_CODE) {
                throw new LumifyException("no results for LDAP search: " + filter, lse);
            }
            throw lse;
        }

        if (results.getEntryCount() == 0) {
            throw new LumifyException("no results for LDAP search: " + filter);
        }

        if (certificate != null) {
            for (SearchResultEntry entry : results.getSearchEntries()) {
                byte[][] entryCertificates = entry.getAttributeValueByteArrays(searchConfiguration.certificateAttributeName);
                for (byte[] entryCertificate : entryCertificates) {
                    if (Arrays.equals(entryCertificate, certificate)) {
                        return entry;
                    }
                }
            }
            throw new LumifyException("no results with matching certificate for LDAP search: " + filter);
        } else {
            if (results.getEntryCount() > 1) {
                throw new LumifyException("certificate matching not requested and more than one result for LDAP search: " + filter);
            }
            return results.getSearchEntries().get(0);
        }
    }

    private SearchResultEntry search(String dn) throws LDAPException {
        return search(dn, null);
    }

    private String attributeToString(Attribute attribute) {
        StringBuilder sb = new StringBuilder();
        sb.append(attribute.getName());
        sb.append(":");

        if (attribute.getName().endsWith(";binary")) {
            if (attribute.size() > 1) {
                for (byte[] value : attribute.getValueByteArrays()) {
                    sb.append("\n  ");
                    sb.append(Base64.encodeBase64String(value));
                    sb.append(" (");
                    sb.append(value.length);
                    sb.append(" bytes)");
                }
            } else {
                byte[] value = attribute.getValueByteArray();
                sb.append(" ");
                sb.append(Base64.encodeBase64String(value));
                sb.append(" (");
                sb.append(value.length);
                sb.append(" bytes)");
            }
        } else {
            if (attribute.size() > 1) {
                for (String value : attribute.getValues()) {
                    sb.append("\n  ");
                    sb.append(value);
                }
            } else {
                sb.append(" ");
                sb.append(attribute.getValue());
            }
        }

        return sb.toString();
    }

    private static class SearchConfiguration {
        // required
        protected String searchBase;
        protected SearchScope searchScope;
        protected List<String> attributeNames;

        // optional
        protected String certificateAttributeName;

        protected boolean isValid() {
            return searchBase != null && searchBase.length() > 0
                    && searchScope != null
                    && attributeNames != null && attributeNames.size() > 0;
        }

        protected boolean certificateAttributeNameConfigured() {
            return certificateAttributeName != null && certificateAttributeName.length() > 0;
        }
    }
}
