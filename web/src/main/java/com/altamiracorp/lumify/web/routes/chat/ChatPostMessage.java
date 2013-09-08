package com.altamiracorp.lumify.web.routes.chat;

import com.altamiracorp.lumify.web.Responder;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ChatPostMessage implements Handler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
//        User currentUser = DevBasicAuthenticator.getUser(request);
//        String chatId = (String) request.getAttribute("chatId");
//        String messageString = request.getParameter("message");
//
//        Chat chat = messageBus.getChat(chatId);
//
//        if (chat == null) {
//            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//            return;
//        }
//
//        ChatMessage message = null;
//        for (String userId : chat.getUserIds()) {
//            if (userId.equals(currentUser.getId())) {
//                continue;
//            }
//            message = chat.addMessage(currentUser.getId(), messageString);
//            messageBus.postMessage(userId, message.createMessageBusMessage(chat));
//        }
//        if (message == null) {
//            throw new Exception("No users to send to");
//        }

        new Responder(response).respondWith(""); //message.toJson(messageBus));
    }
}
