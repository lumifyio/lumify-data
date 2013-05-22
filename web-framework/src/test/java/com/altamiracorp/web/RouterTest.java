package com.altamiracorp.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.altamiracorp.web.Route.Method;

import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class RouterTest {
    private Router router;
    private Handler handler;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String path = "/foo";

    @Before
    public void before() {
        router = new Router();
        handler = mock(Handler.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    public void testSimpleRoute() throws Exception {
        router.addRoute(Method.GET, path, handler);
        when(request.getMethod()).thenReturn(Method.GET.toString());
        when(request.getPathInfo()).thenReturn(path);
        router.route(request, response);
        verify(handler).handle(eq(request), eq(response), any(HandlerChain.class));
    }

    @Test
    public void testRouteWithComponent() throws Exception {
        router.addRoute(Method.GET, path + "/{id}/text", handler);
        when(request.getMethod()).thenReturn(Method.GET.toString());
        when(request.getPathInfo()).thenReturn(path + "/25/text");
        router.route(request, response);
        verify(handler).handle(eq(request), eq(response), any(HandlerChain.class));
        verify(request).setAttribute("id", "25");
    }

    @Test
    public void testRouteMissingDueToMethod() throws Exception {
        router.addRoute(Method.GET, path, handler);
        when(request.getMethod()).thenReturn(Method.POST.toString());
        when(request.getPathInfo()).thenReturn(path);
        router.route(request, response);
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testRouteMissingDueToPath() throws Exception {
        router.addRoute(Method.GET, path, handler);
        when(request.getMethod()).thenReturn(Method.GET.toString());
        when(request.getPathInfo()).thenReturn(path + "extra");
        router.route(request, response);
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
