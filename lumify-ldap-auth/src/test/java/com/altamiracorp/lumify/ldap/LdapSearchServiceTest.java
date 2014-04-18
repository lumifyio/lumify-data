package com.altamiracorp.lumify.ldap;

import com.altamiracorp.lumify.core.exception.LumifyException;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.util.ssl.KeyStoreKeyManager;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import com.unboundid.util.ssl.TrustStoreTrustManager;
import org.apache.commons.codec.binary.Base64;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.security.GeneralSecurityException;

import static org.junit.Assert.*;

public class LdapSearchServiceTest {
    private static final String BIND_DN = "cn=root,dc=lumify,dc=io";
    private static final String BIND_PASSWORD = "lumify";
    private static InMemoryDirectoryServer ldapServer;

    @BeforeClass
    public static void setUp() throws Exception {
        KeyStoreKeyManager ksManager = new KeyStoreKeyManager("config/ssl/lumify-vm.lumify.io.jks", "password".toCharArray());
        TrustStoreTrustManager tsManager = new TrustStoreTrustManager("config/ssl/lumify-ca.jks");
        SSLUtil serverSslUtil = new SSLUtil(ksManager, tsManager);
        SSLUtil clientSslUtil = new SSLUtil(new TrustAllTrustManager());
        InMemoryListenerConfig sslConfig = InMemoryListenerConfig.createLDAPSConfig(
                "LDAPS",
                InetAddress.getLoopbackAddress(),
                4636,
                serverSslUtil.createSSLServerSocketFactory(),
                clientSslUtil.createSSLSocketFactory()
        );

        InMemoryDirectoryServerConfig ldapConfig = new InMemoryDirectoryServerConfig("dc=lumify,dc=io");
        ldapConfig.addAdditionalBindCredentials(BIND_DN, BIND_PASSWORD);
        ldapConfig.setListenerConfigs(sslConfig);

        ldapServer = new InMemoryDirectoryServer(ldapConfig);

        ldapServer.importFromLDIF(false, "config/ssl/init.ldif");

        ldapServer.importFromLDIF(false, "config/ssl/users/people.ldif");
        ldapServer.importFromLDIF(false, "config/ssl/users/alice.ldif");
        ldapServer.importFromLDIF(false, "config/ssl/users/bob.ldif");
        ldapServer.importFromLDIF(false, "config/ssl/users/carlos.ldif");
        ldapServer.importFromLDIF(false, "config/ssl/users/diane.ldif");

        ldapServer.importFromLDIF(false, "config/ssl/groups/groups.ldif");
        ldapServer.importFromLDIF(false, "config/ssl/groups/admins.ldif");
        ldapServer.importFromLDIF(false, "config/ssl/groups/a-users.ldif");
        ldapServer.importFromLDIF(false, "config/ssl/groups/b-users.ldif");
        ldapServer.importFromLDIF(false, "config/ssl/groups/c-users.ldif");
        ldapServer.importFromLDIF(false, "config/ssl/groups/d-users.ldif");
        ldapServer.importFromLDIF(false, "config/ssl/groups/all-users.ldif");

        ldapServer.startListening();
    }

    @AfterClass
    public static void tearDown() {
        ldapServer.shutDown(true);
    }

    @Test
    public void searchForAliceWithMatchingCert() throws GeneralSecurityException, LDAPException {
        LdapSearchService service = new LdapSearchServiceImpl(getServerConfig(), getSearchConfig());
        SearchResultEntry result = service.search("cn=Alice", getAliceCert());

        assertNotNull(result);
        assertEquals("cn=Alice,ou=people,dc=lumify,dc=io", result.getDN());
        assertEquals("Alice Smith-Y-", result.getAttributeValue("displayName"));
        assertEquals("3", result.getAttributeValue("employeeNumber"));
        assertArrayEquals(getAliceCert(), result.getAttributeValueBytes("userCertificate;binary"));

        printResult(result);
    }

    @Test
    public void searchForBob() throws GeneralSecurityException, LDAPException {
        LdapSearchService service = new LdapSearchServiceImpl(getServerConfig(), getSearchConfig());
        SearchResultEntry result = service.search("cn=Bob");

        assertNotNull(result);
        assertEquals("cn=Bob,ou=people,dc=lumify,dc=io", result.getDN());
        assertEquals("Bob Maluga-Y-", result.getAttributeValue("displayName"));
        assertEquals("4", result.getAttributeValue("employeeNumber"));

        printResult(result);
    }

    @Test(expected = LumifyException.class)
    public void searchForNonExistentPerson() throws GeneralSecurityException, LDAPException {
        LdapSearchService service = new LdapSearchServiceImpl(getServerConfig(), getSearchConfig());
        service.search("cn=Missing");
    }

    private LdapServerConfiguration getServerConfig() throws LDAPException {
        LdapServerConfiguration serverConfig = new LdapServerConfiguration();
        serverConfig.setPrimaryLdapServerHostname(ldapServer.getConnection().getConnectedAddress());
        serverConfig.setPrimaryLdapServerPort(ldapServer.getListenPort("LDAPS"));
        serverConfig.setMaxConnections(1);
        serverConfig.setBindDn(BIND_DN);
        serverConfig.setBindPassword(BIND_PASSWORD);
        return serverConfig;
    }

    private LdapSearchConfiguration getSearchConfig() {
        LdapSearchConfiguration searchConfig = new LdapSearchConfiguration();
        searchConfig.setUserSearchBase("dc=lumify,dc=io");
        searchConfig.setUserSearchScope("sub");
        searchConfig.setUserAttributes("displayName,employeeNumber,telephoneNumber");
        searchConfig.setUserCertificateAttribute("userCertificate;binary");
        searchConfig.setGroupSearchBase("dc=lumify,dc=io");
        searchConfig.setGroupSearchScope("sub");
        return searchConfig;
    }

    private byte[] getAliceCert() {
        return Base64.decodeBase64(CERT_BASE64_ALICE);
    }

    private static final String CERT_BASE64_ALICE = "MIIDXDCCAkSgAwIBAgIBAzANBgkqhkiG9w0BAQUFADBWMQswCQYDVQQGEwJVUzERMA8GA1UECBMIVmlyZ2luaWExDzANBgNVBAoTBkx1bWlmeTEjMCEGA1UEAxQaIzM2MSBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkwHhcNMTQwNDExMTg1MDQ1WhcNMTYwNDEwMTg1MDQ1WjAQMQ4wDAYDVQQDEwVBbGljZTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMbcBCBeXrbIKVvUNZ7JUwJrhDLYR1SXtqfcgrSASl5j6Ra37wh2PvgVaaG9/l7GvS29a3wSa9+zVsV7XSdR4557E+tfqVqRz9C6q28ZJdUcBLhe2FR9N0XzrtGGg3i/4wzc1ppr7+GS2sQymbeuRmvyory2H09aNTrXqbU2vLbkQoAbpaEiazmxFpIRn1JVi64bvw+Ds/FnIJC7daVlhn6Je6etCatnFET4OmOXlbP+/GnU2KsKbNelKuiGjqvcrF1AEd8D3qPB58vTVMfR/hYtpyuv1K+sHGC8SFQCvefEQMlwlfo6nb/wrn+44SBpEQ8fshmz0w6Zp6sz+L1Sv60CAwEAAaN7MHkwCQYDVR0TBAIwADAsBglghkgBhvhCAQ0EHxYdT3BlblNTTCBHZW5lcmF0ZWQgQ2VydGlmaWNhdGUwHQYDVR0OBBYEFGFD3vCoMiTYdz2jHEb+Fr5LPZUKMB8GA1UdIwQYMBaAFCZac+S7v8SMAXWZ8r8QEdz+COwqMA0GCSqGSIb3DQEBBQUAA4IBAQDSTj/6ZTqT1yPIj8AfGs/JnFOAnDrN0SiJEMm06JHIMF7xotg1Sq+y21+UkUDHgIDXhKqOfElzG6W6mZZV02yHh709EYZGqviVNW+H1AVOWoFLtLEQD9v4VJa5UzIXU+EfxV1eKbNiZ3CO18fuC36QiQFewnPTU+TwRsuKuFM1aJQyPxthQmkbrKqWDcV6YVSlSUsFRpb77wnTC/o+ogjR02ekrqugp7OfrM0WMMERrALl432FkXc5ZNbjhbNry1BwS0qbTBW3CCuvInMNb4AbQP3Q+3mIEqPGDUxOznLKTKpRhoxDdZWEGs0rXNOE6rqjAL62em6KyRGF0z9vVshh";

    private void printResult(SearchResultEntry result) {
        System.out.println();
        System.out.println(result.getDN());
        for (Attribute attribute : result.getAttributes()) {
            System.out.println(attributeToString(attribute));
        }
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
}
