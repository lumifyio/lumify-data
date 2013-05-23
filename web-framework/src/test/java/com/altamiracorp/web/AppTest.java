package com.altamiracorp.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class AppTest {
    private String path = "/foo";
    private Handler handler;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @Before
    public void before() {
        handler = mock(Handler.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    public void testRouteMatch() throws Exception {
        App app = new App();
        app.get(path, handler);
        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn(path);
        app.handle(request, response);
        verify(handler).handle(eq(request), eq(response), any(HandlerChain.class));
    }

    @Test
    public void testRouteMiss() throws Exception {
        App app = new App();
        app.get(path, handler);
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn(path);
        app.handle(request, response);
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testExceptionInHandler() throws Exception {
        App app = new App();
        handler = new Handler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
                throw new Exception("boom");
            }
        };
        app.post(path, handler);
        when(request.getMethod()).thenReturn("POST");
        when(request.getPathInfo()).thenReturn(path);
        try {
            app.handle(request, response);
            fail("exception should have been thrown");
        } catch (Exception e) {
            assertEquals("boom", e.getMessage());
        }
    }

    @Test
    public void testRouteSetupByClass() throws Exception {
        App app = new App();
        app.delete(path, TestHandler.class);
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getPathInfo()).thenReturn(path);
        app.handle(request, response);
        verify(request).setAttribute("handled", "true");
    }

    @Test
    public void testMultipleHandlersForRoute() throws Exception {
        App app = new App();
        Handler h2 = new TestHandler();
        app.get(path, h2, handler);
        when(request.getMethod()).thenReturn("GET");
        when(request.getPathInfo()).thenReturn(path);
        app.handle(request, response);
        verify(handler).handle(eq(request), eq(response), any(HandlerChain.class));
    }

    @Test
    public void testMulitpleHandlersForRouteSetupByClass() throws Exception {
        App app = new App();
        app.delete(path, TestHandler.class, TestHandler.class);
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getPathInfo()).thenReturn(path);
        app.handle(request, response);
        verify(request, times(2)).setAttribute("handled", "true");
    }

    @Test
    public void testSettingOfAppInstance() throws Exception {
        App app = new App();
        TestAppAwareHandler handler = new TestAppAwareHandler();
        app.get(path, handler);
        assertNotNull(handler.getApp());
    }
}
