package com.altamiracorp.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Matchers;

import com.google.inject.Injector;

@RunWith(JUnit4.class)
public class AppTest {
    private String path = "/foo";
    private Handler handler;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher requestDispatcher;
    private ServletContext servletContext;
    private ServletConfig servletConfig;
    private Injector injectorMock;
    private App app;

    @Before
    public void before() {
        handler = mock(Handler.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        requestDispatcher = mock(RequestDispatcher.class);
        servletContext = mock(ServletContext.class);
        servletConfig = mock(ServletConfig.class);
        injectorMock = mock(Injector.class);

        app = new App(servletConfig, injectorMock);

        when(injectorMock.getInstance(Matchers.<Class<Object>>any())).thenReturn(new TestHandler());
        when(servletConfig.getServletContext()).thenReturn(servletContext);
    }

    @Test
    public void testRouteMatch() throws Exception {
        app.get(path, handler);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn(path);
        app.handle(request, response);
        verify(handler).handle(eq(request), eq(response), any(HandlerChain.class));
    }

    @Test
    public void testRouteMiss() throws Exception {
        app.get(path, handler);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn(path);
        when(servletContext.getNamedDispatcher(anyString())).thenReturn(requestDispatcher);
        app.handle(request, response);
        verify(requestDispatcher).forward(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testExceptionInHandler() throws Exception {
        handler = new Handler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
                throw new Exception("boom");
            }
        };
        app.post(path, handler);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn(path);
        try {
            app.handle(request, response);
            fail("exception should have been thrown");
        } catch (Exception e) {
            assertEquals("boom", e.getMessage());
        }
    }

    @Test
    public void testRouteSetupByClass() throws Exception {
        app.delete(path, TestHandler.class);
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRequestURI()).thenReturn(path);
        app.handle(request, response);
        verify(request).setAttribute("handled", "true");
    }

    @Test
    public void testMultipleHandlersForRoute() throws Exception {
        Handler h2 = new TestHandler();
        app.get(path, h2, handler);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn(path);
        app.handle(request, response);
        verify(handler).handle(eq(request), eq(response), any(HandlerChain.class));
    }

    @Test
    public void testMulitpleHandlersForRouteSetupByClass() throws Exception {
        app.delete(path, TestHandler.class, TestHandler.class);
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRequestURI()).thenReturn(path);
        app.handle(request, response);
        verify(request, times(2)).setAttribute("handled", "true");
    }

    @Test
    public void testSettingOfAppInstance() throws Exception {
        TestAppAwareHandler handler = new TestAppAwareHandler();
        app.get(path, handler);
        assertNotNull(handler.getApp());
    }
}
