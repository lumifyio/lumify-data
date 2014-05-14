package io.lumify.web.auth;

import com.altamiracorp.miniweb.Handler;
import com.altamiracorp.miniweb.StaticResourceHandler;
import com.google.inject.Inject;
import io.lumify.core.config.Configuration;
import io.lumify.core.model.user.UserRepository;
import io.lumify.ldap.LdapSearchService;
import io.lumify.web.AuthenticationHandler;
import io.lumify.web.WebApp;
import io.lumify.web.WebAppPlugin;
import org.securegraph.Graph;

import javax.servlet.ServletConfig;

public class LdapX509WebAppPlugin implements WebAppPlugin {
    private UserRepository userRepository;
    private Configuration configuration;
    private LdapSearchService ldapSearchService;
    private Graph graph;

    @Inject
    public void configure(UserRepository userRepository, Graph graph, LdapSearchService ldapSearchService, Configuration configuration) {
        this.userRepository = userRepository;
        this.graph = graph;
        this.ldapSearchService = ldapSearchService;
        this.configuration = configuration;
    }

    @Override
    public void init(WebApp app, ServletConfig config, Handler authenticationHandler) {
        StaticResourceHandler jsHandler = new StaticResourceHandler(this.getClass(), "/ldap-x509/authentication.js", "application/javascript");
        StaticResourceHandler loginTemplateHandler = new StaticResourceHandler(this.getClass(), "/ldap-x509/templates/login.hbs", "text/plain");
        StaticResourceHandler lessHandler = new StaticResourceHandler(this.getClass(), "/ldap-x509/less/login.less", "text/plain");

        app.get("/jsc/configuration/plugins/authentication/authentication.js", jsHandler);
        app.get("/jsc/configuration/plugins/authentication/templates/login.hbs", loginTemplateHandler);
        app.get("/jsc/configuration/plugins/authentication/less/login.less", lessHandler);

        app.post(AuthenticationHandler.LOGIN_PATH, new LdapX509AuthenticationHandler(userRepository, graph, ldapSearchService, configuration));
    }
}
