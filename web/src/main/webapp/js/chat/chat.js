
define([
    'flight/lib/component',
    'service/chat',
    'tpl!./chatWindow',
    'tpl!./chatMessage'
], function(defineComponent, ChatService, chatWindowTemplate, chatMessageTemplate) {
    'use strict';

    return defineComponent(Chat);

    function Chat() {
        this.chatService = new ChatService();
        this.openChats = {};
        this.currentUser = null;

        this.defaultAttrs({
            newMessageFormSelector: 'form.new-message',
            chatWindowSelector: '.chat-window'
        });

        this.after('initialize', function() {
            this.on(document, 'onlineStatusChanged', this.onOnlineStatusChanged);
            this.on(document, 'userSelected', this.onUserSelected);
            this.on(document, 'message', this.onMessage);
            this.on('createChatWindow', this.onCreateChatWindow);
            this.on('submit', {
                newMessageFormSelector: this.onNewMessageFormSubmit
            });
        });

        this.onOnlineStatusChanged = function(evt, data) {
            this.currentUser = data.user;
        };

        this.onUserSelected = function(evt, data) {
            var chat = this.findChatByToUserId(data.userId);
            if(chat) {
                this.select('chatWindowSelector').hide();
                return $('#chat-window-' + chat.id).show().find('.message').focus();
            }

            data.activate = true;
            this.trigger('createChatWindow', data);
        };

        this.onCreateChatWindow = function(evt, user) {
            var self = this;
            var userId = user.userId;
            this.chatService.createChat(userId, function(err, chat) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                return self.createOrFocusChat(chat, { activate: user.activate });
            });
        };

        this.createOrFocusChat = function(chat, options) {
            if(!this.openChats[chat.id]) {
                this.openChats[chat.id] = chat;
                var dom = $(chatWindowTemplate({ chat: chat }));
                dom.hide().appendTo(this.$node);
            }

            if (options && options.activate) {
                this.select('chatWindowSelector').hide();
                $('#chat-window-' + chat.id).show().find('.message').focus();
            }
        };

        this.addMessage = function(chatId, message) {
            var $chatWindow = $('#chat-window-' + chatId);
            var $chatMessages = $('.chat-messages', $chatWindow);
            $chatMessages.append(chatMessageTemplate({
                message: message
            }));
        };

        this.onMessage = function(evt, message) {
            if(message.type == 'chat') {
                this.createOrFocusChat(message.chat);
            } else if(message.type == 'chatMessage') {
                this.addMessage(message.chatId, message.message);
            }
        };

        this.onNewMessageFormSubmit = function(evt) {
            evt.preventDefault();

            var self = this;
            var $target = $(evt.target);
            var $chatWindow = $target.parents('.chat-window');
            var $messageInput = $('.message', $target);

            var message = $messageInput.val();
            var chatId = $chatWindow.attr('chat-id');
            var chat = this.openChats[chatId];

            var tempId = 'chat-message-temp-' + Date.now();

            // add a temporary message to create the feel of responsiveness
            this.addMessage(chatId, {
                tempId: tempId,
                from: { username: 'me' },
                message: message,
                postDate: null
            });

            this.chatService.sendChatMessage(chatId, message, function(err, message) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                $('#' + tempId).remove();
                self.addMessage(chatId, message);
            });

            $messageInput.val('');
            $messageInput.focus();
        };

        this.findChatByToUserId = function(userId) {
            var self = this;
            var filteredChats = Object.keys(this.openChats).filter(function(chatId) {
                var chat = self.openChats[chatId];
                var matchingUsers = chat.users.filter(function(u) { return u.id == userId; });
                return matchingUsers.length > 0;
            });
            return filteredChats.length == 0 ? null : this.openChats[filteredChats[0]];
        };
    }
});
