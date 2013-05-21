package com.altamiracorp.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class RouterTest {
    private Router router;
    private RequestHandler handler;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String path = "/foo";

    @Before
    public void before() {
        router = new Router();
        handler = mock(RequestHandler.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    public void testSimpleRoute() throws Exception {
        router.addRoute(Router.Method.GET, path, handler);
        when(request.getMethod()).thenReturn(Router.Method.GET.toString());
        when(request.getPathInfo()).thenReturn(path);
        router.route(request, response);
        verify(handler).handle(request, response);
    }

    @Test
    public void testRouteWithComponent() throws Exception {
        router.addRoute(Router.Method.GET, path + "/:id/text", handler);
        when(request.getMethod()).thenReturn(Router.Method.GET.toString());
        when(request.getPathInfo()).thenReturn(path + "/25/text");
        router.route(request, response);
        verify(handler).handle(request, response);
        verify(request).setAttribute("id", "25");
    }

    @Test
    public void testRouteMissingDueToMethod() throws Exception {
        router.addRoute(Router.Method.GET, path, handler);
        when(request.getMethod()).thenReturn(Router.Method.POST.toString());
        when(request.getPathInfo()).thenReturn(path);
        router.route(request, response);
        verify(handler).handle(request, response);
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
}
