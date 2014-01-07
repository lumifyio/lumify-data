/*
 * Copyright 2014 Altamira Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.altamiracorp.lumify.twitter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ URLUrlStreamCreator.class })
public class URLUrlStreamCreatorTest {
    private static final String TEST_URL = "http://lumify.io";
    
    private URLUrlStreamCreator instance;
    
    @Before
    public void setUp() {
        instance = new URLUrlStreamCreator();
    }
    
    @Test(expected=MalformedURLException.class)
    public void testOpenUrlStream_MalformedURL() throws Exception {
        PowerMockito.whenNew(URL.class).withParameterTypes(String.class).withArguments(TEST_URL).thenThrow(MalformedURLException.class);
        instance.openUrlStream(TEST_URL);
    }
    
    @Test(expected=IOException.class)
    public void testOpenUrlStream_IOException() throws Exception {
        URL url = PowerMockito.mock(URL.class);
        PowerMockito.whenNew(URL.class).withParameterTypes(String.class).withArguments(TEST_URL).thenReturn(url);
        when(url.openStream()).thenThrow(IOException.class);
        instance.openUrlStream(TEST_URL);
    }
    
    @Test
    public void testOpenUrlStream() throws Exception {
        URL url = PowerMockito.mock(URL.class);
        InputStream expected = mock(InputStream.class);
        PowerMockito.whenNew(URL.class).withParameterTypes(String.class).withArguments(TEST_URL).thenReturn(url);
        when(url.openStream()).thenReturn(expected);
        
        InputStream actual = instance.openUrlStream(TEST_URL);
        assertEquals(expected, actual);
    }
}
