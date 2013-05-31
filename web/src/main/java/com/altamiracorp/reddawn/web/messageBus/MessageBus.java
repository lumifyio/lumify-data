package com.altamiracorp.reddawn.web.messageBus;

import com.altamiracorp.reddawn.web.User;
import com.altamiracorp.reddawn.web.routes.chat.models.Chat;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashMap;

public class MessageBus {
    private static final String MESSAGE_BUS_ATTRIBUTE_NAME = "MessageBus";

    private HashMap<String, MessageBusUser> users = new HashMap<String, MessageBusUser>();
    private HashMap<String, Chat> chats = new HashMap<String, Chat>();

    public Collection<MessageBusUser> getUsers() {
        return users.values();
    }

    public MessageBusUser findAddUser(User user) {
        MessageBusUser messageBusUser = users.get(user.getId());
        if (messageBusUser == null) {
            messageBusUser = new MessageBusUser(user);
            users.put(user.getId(), messageBusUser);
        }
        return messageBusUser;
    }

    public static MessageBus getMessageBus(HttpServletRequest request) {
        MessageBus messageBus = (MessageBus) request.getSession().getServletContext().getAttribute(MESSAGE_BUS_ATTRIBUTE_NAME);
        if (messageBus == null) {
            messageBus = new MessageBus();
            request.getSession().getServletContext().setAttribute(MESSAGE_BUS_ATTRIBUTE_NAME, messageBus);
        }
        return messageBus;
    }

    public MessageBusUser getUser(String userId) {
        return users.get(userId);
    }

    public Chat getChat(String chatId) {
        return this.chats.get(chatId);
    }

    public void addChat(Chat chat) {
        this.chats.put(chat.getId(), chat);
    }

    public void postMessage(String toUserId, Message message) {
        MessageBusUser toUser = getUser(toUserId);
        if (toUser == null) {
            throw new RuntimeException("User with id " + toUserId + " not found.");
        }
        toUser.addMessage(message);
    }
}
