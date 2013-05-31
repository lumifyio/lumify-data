package com.altamiracorp.reddawn.web.routes.chat.models;

import com.altamiracorp.reddawn.web.messageBus.MessageBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class Chat {
    private final String id;
    private final ArrayList<String> userIds = new ArrayList<String>();
    private ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();

    public Chat(String userId1, String userId2) {
        this.id = Long.toString(new Date().getTime());
        userIds.add(userId1);
        userIds.add(userId2);
    }

    public String getId() {
        return id;
    }

    public Collection<String> getUserIds() {
        return userIds;
    }

    public JSONObject toJson(MessageBus messageBus) throws JSONException {
        JSONArray usersJson = new JSONArray();
        for (String userId : userIds) {
            usersJson.put(messageBus.getUser(userId).toJson());
        }

        JSONObject results = new JSONObject();
        results.put("id", this.id);
        results.put("users", usersJson);
        return results;
    }

    public ChatMessage addMessage(String fromUserId, String messageString) {
        ChatMessage message = new ChatMessage(fromUserId, messageString);
        messages.add(message);
        return message;
    }
}
