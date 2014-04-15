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
import org.apache.commons.codec.binary.Base64;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class LdapX509AuthenticationProvider extends X509AuthenticationProvider {
    public static final String CLIENT_DN_HEADER = "SSL_CLIENT_S_DN";
    public static final String CLIENT_CERT_HEADER = "SSL_CLIENT_CERT";

    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(LdapX509AuthenticationProvider.class);
    private LdapSearchService ldapSearchService;

    @Inject
    public LdapX509AuthenticationProvider(final UserRepository userRepository, final Graph graph, final LdapSearchService ldapSearchService) {
        super(userRepository, graph);
        this.ldapSearchService = ldapSearchService;
    }

    @Override
    protected X509Certificate extractCertificate(HttpServletRequest request) {
        X509Certificate cert = super.extractCertificate(request);
        if (cert != null) {
            LOGGER.info("using cert from " + CERTIFICATE_REQUEST_ATTRIBUTE + " request attribute");
        } else {
            try {
                cert = getClientCert(request);
            } catch (Exception e) {
                throw new LumifyException("failed to extract cert from request header", e);
            }
            if (cert != null) {
                LOGGER.info("using cert from " + CLIENT_CERT_HEADER + " request header");
                LOGGER.info("header client DN = " + getClientDN(request));
            } else {
                LOGGER.error("no certificate found in request attribute or header");
                return null;
            }
        }
        return cert;
    }

    @Override
    protected void modifyUser(User authUser, X509Certificate cert, HttpServletRequest request) {
        String dn = getClientDN(request);
        if (dn.startsWith("/")) {
            dn = dn.substring("/".length());
        }
        SearchResultEntry searchResultEntry = ldapSearchService.search(dn, cert);
        LOGGER.info("searchResultEntry = " + searchResultEntry.toLDIFString());
    }

    @Override
    public boolean login(HttpServletRequest request) {
        return false;
    }

    private String getClientDN(HttpServletRequest request) {
        return request.getHeader(CLIENT_DN_HEADER);
    }

    private X509Certificate getClientCert(HttpServletRequest request) throws NoSuchAlgorithmException, CertificateException {
        String pemCertText = request.getHeader(CLIENT_CERT_HEADER);
        pemCertText = pemCertText.replaceAll("-----(BEGIN|END) CERTIFICATE-----", "");
        pemCertText = pemCertText.replaceAll("\\n", "");
        byte[] certBytes = Base64.decodeBase64(pemCertText);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));
    }
}
