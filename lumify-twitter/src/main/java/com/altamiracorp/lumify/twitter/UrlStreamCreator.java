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
