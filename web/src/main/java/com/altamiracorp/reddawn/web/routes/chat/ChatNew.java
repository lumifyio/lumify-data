package com.altamiracorp.reddawn.web.routes.chat;

import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ChatNew implements Handler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
//        MessageBus messageBus = MessageBus.getMessageBus(request);
//        String toUserId = request.getParameter("userId");
//        User currentUser = User.getUser(request);
//
//        Chat chat = new Chat(currentUser.getId(), toUserId);
//        messageBus.addChat(chat);
//        messageBus.postMessage(toUserId, new ChatCreateMessage(chat));

        new Responder(response).respondWith("");//chat.toJson(messageBus));
    }

//    public class ChatCreateMessage extends Message {
//        private final Chat chat;
//
//        public ChatCreateMessage(Chat chat) {
//            this.chat = chat;
//        }
//
//        @Override
//        public JSONObject toJson(MessageBus messageBus) throws JSONException {
//            JSONObject result = new JSONObject();
//            result.put("type", "chat");
//            result.put("chat", this.chat.toJson(messageBus));
//            return result;
//        }
//    }
}
