define([
    'flight/lib/component',
    'service/chat',
    'tpl!./chatWindow',
    'tpl!./chatMessage'
], function (defineComponent, ChatService, chatWindowTemplate, chatMessageTemplate) {
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

        this.after('initialize', function () {
            this.on(document, 'onlineStatusChanged', this.onOnlineStatusChanged);
            this.on(document, 'userSelected', this.onUserSelected);
            this.on(document, 'chatMessage', this.onChatMessage);
            this.on(document, 'socketMessage', this.onSocketMessage);
            this.on('createChatWindow', this.onCreateChatWindow);
            this.on('submit', {
                newMessageFormSelector: this.onNewMessageFormSubmit
            });
        });

        this.onOnlineStatusChanged = function (evt, data) {
            this.currentUser = data.user;
        };

        this.onUserSelected = function (evt, data) {
            if (this.openChats[data.rowKey]) {
                this.select('chatWindowSelector').hide();
                return $('#chat-window-' + data.rowKey).show().find('.message').focus();
            }

            data.activate = true;
            this.trigger('createChatWindow', data);
        };

        this.onCreateChatWindow = function (evt, user) {
            this.createOrFocusChat(user, {activate: user.activate});
        };

        this.createOrFocusChat = function (user, options) {
            if (!this.openChats[user.rowKey]) {
                this.openChats[user.rowKey] = user;
                var dom = $(chatWindowTemplate({ user: user, users: [this.currentUser, user]}));
                dom.hide().appendTo(this.$node);
            }

            if (options && options.activate) {
                this.select('chatWindowSelector').hide();
                $('#chat-window-' + user.rowKey).show().find('.message').focus();
                this.trigger(document, 'userSelected', user);
            }
        };

        this.addMessage = function (userId, message) {
            this.checkChatWindow(userId);
            var $chatWindow = $('#chat-window-' + userId);
            var data = {
                message: message
            };
            $chatWindow.find('.chat-messages').append(chatMessageTemplate(data));

            this.scrollWindowToBottom($chatWindow);
        };

        this.checkChatWindow = function (userId) {
            var $chatWindow = $('#chat-window-' + userId);
            if ($chatWindow.length === 0) {
                this.trigger('createChatWindow', { rowKey: userId, activate: true });
                $chatWindow = $('#chat-window-' + userId);
            }

            this.scrollWindowToBottom($chatWindow);
        };

        this.scrollWindowToBottom = function (chatWindow) {
            console.log('scrollWindowToBottom', chatWindow);
            clearTimeout(this.scrollTimeout);
            this.scrollTimeout = setTimeout(function () {
                var bottom = chatWindow[0].scrollHeight - chatWindow.height();
                chatWindow.clearQueue().animate({scrollTop: bottom}, 'fast');
            }, 100);
        };

        this.onSocketMessage = function (evt, data) {
            var self = this;

            switch (data.type) {
                case 'chatMessage':
                    self.trigger(document, 'chatMessage', data.data);
                    break;
            }
        };

        this.onChatMessage = function (evt, message) {
            this.addMessage(message.from, message);
        };

        this.onNewMessageFormSubmit = function (evt) {
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
                to: chatId,
                from: self.currentUser.rowKey,
                message: message,
                postDate: null,
                tempId: tempId
            });

            this.chatService.sendChatMessage(chatId, self.currentUser.rowKey, message, function (err, message) {
                if (err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                $('#' + tempId).remove();
                return self.addMessage(chatId, message);
            });

            $messageInput.val('');
            $messageInput.focus();
        };

    }
});
