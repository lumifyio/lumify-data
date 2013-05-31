package com.altamiracorp.reddawn.web.routes.chat;

import com.altamiracorp.reddawn.web.User;
import com.altamiracorp.reddawn.web.messageBus.MessageBus;
import com.altamiracorp.reddawn.web.routes.chat.models.Chat;
import com.altamiracorp.reddawn.web.routes.chat.models.ChatMessage;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ChatPostMessage implements Handler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        MessageBus messageBus = MessageBus.getMessageBus(request);

        User currentUser = User.getUser(request);
        String chatId = (String) request.getAttribute("chatId");
        String messageString = request.getParameter("message");

        Chat chat = messageBus.getChat(chatId);

        if (chat == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        ChatMessage message = null;
        for (String userId : chat.getUserIds()) {
            if (userId.equals(currentUser.getId())) {
                continue;
            }
            message = chat.addMessage(currentUser.getId(), messageString);
            messageBus.postMessage(userId, message.createMessageBusMessage(chat));
        }
        if (message == null) {
            throw new Exception("No users to send to");
        }

        response.setContentType("application/json");
        response.getWriter().write(message.toJson(messageBus).toString());
    }
}
