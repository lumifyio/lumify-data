package com.altamiracorp.lumify.web;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class BaseRequestHandlerTest {

    private static final JSONObject JSON_OBJECT = new JSONObject();
    private static final String TEST_PARAM = "foo";
    private static final String TEST_PARAM_VALUE = "1";

    private BaseRequestHandler mock;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PrintWriter writer;

    @Before
    public void setupTests() {
        mock = Mockito.mock(BaseRequestHandler.class, Mockito.CALLS_REAL_METHODS);

        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = NullPointerException.class)
    public void testRequiredParameterInvalidRequest() {
        mock.getRequiredParameter(null, TEST_PARAM);
    }

    @Test(expected = RuntimeException.class)
    public void testRequiredParameterInvalidParameter() {
        mock.getRequiredParameter(request, null);
    }

    @Test(expected = RuntimeException.class)
    public void testRequiredParameterEmptyParameter() {
        mock.getRequiredParameter(request, "");
    }

    @Test
    public void testRequiredParameter() {
        when(request.getParameter(TEST_PARAM)).thenReturn(TEST_PARAM_VALUE);
        assertEquals(TEST_PARAM_VALUE, mock.getRequiredParameter(request, TEST_PARAM));
        verify(request, times(1)).getParameter(eq(TEST_PARAM));
    }


    @Test
    public void testRequiredParameterAsLong() {
        when(request.getParameter(TEST_PARAM)).thenReturn(TEST_PARAM_VALUE);
        assertEquals(Long.parseLong(TEST_PARAM_VALUE), mock.getRequiredParameterAsLong(request, TEST_PARAM));
        verify(request, times(1)).getParameter(eq(TEST_PARAM));
    }

    @Test
    public void testRequiredParameterAsDouble() {
        when(request.getParameter(TEST_PARAM)).thenReturn(TEST_PARAM_VALUE);
        assertEquals(Double.parseDouble(TEST_PARAM_VALUE), mock.getRequiredParameterAsDouble(request, TEST_PARAM), 0.001);
        verify(request, times(1)).getParameter(eq(TEST_PARAM));
    }

    @Test(expected = NullPointerException.class)
    public void testOptionalParameterInvalidRequest() {
        mock.getOptionalParameter(null, TEST_PARAM);
    }

    @Test
    public void testOptionalParameterFound() {
        when(request.getParameter(TEST_PARAM)).thenReturn(TEST_PARAM_VALUE);
        assertEquals(TEST_PARAM_VALUE, mock.getOptionalParameter(request, TEST_PARAM));
        verify(request, times(1)).getParameter(eq(TEST_PARAM));
    }

    @Test
    public void testOptionalParameterNotFound() {
        when(request.getParameter(TEST_PARAM)).thenReturn(null);
        assertEquals(null, mock.getOptionalParameter(request, TEST_PARAM));
        verify(request, times(1)).getParameter(eq(TEST_PARAM));
    }

    @Test(expected = NullPointerException.class)
    public void testRespondWithJsonInvalidResponse() {
        mock.respondWithJson(null, JSON_OBJECT);
    }

    @Test(expected = NullPointerException.class)
    public void testRespondWithJsonInvalidJsonObject() {
        mock.respondWithJson(response, null);
    }

    @Test
    public void testRespondWithJson() throws IOException {
        when(response.getWriter()).thenReturn(writer);
        mock.respondWithJson(response, JSON_OBJECT);
    }

    @Test(expected = RuntimeException.class)
    public void testRespondWithJsonWriterException() throws IOException {
        when(response.getWriter()).thenThrow(new IOException());
        mock.respondWithJson(response, JSON_OBJECT);
    }
}
