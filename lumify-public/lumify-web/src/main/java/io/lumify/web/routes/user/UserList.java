package io.lumify.web.routes.user;

import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;
import io.lumify.core.config.Configuration;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.model.workspace.Workspace;
import io.lumify.core.model.workspace.WorkspaceRepository;
import io.lumify.core.model.workspace.WorkspaceUser;
import io.lumify.core.user.User;
import io.lumify.web.BaseRequestHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.securegraph.util.ConvertingIterable;
import org.securegraph.util.FilterIterable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserList extends BaseRequestHandler {
    @Inject
    public UserList(
            final UserRepository userRepository,
            final WorkspaceRepository workspaceRepository,
            final Configuration configuration) {
        super(userRepository, workspaceRepository, configuration);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String query = getOptionalParameter(request, "q");
        String workspaceId = getOptionalParameter(request, "workspaceId");

        User user = getUser(request);
        Iterable<User> users = getUserRepository().find(query);

        if (workspaceId != null) {
            users = getUsersWithWorkspaceAccess(workspaceId, users, user);
        }

        Iterable<String> workspaceIds = getCurrentWorkspaceIds(users);
        Map<String, String> workspaceNames = getWorkspaceNames(workspaceIds, user);

        JSONObject resultJson = new JSONObject();
        JSONArray usersJson = UserRepository.toJson(users, workspaceNames);
        resultJson.put("users", usersJson);

        respondWithJson(response, resultJson);
    }

    private Map<String, String> getWorkspaceNames(Iterable<String> workspaceIds, User user) {
        Map<String, String> result = new HashMap<String, String>();
        for (Workspace workspace : getWorkspaceRepository().findByIds(workspaceIds, user)) {
            if (workspace != null) {
                result.put(workspace.getId(), workspace.getDisplayTitle());
            }
        }
        return result;
    }

    private Iterable<String> getCurrentWorkspaceIds(Iterable<User> users) {
        return new ConvertingIterable<User, String>(users) {
            @Override
            protected String convert(User user) {
                return user.getCurrentWorkspaceId();
            }
        };
    }

    private Iterable<User> getUsersWithWorkspaceAccess(String workspaceId, final Iterable<User> users, User user) {
        final List<WorkspaceUser> usersWithAccess = getWorkspaceRepository().findUsersWithAccess(workspaceId, user);
        return new FilterIterable<User>(users) {
            @Override
            protected boolean isIncluded(User u) {
                return contains(usersWithAccess, u);
            }

            private boolean contains(List<WorkspaceUser> usersWithAccess, User u) {
                for (WorkspaceUser userWithAccess : usersWithAccess) {
                    if (userWithAccess.getUserId().equals(u.getUserId())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
