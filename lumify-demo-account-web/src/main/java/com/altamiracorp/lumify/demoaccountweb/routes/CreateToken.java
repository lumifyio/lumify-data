package com.altamiracorp.lumify.demoaccountweb.routes;

import static com.altamiracorp.lumify.demoaccountweb.ApplicationConfiguration.*;

import com.altamiracorp.lumify.demoaccountweb.ApplicationConfiguration;
import com.altamiracorp.lumify.demoaccountweb.DemoAccountUserRepository;
import com.altamiracorp.lumify.demoaccountweb.model.DemoAccountUser;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;
import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.resolver.DataSourceFileResolver;
import org.apache.commons.mail.resolver.DataSourceUrlResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class CreateToken extends BaseRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateToken.class.getName());
    private DemoAccountUserRepository demoAccountUserRepository;
    private ApplicationConfiguration configuration;

    @Inject
    public void setConfiguration(ApplicationConfiguration configuration) {
        this.configuration = configuration;
    }

    @Inject
    public void setDemoAccountUserRepository(DemoAccountUserRepository demoAccountUserRepository) {
        this.demoAccountUserRepository = demoAccountUserRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String email = getRequiredParameter(request, "email");
        boolean shouldRegister = getParameterBoolean(request, "register");

        final DemoAccountUser user = demoAccountUserRepository.getOrCreateUser(email, shouldRegister);
        demoAccountUserRepository.generateToken(user);
        demoAccountUserRepository.save(user);

        final String baseUrl = getBaseUrl(request);
        AsyncContext asyncContext = request.startAsync(request, response);
        asyncContext.start(new Runnable() {
            @Override
            public void run() {
                try {
                    sendEmail(user, baseUrl);
                } catch (Exception e) {
                    LOGGER.error("Unable to send email", e);
                }
            }
        });

        response.sendRedirect("token-created.html");
    }

    private void sendEmail(DemoAccountUser demoAccountUser, String baseUrl) throws IOException, EmailException {
        URL html = CreateToken.class.getResource("email-template.html");
        URL text = CreateToken.class.getResource("email-template.txt");
        File htmlFile = new File(html.getFile());
        File textFile = new File(text.getFile());

        ImageHtmlEmail email = new ImageHtmlEmail();

        email.setHostName(configuration.get(EMAIL_SMTP_HOST));
        email.setAuthentication(configuration.get(EMAIL_SMTP_USER), configuration.get(EMAIL_SMTP_PASS));

        String ssl = configuration.get(EMAIL_SMTP_SSL);
        email.setSSLOnConnect(ssl != null && ssl.toLowerCase().equals("true"));
        email.setDataSourceResolver(new DataSourceFileResolver(new File("./src/main/webapp/img")));
        email.addTo(demoAccountUser.getMetadata().getEmail());

        String from = configuration.get(EMAIL_FROM);
        String fromFull = configuration.get(EMAIL_FROM_FULL);
        email.setFrom(from, fromFull == null ? from : fromFull);
        email.setSubject(configuration.get(EMAIL_SUBJECT));

        Map<String, String> replacementTokens = new HashMap();
        replacementTokens.put("create-account-link", baseUrl + "/create-account?token=" + demoAccountUser.getMetadata().getToken());

        email.setHtmlMsg(replaceTokens(FileUtils.readFileToString(htmlFile), replacementTokens));
        email.setTextMsg(replaceTokens(FileUtils.readFileToString(textFile), replacementTokens));

        email.send();
        LOGGER.info("Sent token email to " + demoAccountUser.getMetadata().getEmail());
    }


}
