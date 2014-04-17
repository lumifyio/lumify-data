package com.altamiracorp.lumify.web;

import com.altamiracorp.lumify.core.config.Configuration;
import com.altamiracorp.lumify.core.exception.LumifyException;
import com.altamiracorp.lumify.core.model.user.UserRepository;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.ldap.LdapSearchService;
import com.altamiracorp.securegraph.Graph;
import com.google.inject.Inject;
import com.unboundid.ldap.sdk.SearchResultEntry;
import org.apache.accumulo.core.util.StringUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public class LdapX509AuthenticationProvider extends X509AuthenticationProvider {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(LdapX509AuthenticationProvider.class);
    private LdapSearchService ldapSearchService;
    private LdapX509AuthenticationConfiguration ldapX509AuthenticationConfiguration;

    @Inject
    public LdapX509AuthenticationProvider(final UserRepository userRepository, final Graph graph, final LdapSearchService ldapSearchService, final Configuration configuration) {
        super(userRepository, graph);
        this.ldapSearchService = ldapSearchService;

        ldapX509AuthenticationConfiguration = new LdapX509AuthenticationConfiguration();
        configuration.setConfigurables(ldapX509AuthenticationConfiguration, "ldap-x509-authentication");
    }

    @Override
    protected X509Certificate extractCertificate(HttpServletRequest request) {
        X509Certificate cert = super.extractCertificate(request);
        if (cert != null) {
            LOGGER.info("using cert from %s request attribute", CERTIFICATE_REQUEST_ATTRIBUTE);
        } else {
            try {
                cert = getHeaderClientCert(request);
            } catch (Exception e) {
                throw new LumifyException("failed to extract cert from request header", e);
            }
            if (cert != null) {
                LOGGER.info("using cert from %s request header", ldapX509AuthenticationConfiguration.getClientCertHeader());
                LOGGER.info("client dn from %s request header is %s", ldapX509AuthenticationConfiguration.getClientDnHeader(), getHeaderClientDN(request));
            } else {
                LOGGER.error("no certificate found in request attribute %s or request header %s", CERTIFICATE_REQUEST_ATTRIBUTE, ldapX509AuthenticationConfiguration.getClientCertHeader());
                return null;
            }
        }
        return cert;
    }

    @Override
    protected User getUser(HttpServletRequest request, X509Certificate cert) {
        // TODO: use DN from cert because we might be using attributes and not headers
        String dn = getHeaderClientDN(request);

        SearchResultEntry searchResultEntry = ldapSearchService.search(dn, cert);
        LOGGER.info("searchResultEntry is\n" + searchResultEntry.toLDIFString());

        String username;
        if (ldapX509AuthenticationConfiguration.getUsernameAttribute() != null) {
            username = searchResultEntry.getAttributeValue(ldapX509AuthenticationConfiguration.getUsernameAttribute());
        } else {
            username = super.getUsername(cert);
        }

        String displayName;
        if (ldapX509AuthenticationConfiguration.getDisplayNameAttribute() != null) {
            displayName = searchResultEntry.getAttributeValue(ldapX509AuthenticationConfiguration.getDisplayNameAttribute());
        } else {
            displayName = username;
        }

        User user = getUserRepository().findOrAddUser(username, displayName, X509_USER_PASSWORD, new String[0]);
        LOGGER.info("user is %s", user.toString());
        return user;
    }

    @Override
    public boolean login(HttpServletRequest request) {
        return false;
    }

    private String getHeaderClientDN(HttpServletRequest request) {
        String dnComponents = request.getHeader(ldapX509AuthenticationConfiguration.getClientDnHeader());
        if (dnComponents.startsWith("/")) {
            dnComponents = dnComponents.substring("/".length());
        }
        ArrayUtils.reverse(dnComponents.split("/"));
        return StringUtil.join(Arrays.asList(dnComponents), ",");
    }

    private X509Certificate getHeaderClientCert(HttpServletRequest request) throws NoSuchAlgorithmException, CertificateException {
        String pemCertText = request.getHeader(ldapX509AuthenticationConfiguration.getClientCertHeader());
        pemCertText = pemCertText.replaceAll("-----(BEGIN|END) CERTIFICATE-----", "");
        pemCertText = pemCertText.replaceAll("\\n", "");
        byte[] certBytes = Base64.decodeBase64(pemCertText);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));
    }
}
