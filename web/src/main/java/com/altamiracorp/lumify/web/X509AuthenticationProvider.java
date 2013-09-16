package com.altamiracorp.lumify.web;

import com.altamiracorp.web.HandlerChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class X509AuthenticationProvider extends AuthenticationProvider {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }
}
