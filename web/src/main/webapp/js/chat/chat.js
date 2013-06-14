
define([
    'flight/lib/component',
    'service/chat',
	'sync/sync',
    'tpl!./chatWindow',
    'tpl!./chatMessage'
], function(defineComponent, ChatService, Sync, chatWindowTemplate, chatMessageTemplate) {
    'use strict';

    return defineComponent(Chat);

    function Chat() {
        this.chatService = new ChatService();
        this.openChats = {};
        this.currentUser = null;

        this.defaultAttrs({
            newMessageFormSelector: 'form.new-message',
            chatWindowSelector: '.chat-window',
			syncRequestSelector: '.sync-request'
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
            if(this.openChats[data.id]) {
                this.select('chatWindowSelector').hide();
                return $('#chat-window-' + data.id).show().find('.message').focus();
            }

            data.activate = true;
            this.trigger('createChatWindow', data);
        };

        this.onCreateChatWindow = function(evt, user) {
			this.createOrFocusChat(user,{activate: user.activate});
        };

        this.createOrFocusChat = function(user, options) {
            if(!this.openChats[user.id]) {
                this.openChats[user.id] = user;
                var dom = $(chatWindowTemplate({ user: user , users: [this.currentUser, user]}));
                dom.hide().appendTo(this.$node);
				Sync.attachTo(this.select('syncRequestSelector'),{ chatUser : user.id, me : this.currentUser.id});
            }

            if (options && options.activate) {
                this.select('chatWindowSelector').hide();
                $('#chat-window-' + user.id).show().find('.message').focus();
                this.trigger(document, 'userSelected', user);
            }
        };

        this.addMessage = function(userId, message) {
			this.checkChatWindow(userId);
            var $chatWindow = $('#chat-window-' + userId);
            $chatWindow.find('.chat-messages').append(chatMessageTemplate({
                message: message
            }));

            var bottom = $chatWindow[0].scrollHeight - $chatWindow.height();
            $chatWindow.animate({scrollTop:bottom}, 'fast');
        };

		this.checkChatWindow = function (userId) {
		    var $chatWindow = $('#chat-window-' + userId);
            if ($chatWindow.length === 0) {
                this.trigger('createChatWindow', { id:userId, activate:true });
                $chatWindow = $('#chat-window-' + userId);
            }	
		}

        this.onMessage = function(evt, message) {
			switch (message.type) {
				case 'chatMessage':
					this.addMessage(message.from.id, message);
					break;
				case 'syncRequest':
					this.checkChatWindow(message.initiatorId);
					this.trigger('incomingSyncRequest',message);
					break;
				case 'syncRequestAcceptance':
					this.checkChatWindow(message.userIds[0]);
					this.trigger('incomingSyncAccept',message);
					break;
				case 'syncRequestRejection':
					this.checkChatWindow(message.userIds[0]);
					this.trigger('incomingSyncReject',message);
					break;
			}
        };

        this.onNewMessageFormSubmit = function(evt) {
            evt.preventDefault();

            var self = this;
            var $target = $(evt.target);
            var $chatWindow = $target.parents('.chat-window');
            var $messageInput = $('.message', $target);

            var message = $messageInput.val();
            var userId = $chatWindow.attr('chat-id');
            var chat = this.openChats[userId];

            var tempId = 'chat-message-temp-' + Date.now();

            // add a temporary message to create the feel of responsiveness
            this.addMessage(userId, {
                tempId: tempId,
                from: { id: 'me' },
                message: message,
                postDate: null
            });

            this.chatService.sendChatMessage(userId, self.currentUser.id, message, function(err, message) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                $('#' + tempId).remove();
                self.addMessage(userId, message);
            });

            $messageInput.val('');
            $messageInput.focus();
        };

    }
});
