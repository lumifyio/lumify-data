package io.lumify.web;

import com.altamiracorp.miniweb.HandlerChain;
import com.altamiracorp.miniweb.utils.UrlUtils;
import com.google.inject.Inject;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.user.Roles;
import io.lumify.core.user.User;
import org.securegraph.Graph;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UsernameOnlyAuthenticationProvider extends AuthenticationProvider {
    private static final String PASSWORD = "8XXuk2tQ523b";
    private final UserRepository userRepository;
    private final Graph graph;

    @Inject
    public UsernameOnlyAuthenticationProvider(final UserRepository userRepository,
                                              final Graph graph) {
        this.userRepository = userRepository;
        this.graph = graph;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User user = getUser(request);
        if (user != null) {
            chain.next(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Override
    public boolean login(HttpServletRequest request) {
        final String username = UrlUtils.urlDecode(request.getParameter("username"));

        User user = userRepository.findByUsername(username);
        if (user == null) {
            user = userRepository.addUser(graph.getIdGenerator().nextId().toString(), username, PASSWORD, Roles.NONE, new String[0]);
        }
        setUser(request, user);
        return true;
    }
}
