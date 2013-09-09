package com.altamiracorp.lumify.web.routes.user;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.user.User;
import com.altamiracorp.lumify.model.user.UserRepository;
import com.altamiracorp.lumify.web.Responder;
import com.altamiracorp.lumify.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class UserList implements Handler, AppAware {
    private UserRepository userRepository = new UserRepository();
    private WebApp app;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);

        List<User> users = userRepository.findAll(session.getModelSession());

        JSONObject resultJson = new JSONObject();
        JSONArray usersJson = getJson(users);
        resultJson.put("users", usersJson);

        new Responder(response).respondWith(resultJson);
    }

    private JSONArray getJson(List<User> users) throws JSONException {
        JSONArray usersJson = new JSONArray();
        for (User user : users) {
            usersJson.put(user.toJson());
        }
        return usersJson;
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
