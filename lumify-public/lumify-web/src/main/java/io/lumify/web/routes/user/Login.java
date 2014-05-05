package io.lumify.web.routes.user;

import io.lumify.core.config.Configuration;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.model.workspace.WorkspaceRepository;
import io.lumify.web.AuthenticationProvider;
import io.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Login extends BaseRequestHandler {
    private final AuthenticationProvider authenticationProvider;

    @Inject
    public Login(
            final AuthenticationProvider authenticationProvider,
            final UserRepository userRepository,
            final WorkspaceRepository workspaceRepository,
            final Configuration configuration) {
        super(userRepository, workspaceRepository, configuration);
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        if (authenticationProvider.login(request)) {
            JSONObject json = new JSONObject();
            json.put("status", "OK");
            respondWithJson(response, json);
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
