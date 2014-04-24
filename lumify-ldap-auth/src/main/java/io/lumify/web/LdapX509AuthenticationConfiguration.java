package io.lumify.web;

import io.lumify.core.config.Configurable;

public class LdapX509AuthenticationConfiguration {
    private String clientDnHeader;
    private String clientCertHeader;
    private String usernameAttribute;
    private String displayNameAttribute;

    @Configurable(name = "clientDnHeader", defaultValue = "SSL_CLIENT_S_DN")
    public void setClientDnHeader(String clientDnHeader) {
        this.clientDnHeader = clientDnHeader;
    }

    @Configurable(name = "clientCertHeader", defaultValue = "SSL_CLIENT_CERT")
    public void setClientCertHeader(String clientCertHeader) {
        this.clientCertHeader = clientCertHeader;
    }

    @Configurable(name = "usernameAttribute", required = false)
    public void setUsernameAttribute(String usernameAttribute) {
        this.usernameAttribute = usernameAttribute;
    }

    @Configurable(name = "displayNameAttribute", required = false)
    public void setDisplayNameAttribute(String displayNameAttribute) {
        this.displayNameAttribute = displayNameAttribute;
    }

    public String getDisplayNameAttribute() {
        return displayNameAttribute;
    }

    public String getUsernameAttribute() {
        return usernameAttribute;
    }

    public String getClientCertHeader() {
        return clientCertHeader;
    }

    public String getClientDnHeader() {
        return clientDnHeader;
    }
}
