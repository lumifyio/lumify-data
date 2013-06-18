package com.altamiracorp.reddawn.web.routes.user;

import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.User;
import com.altamiracorp.reddawn.web.messageBus.Message;
import com.altamiracorp.reddawn.web.messageBus.MessageBus;
import com.altamiracorp.reddawn.web.messageBus.MessageBusUser;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;

public class MessagesGet implements Handler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        MessageBus messageBus = MessageBus.getMessageBus(request);
        User currentUser = User.getUser(request);
        if (currentUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
            return;
        }
        MessageBusUser currentMessageBusUser = messageBus.findAddUser(currentUser);
        currentMessageBusUser.updateLastSeenDate();

        ArrayList<MessageBusUser> users = getUsers(messageBus.getUsers(), currentUser);
        JSONObject resultsJson = new JSONObject();
        resultsJson.put("user", currentMessageBusUser.toJson());
        resultsJson.put("users", userDatasToJson(users));
        resultsJson.put("messages", Message.toJson(messageBus, currentMessageBusUser.getMessagesAndClear()));

        new Responder(response).respondWith(resultsJson);
    }

    private ArrayList<MessageBusUser> getUsers(Collection<MessageBusUser> allUsers, User currentUser) {
        ArrayList<MessageBusUser> filteredUsers = new ArrayList<MessageBusUser>();
        for (MessageBusUser u : allUsers) {
            if (u.getUser().getId().equals(currentUser.getId())) {
                continue;
            }
            filteredUsers.add(u);
        }
        return filteredUsers;
    }

    private JSONArray userDatasToJson(Collection<MessageBusUser> messageBusUsers) throws JSONException {
        JSONArray usersJson = new JSONArray();
        for (MessageBusUser messageBusUser : messageBusUsers) {
            usersJson.put(messageBusUser.toJson());
        }
        return usersJson;
    }
}
