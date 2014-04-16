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
            LOGGER.info("using cert from %s request attribute", CERTIFICATE_REQUEST_ATTRIBUTE);
        } else {
            try {
                cert = getHeaderClientCert(request);
            } catch (Exception e) {
                throw new LumifyException("failed to extract cert from request header", e);
            }
            if (cert != null) {
                LOGGER.info("using cert from %s request header", CLIENT_CERT_HEADER);
                LOGGER.info("client dn from %s request header is %s", CLIENT_DN_HEADER, getHeaderClientDN(request));
            } else {
                LOGGER.error("no certificate found in request attribute %s or request header %s", CERTIFICATE_REQUEST_ATTRIBUTE, CLIENT_CERT_HEADER);
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
        if (this.usernameAttribute != null) {
            username = searchResultEntry.getAttributeValue(this.usernameAttribute);
        } else {
            username = super.getUsername(cert);
        }

        String displayName = searchResultEntry.getAttributeValue(this.displayNameAttribute);

        User user = getUserRepository().findOrAddUser(username, displayName, X509_USER_PASSWORD, new String[0]);
        LOGGER.info("user is %s", user.toString());
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
