package com.altamiracorp.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class WebAppTest {
    private String path = "/foo";
    private RequestHandler handler;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletConfig servletConfig;

    @Before
    public void before() {
        handler = mock(RequestHandler.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        servletConfig = mock(ServletConfig.class);
    }

    @Test
    public void testRouteMatch() throws Exception {
        WebApp app = new WebApp() {
            @Override
            public void setup(ServletConfig config) {
                get(path, handler);
            }
        };
        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn(path);
        app.init(servletConfig);
        app.service(request, response);
        verify(handler).handle(request, response);
    }

    @Test
    public void testRouteMiss() throws Exception {
        WebApp app = new WebApp() {
            @Override
            public void setup(ServletConfig config) {
                get(path, handler);
            }
        };
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn(path);
        app.init(servletConfig);
        app.service(request, response);
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testExceptionInHandler() throws Exception {
        WebApp app = new WebApp() {
            @Override
            public void setup(ServletConfig config) {
                post(path, handler);
            }
        };
        handler = new RequestHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
                throw new Exception("boom");
            }
        };
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn(path);
        app.init(servletConfig);
        try {
            app.service(request, response);
            fail("exception should have been thrown");
        } catch (ServletException s) {
            assertEquals("boom", s.getRootCause().getMessage());
        }
    }

    @Test
    public void testRouteSetupByClass() throws Exception {
        WebApp app = new WebApp() {
            @Override
            public void setup(ServletConfig config) {
                delete(path, TestRequestHandler.class);
            }
        };
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getPathInfo()).thenReturn(path);
        app.init(servletConfig);
        app.service(request, response);
        verify(request).setAttribute("handled", "true");
    }
}
