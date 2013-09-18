package com.altamiracorp.lumify.web;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.Session;
import com.altamiracorp.web.HandlerChain;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import static org.mockito.Mockito.*;

public class X509AuthenticationProviderTest {
    public static final String X509_REQ_ATTR_NAME = "javax.servlet.request.X509Certificate";
    public static final String TEST_USERNAME = "testuser";

    private X509AuthenticationProvider mock;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HandlerChain chain;
    @Mock
    private WebApp app;
    @Mock
    private AppSession appSession;
    @Mock
    private Session modelSession;

    @Before
    public void setupTests() {
        mock = Mockito.mock(X509AuthenticationProvider.class);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testNoCertificateAvailable() throws Exception {
        when(request.getAttribute(X509_REQ_ATTR_NAME)).thenReturn(null);
        doCallRealMethod().when(mock).handle(request, response, chain);
        mock.handle(request, response, chain);
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void testEmptyCertificateArrayAvailable() throws Exception {
        X509Certificate[] certs = new X509Certificate[]{};
        when(request.getAttribute(X509_REQ_ATTR_NAME)).thenReturn(certs);
        doCallRealMethod().when(mock).handle(request, response, chain);
        mock.handle(request, response, chain);
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Ignore("Un-ignore after Sep 19th")
    public void testExpiredCertificate() throws Exception {
        X509Certificate cert = getCertificate("expired");
        X509Certificate[] certs = new X509Certificate[]{cert};
        when(request.getAttribute(X509_REQ_ATTR_NAME)).thenReturn(certs);
        when(mock.getUsername(cert)).thenReturn(TEST_USERNAME);
        doCallRealMethod().when(mock).handle(request, response, chain);
        mock.handle(request, response, chain);
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Ignore("Implement after refactor that injects repository objects")
    public void testValidCertificate() throws Exception {
        // TODO: Implement after refactor that injects repository objects
    }

    private X509Certificate getCertificate(String name) {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            InputStream ksis = X509AuthenticationProviderTest.class.getResourceAsStream("/" + name + ".jks");
            ks.load(ksis, "password".toCharArray());
            return (X509Certificate) ks.getCertificate(name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
