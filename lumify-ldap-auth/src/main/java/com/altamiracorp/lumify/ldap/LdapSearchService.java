package com.altamiracorp.lumify.ldap;


import com.altamiracorp.lumify.core.exception.LumifyException;
import com.google.inject.Singleton;
import com.unboundid.ldap.sdk.*;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;

import javax.net.ssl.SSLSocketFactory;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
public class LdapSearchService {
    private LDAPConnectionPool pool;
    private LdapSearchConfiguration ldapSearchConfiguration;

    public LdapSearchService(LdapServerConfiguration serverConfig, LdapSearchConfiguration searchConfig) throws GeneralSecurityException, LDAPException {
        SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
        SSLSocketFactory socketFactory = sslUtil.createSSLSocketFactory();

        if (serverConfig.getFailoverLdapServerHostname() != null) {
            String[] addresses = {serverConfig.getPrimaryLdapServerHostname(), serverConfig.getFailoverLdapServerHostname()};
            int[] ports = {serverConfig.getPrimaryLdapServerPort(), serverConfig.getFailoverLdapServerPort()};
            FailoverServerSet failoverSet = new FailoverServerSet(addresses, ports, socketFactory);
            SimpleBindRequest bindRequest = new SimpleBindRequest(serverConfig.getBindDn(), serverConfig.getBindPassword());
            pool = new LDAPConnectionPool(failoverSet, bindRequest, serverConfig.getMaxConnections());
        } else {
            LDAPConnection ldapConnection = new LDAPConnection(socketFactory, serverConfig.getPrimaryLdapServerHostname(), serverConfig.getPrimaryLdapServerPort(), serverConfig.getBindDn(), serverConfig.getBindPassword());
            pool = new LDAPConnectionPool(ldapConnection, serverConfig.getMaxConnections());
        }

        ldapSearchConfiguration = searchConfig;
    }

    public SearchResultEntry search(String dn, byte[] certificate) throws LDAPException {
        // TODO: support X509Certificate object
        // TODO: populate groups

        // TODO: Filter.createEqualityFilter("dn", dn);
        Filter filter = Filter.create("(" + dn + ")");

        List<String> attributeNames = new ArrayList<String>(ldapSearchConfiguration.getUserAttributes());
        if (certificate != null) {
            attributeNames.add(ldapSearchConfiguration.getUserCertificateAttribute());
        }

        SearchResult results;
        try {
            results = pool.search(ldapSearchConfiguration.getUserSearchBase(), ldapSearchConfiguration.getUserSearchScope(), filter, attributeNames.toArray(new String[attributeNames.size()]));
        } catch (LDAPSearchException lse) {
            if (lse.getResultCode() == ResultCode.NO_SUCH_OBJECT) {
                throw new LumifyException("no results for LDAP search: " + filter, lse);
            }
            throw lse;
        }

        if (results.getEntryCount() == 0) {
            throw new LumifyException("no results for LDAP search: " + filter);
        }

        if (certificate != null) {
            for (SearchResultEntry entry : results.getSearchEntries()) {
                byte[][] entryCertificates = entry.getAttributeValueByteArrays(ldapSearchConfiguration.getUserCertificateAttribute());
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

    public SearchResultEntry search(String dn) throws LDAPException {
        return search(dn, null);
    }



}
