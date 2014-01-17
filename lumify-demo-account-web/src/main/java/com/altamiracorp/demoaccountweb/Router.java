package com.altamiracorp.demoaccountweb;

import com.altamiracorp.demoaccountweb.routes.CreateAccount;
import com.altamiracorp.demoaccountweb.routes.CreateAccountForm;
import com.altamiracorp.demoaccountweb.routes.CreateToken;
import com.altamiracorp.demoaccountweb.security.AuthenticationProvider;
import com.altamiracorp.demoaccountweb.util.SimpleTemplateFileHandler;
import com.altamiracorp.miniweb.Handler;
import com.google.inject.Injector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Router extends HttpServlet {
    private WebApp app;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        final Injector injector = (Injector) config.getServletContext().getAttribute(Injector.class.getName());

        AuthenticationProvider authenticatorInstance = injector.getInstance(AuthenticationProvider.class);
        Class<? extends Handler> authenticator = authenticatorInstance.getClass();

        app = new WebApp(config, injector);
        app.get("/index.html", new SimpleTemplateFileHandler());
        app.post("/create-token", CreateToken.class);
        app.get("/create-account", CreateAccountForm.class);
        app.post("/create-account", CreateAccount.class);
    }

    @Override
    public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
        try {
            HttpServletResponse httpResponse = (HttpServletResponse) resp;
            httpResponse.addHeader("Accept-Ranges", "bytes");
            app.handle((HttpServletRequest) req, httpResponse);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
