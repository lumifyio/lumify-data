package com.altamiracorp.lumify.web;

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
    public static final String CLIENT_DN_HEADER = "SSL_CLIENT_S_DN";
    public static final String CLIENT_CERT_HEADER = "SSL_CLIENT_CERT";

    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(LdapX509AuthenticationProvider.class);
    private LdapSearchService ldapSearchService;
    private String usernameAttribute;
    private String displayNameAttribute;

    @Inject
    public LdapX509AuthenticationProvider(final UserRepository userRepository, final Graph graph, final LdapSearchService ldapSearchService) {
        super(userRepository, graph);
        this.ldapSearchService = ldapSearchService;

        // TODO: configure these values from .properties
        this.usernameAttribute = "employeeNumber";
        this.displayNameAttribute = "displayName";
    }

    @Override
    protected X509Certificate extractCertificate(HttpServletRequest request) {
        X509Certificate cert = super.extractCertificate(request);
        if (cert != null) {
            LOGGER.info("using cert from " + CERTIFICATE_REQUEST_ATTRIBUTE + " request attribute");
        } else {
            try {
                cert = getHeaderClientCert(request);
            } catch (Exception e) {
                throw new LumifyException("failed to extract cert from request header", e);
            }
            if (cert != null) {
                LOGGER.info("using cert from " + CLIENT_CERT_HEADER + " request header");
                LOGGER.info("request header client DN is " + getHeaderClientDN(request));
            } else {
                LOGGER.error("no certificate found in request attribute or header");
                return null;
            }
        }
        return cert;
    }

    protected User getUser(HttpServletRequest request, X509Certificate cert) {
        SearchResultEntry searchResultEntry = ldapSearchService.search(getHeaderClientDN(request), cert);
        LOGGER.info("searchResultEntry = " + searchResultEntry.toLDIFString());

        String username;
        if (this.usernameAttribute.toLowerCase().equals("dn")) {
            username = super.getUsername(cert);
        } else {
            username = searchResultEntry.getAttributeValue(this.usernameAttribute);
        }

        String displayName = searchResultEntry.getAttributeValue(this.displayNameAttribute);

        User user = getUserRepository().findOrAddUser(username, displayName, X509_USER_PASSWORD, new String[0]);
        LOGGER.info("user is " + user.toString());
        return user;
    }

    @Override
    public boolean login(HttpServletRequest request) {
        return false;
    }

    private String getHeaderClientDN(HttpServletRequest request) {
        String dnComponents = request.getHeader(CLIENT_DN_HEADER);
        if (dnComponents.startsWith("/")) {
            dnComponents = dnComponents.substring("/".length());
        }
        ArrayUtils.reverse(dnComponents.split("/"));
        return StringUtil.join(Arrays.asList(dnComponents), ",");
    }

    private X509Certificate getHeaderClientCert(HttpServletRequest request) throws NoSuchAlgorithmException, CertificateException {
        String pemCertText = request.getHeader(CLIENT_CERT_HEADER);
        pemCertText = pemCertText.replaceAll("-----(BEGIN|END) CERTIFICATE-----", "");
        pemCertText = pemCertText.replaceAll("\\n", "");
        byte[] certBytes = Base64.decodeBase64(pemCertText);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));
    }
}
