package com.altamiracorp.lumify.web;

import com.altamiracorp.lumify.core.model.user.UserRepository;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.miniweb.HandlerChain;
import com.altamiracorp.securegraph.Graph;
import com.google.inject.Inject;
import org.apache.commons.codec.binary.Base64;

import javax.naming.InvalidNameException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class LdapX509AuthenticationProvider extends X509AuthenticationProvider {
    public static final String CLIENT_DN_HEADER = "SSL_CLIENT_S_DN";
    public static final String CLIENT_CERT_HEADER = "SSL_CLIENT_CERT";

    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(LdapX509AuthenticationProvider.class);

    @Inject
    public LdapX509AuthenticationProvider(final UserRepository userRepository, final Graph graph) {
        super(userRepository, graph);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        X509Certificate cert = extractCertificate(request);
        if (cert != null) {
            LOGGER.info("using cert from " + CERTIFICATE_REQUEST_ATTRIBUTE + " request attribute");
        } else {
            cert = getClientCert(request);
            if (cert != null) {
                LOGGER.info("using cert from " + CLIENT_CERT_HEADER + " request header");
                LOGGER.info("header client DN = " + getClientDN(request));
            } else {
                LOGGER.error("no certificate found in request attribute or header");
                respondWithAuthenticationFailure(response);
                return;
            }
        }

        if (isInvalid(cert)) {
            respondWithAuthenticationFailure(response);
            return;
        }

        /*
        String username = getUsername(cert);
        if (username == null || username.trim().equals("")) {
            respondWithAuthenticationFailure(response);
            return;
        }

        User authUser = userRepository.findByUserName(username);
        if (authUser == null) {
            authUser = userRepository.addUser(graph.getIdGenerator().nextId().toString(), username, X509_USER_PASSWORD, new String[0]);
        }
        setUser(request, authUser);
        */

        chain.next(request, response);
    }

    @Override
    protected String getUsername(X509Certificate cert) {
        // TODO: use displayname or DN?
        /*
        String dn = cert.getSubjectX500Principal().getName();
        try {
            return getCn(dn);
        } catch (InvalidNameException e) {
            LOGGER.error("Unable to parse CN from X509 certificate DN: %s", dn);
            return null;
        }
        */
        return null;
    }

    @Override
    public boolean login(HttpServletRequest request) {
        return false;
    }

    private String getClientDN(HttpServletRequest request) {
        // /CN=Diane
        return request.getHeader(CLIENT_DN_HEADER);
    }

    private X509Certificate getClientCert(HttpServletRequest request) throws NoSuchAlgorithmException, CertificateException {
        // -----BEGIN CERTIFICATE----- MIIDXDCCAkSgAwIBAgIBBjANBgkqhkiG9w0BAQUFADBWMQswCQYDVQQGEwJVUzER MA8GA1UECBMIVmlyZ2luaWExDzANBgNVBAoTBkx1bWlmeTEjMCEGA1UEAxQaIzM2 MSBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkwHhcNMTQwNDExMTg1NTMyWhcNMTYwNDEw MTg1NTMyWjAQMQ4wDAYDVQQDEwVEaWFuZTCCASIwDQYJKoZIhvcNAQEBBQADggEP ADCCAQoCggEBAN7Nc+6ZnBJdDSHpIIhddYHdG2/OFOjetHs4JZv+vVjQ/R7ZBBNx kc0CCWbzndH+uJiHdRO2sWnSV5I0DazrzJSemC3gM2+RbcXYeP2JIHxa85SofqIT 4W9jzymZwrguHPgGWAfi+rX4t/4oUZXgLfjvXsTx/e6deyKS2I3MRw9MchKiPLiZ ckyKf0WmxW4c5ASMMfhdyM743/T2GSVgecrWU3U3sP+S5w2qtSpwdYv57kmAza3C 5+VG+Rt+Lqx/YRKEFG2GzCmbcR290MQz63txmeLyhJ5ZYGPZCusk3drVyYD+uyOK 2nxPx/6OejLUA01SfMQBYW2g8veUdACBojECAwEAAaN7MHkwCQYDVR0TBAIwADAs BglghkgBhvhCAQ0EHxYdT3BlblNTTCBHZW5lcmF0ZWQgQ2VydGlmaWNhdGUwHQYD VR0OBBYEFHMDYo/PrM3eQ+w2CGriLkhlbvYTMB8GA1UdIwQYMBaAFCZac+S7v8SM AXWZ8r8QEdz+COwqMA0GCSqGSIb3DQEBBQUAA4IBAQCATk1rXoqCgpzDzw2UfaG7 9vPeVt0o7hUFQ3UtcPQWj860cC++sP7Uxs87jqY5QWV+pg3GECDDSzpJuq1Ws8z9 tNP4CffYRDcb14M8t5etrfsYQ9WfL2vFeniffRODLSBsIFwPyIaW92IVi5Ebu7Ru D0U1dlLghy1qnRa169evSPXyG+9DHmOLA+t0ArOD+rnw/1N2eMo2sfE9qoz6UY6w CMTKQdPWlTboY3wuyXKPyvmkR8L45L4NMnrEykYxXBWjj4NdMfaR6AJ9yzV6xD9e oLa6lBqplqv3t0L2h/nLZYfhF94Y+pk/NVvVrrSxK4WDVmjh+or5iRDeuKDc9D1s -----END CERTIFICATE-----
        String pemCertText = request.getHeader(CLIENT_CERT_HEADER);
        pemCertText = pemCertText.replaceAll("-----(BEGIN|END) CERTIFICATE-----", "");
        pemCertText = pemCertText.replaceAll("\\n", "");
        byte[] certBytes = Base64.decodeBase64(pemCertText);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));
    }
}
