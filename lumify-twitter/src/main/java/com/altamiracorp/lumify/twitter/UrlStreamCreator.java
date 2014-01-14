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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * URL stream creators attempt to open an input stream from the provided URL.
 */
public interface UrlStreamCreator {
    /**
     * Open an InputStream to the content found at the provided URL.
     * @param url the URL
     * @return an InputStream for reading the content of the provided URL
     * @throws MalformedURLException if an invalid URL is provided
     * @throws IOException if an error occurs opening the InputStream
     */
    InputStream openUrlStream(final String url) throws MalformedURLException, IOException;
}
