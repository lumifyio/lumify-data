package io.lumify.account.routes;

import io.lumify.account.AccountUserRepository;
import io.lumify.account.model.AccountUser;
import io.lumify.core.model.user.UserRepository;
import com.altamiracorp.miniweb.HandlerChain;
import com.altamiracorp.miniweb.utils.UrlUtils;
import org.securegraph.Graph;
import com.google.inject.Inject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

public class CreateAccount extends BaseRequestHandler {
    private UserRepository userRepository;
    private AccountUserRepository accountUserRepository;
    private Graph graph;

    @Inject
    public void setAccountUserRepository(AccountUserRepository accountUserRepository) {
        this.accountUserRepository = accountUserRepository;
    }

    @Inject
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Inject
    public void setGraph (Graph graph) { this.graph = graph; }

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

        userRepository.addUser(graph.getIdGenerator().nextId().toString(), user.getData().getEmail(), password, new String[0]);

        // expire the token
        user.getData().setTokenExpiration(new Date());
        user.getData().setReset(false);
        accountUserRepository.save(user);

        response.sendRedirect("account-created" + (reset ? "?reset=1" : ""));
    }
}
