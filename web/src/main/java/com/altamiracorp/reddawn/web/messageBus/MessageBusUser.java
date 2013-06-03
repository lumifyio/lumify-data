package com.altamiracorp.reddawn.web.messageBus;

import com.altamiracorp.reddawn.web.User;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class MessageBusUser {
    private static final long USER_TIMEOUT = 10000;
    private User user;
    private Date lastSeen;
    private ArrayList<Message> messages = new ArrayList<Message>();

    public MessageBusUser(User user) {
        this.user = user;
    }

    public void updateLastSeenDate() {
        lastSeen = new Date();
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    private Collection<Message> getMessages() {
        return messages;
    }

    public Collection<Message> getMessagesAndClear() {
        Collection<Message> messages = new ArrayList<Message>(getMessages());
        this.messages.clear();
        return messages;
    }

    public User getUser() {
        return user;
    }

    public UserStatus getStatus() {
        if (new Date().getTime() - getLastSeen().getTime() > USER_TIMEOUT) {
            return UserStatus.Offline;
        } else {
            return UserStatus.Online;
        }
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject userJson = new JSONObject();
        userJson.put("id", getUser().getId());
        userJson.put("status", getStatus().toString().toLowerCase());
        userJson.put("displayName", getUser().getUsername()); // TODO change out to a better display name
        userJson.put("username", getUser().getUsername());
        userJson.put("lastSeen", getLastSeen());
        return userJson;
    }
}
