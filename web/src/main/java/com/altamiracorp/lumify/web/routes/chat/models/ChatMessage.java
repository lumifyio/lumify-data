package com.altamiracorp.lumify.web.routes.chat.models;

import java.util.Date;

public class ChatMessage {
    private final String fromUserId;
    private final String message;
    private final Date postDate;

    public ChatMessage(String fromUserId, String message) {
        this.postDate = new Date();
        this.fromUserId = fromUserId;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Date getPostDate() {
        return postDate;
    }

    public String getFromUserId() {
        return fromUserId;
    }

//    public JSONObject toJson(MessageBus messageBus) throws JSONException {
//        JSONObject result = new JSONObject();
//        result.put("from", messageBus.getUser(getFromUserId()).toJson());
//        result.put("postDate", getPostDate());
//        result.put("message", getMessage());
//        return result;
//    }
//
//    public Message createMessageBusMessage(Chat chat) {
//        return new ChatMessageBusMessage(chat, this);
//    }

//    private class ChatMessageBusMessage extends Message {
//        private final ChatMessage chatMessage;
//        private final Chat chat;
//
//        public ChatMessageBusMessage(Chat chat, ChatMessage chatMessage) {
//            this.chat = chat;
//            this.chatMessage = chatMessage;
//        }
//
//        @Override
//        public JSONObject toJson(MessageBus messageBus) throws JSONException {
//            JSONObject json = new JSONObject();
//            json.put("type", "chatMessage");
//            json.put("chatId", this.chat.getId());
//            json.put("message", this.chatMessage.toJson(messageBus));
//            return json;
//        }
//    }
}
