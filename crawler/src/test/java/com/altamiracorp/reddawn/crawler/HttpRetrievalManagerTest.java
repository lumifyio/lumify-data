package com.altamiracorp.reddawn.crawler;

import com.altamiracorp.reddawn.crawler.HttpRetrievalManager;
import com.altamiracorp.reddawn.crawler.HttpRetriever;
import org.apache.commons.httpclient.HttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class HttpRetrievalManagerTest {

    @Test
    public void testAddJob() throws Exception {
        HttpRetrievalManager managerSpy = spy(new HttpRetrievalManager());
        when(managerSpy.createHttpRetriever((org.apache.http.client.HttpClient) any(HttpClient.class), anyString(),
                anyString(), anyString())).thenReturn(mock(HttpRetriever.class));
        managerSpy.addJob("", "", "");
        verify(managerSpy).createHttpRetriever((org.apache.http.client.HttpClient) any(HttpClient.class), anyString(),
                anyString(), anyString());
    }
}
