package com.altamiracorp.lumify.demoaccountweb.routes;

import com.altamiracorp.lumify.demoaccountweb.ApplicationConfiguration;
import com.altamiracorp.lumify.demoaccountweb.DemoAccountUserRepository;
import com.altamiracorp.lumify.demoaccountweb.model.DemoAccountUser;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.resolver.DataSourceClassPathResolver;
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

import static com.altamiracorp.lumify.demoaccountweb.ApplicationConfiguration.*;

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

        if (StringUtils.isEmpty(email)) {
            throw new IllegalArgumentException("Email must not be blank");
        }

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

        if (user.getMetadata().getReset()) {
            response.sendRedirect("reset-password.html");
        } else {
            response.sendRedirect("confirm-email.html");
        }
    }

    private void sendEmail(DemoAccountUser demoAccountUser, String baseUrl) throws IOException, EmailException {
        String resetPath = demoAccountUser.getMetadata().getReset() ? "-reset" : "";
        URL html = CreateToken.class.getResource("email-template" + resetPath + ".html");
        URL text = CreateToken.class.getResource("email-template" + resetPath + ".txt");
        File htmlFile = new File(html.getFile());
        File textFile = new File(text.getFile());

        ImageHtmlEmail email = new ImageHtmlEmail();

        email.setHostName(configuration.get(EMAIL_SMTP_HOST));
        email.setAuthentication(configuration.get(EMAIL_SMTP_USER), configuration.get(EMAIL_SMTP_PASS));

        String ssl = configuration.get(EMAIL_SMTP_SSL);
        email.setSSLOnConnect(ssl != null && ssl.toLowerCase().equals("true"));
        email.setDataSourceResolver(new DataSourceClassPathResolver("/" + CreateToken.class.getPackage().getName().replaceAll("\\.","/")));
        email.addTo(demoAccountUser.getMetadata().getEmail());

        String from = configuration.get(EMAIL_FROM);
        String fromFull = configuration.get(EMAIL_FROM_FULL);
        email.setFrom(from, fromFull == null ? from : fromFull);
        email.setSubject(configuration.get(EMAIL_SUBJECT));

        Map<String, String> replacementTokens = new HashMap();
        replacementTokens.put("create-account-link",
                baseUrl + "/create-account?" +
                "token=" + demoAccountUser.getMetadata().getToken() +
                (demoAccountUser.getMetadata().getReset() ? "&reset=1" : ""));

        email.setHtmlMsg(replaceTokens(FileUtils.readFileToString(htmlFile), replacementTokens));
        email.setTextMsg(replaceTokens(FileUtils.readFileToString(textFile), replacementTokens));

        email.send();
        LOGGER.info("Sent token email to " + demoAccountUser.getMetadata().getEmail());
    }


}
