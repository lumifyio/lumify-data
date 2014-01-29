package com.altamiracorp.lumify.account.routes;

import com.altamiracorp.lumify.account.AccountUserRepository;
import com.altamiracorp.lumify.account.model.AccountUser;
import com.altamiracorp.lumify.core.model.user.UserRepository;
import com.altamiracorp.lumify.core.user.UserProvider;
import com.altamiracorp.miniweb.HandlerChain;
import com.altamiracorp.miniweb.utils.UrlUtils;
import com.google.inject.Inject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

public class CreateAccount extends BaseRequestHandler {
    private UserRepository userRepository;
    private AccountUserRepository accountUserRepository;
    private UserProvider userProvider;

    @Inject
    public void setAccountUserRepository(AccountUserRepository accountUserRepository) {
        this.accountUserRepository = accountUserRepository;
    }

    @Inject
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Inject
    public void setUserProvider(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String token = getRequiredParameter(request, "token");
        String password = getRequiredParameter(request, "password");
        String confirmPassword = getRequiredParameter(request, "confirm-password");
        boolean reset = getParameterBoolean(request, "reset");

        if (!password.equals(confirmPassword)) {
            String path = "create-account?err=" + UrlUtils.urlEncode("Passwords do not match") +
                    "&token=" + token +
                    (reset ? "&reset=1" : "");
            response.sendRedirect(path);
            return;
        }

        AccountUser user = accountUserRepository.getUserFromToken(token);
        if (user == null) {
            response.sendRedirect("index.html");
            return;
        }

        userRepository.addUser(user.getData().getEmail(), password, this.userProvider.getSystemUser());

        // expire the token
        user.getData().setTokenExpiration(new Date());
        user.getData().setReset(false);
        accountUserRepository.save(user);

        response.sendRedirect("account-created" + (reset ? "?reset=1" : ""));
    }
}
