package io.lumify.account;

import io.lumify.account.routes.AccountCreated;
import io.lumify.account.routes.CreateAccount;
import io.lumify.account.routes.CreateAccountForm;
import io.lumify.account.routes.CreateToken;
import io.lumify.account.util.SimpleTemplateFileHandler;
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

        app = new WebApp(config, injector);
        app.get("/index.html", new SimpleTemplateFileHandler());
        app.post("/create-token", CreateToken.class);
        app.get("/confirm-email.html", new SimpleTemplateFileHandler());
        app.get("/reset-password.html", new SimpleTemplateFileHandler());

        app.get("/create-account", CreateAccountForm.class);
        app.post("/create-account", CreateAccount.class);
        app.get("/account-created", AccountCreated.class);
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
