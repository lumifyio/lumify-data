package com.altamiracorp.reddawn.web.messageBus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

public abstract class Message {
    public static JSONArray toJson(MessageBus messageBus, Collection<Message> messages) throws JSONException {
        JSONArray results = new JSONArray();
        for (Message message : messages) {
            results.put(message.toJson(messageBus));
        }
        return results;
    }

    public abstract JSONObject toJson(MessageBus messageBus) throws JSONException;
}
