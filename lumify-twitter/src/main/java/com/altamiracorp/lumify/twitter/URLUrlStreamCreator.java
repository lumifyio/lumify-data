package com.altamiracorp.lumify.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This implementation of UrlStreamCreator uses the default java.net.URL
 * class to open the InputStream.
 */
public class URLUrlStreamCreator implements UrlStreamCreator {
    @Override
    public InputStream openUrlStream(final String url) throws MalformedURLException, IOException {
        return new URL(url).openStream();
    }
}
