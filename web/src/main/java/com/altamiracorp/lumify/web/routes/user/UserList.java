package com.altamiracorp.lumify.web.routes.user;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.model.user.UserRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class UserList extends BaseRequestHandler {
    private final UserRepository userRepository;

    @Inject
    public UserList(final UserRepository repo) {
        this.userRepository = repo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User authUser = getUser(request);

        List<com.altamiracorp.lumify.core.model.user.User> users = userRepository.findAll(authUser);

        JSONObject resultJson = new JSONObject();
        JSONArray usersJson = getJson(users);
        resultJson.put("users", usersJson);

        respondWithJson(response, resultJson);
    }

    private JSONArray getJson(List<com.altamiracorp.lumify.core.model.user.User> users) throws JSONException {
        JSONArray usersJson = new JSONArray();
        for (com.altamiracorp.lumify.core.model.user.User user : users) {
            usersJson.put(user.toJson());
        }
        return usersJson;
    }
}
