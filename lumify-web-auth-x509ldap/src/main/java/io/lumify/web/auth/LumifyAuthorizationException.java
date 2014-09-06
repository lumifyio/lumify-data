package io.lumify.web.auth;

import io.lumify.core.exception.LumifyException;

public class LumifyAuthorizationException extends LumifyException {
    public LumifyAuthorizationException(String message) {
        super(message);
    }

    public LumifyAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
