define([
    'flight/lib/component',
    'service/user',
    'chat/chat',
    'tpl!./users',
    'tpl!./userListItem'
], function (defineComponent, UsersService, Chat, usersTemplate, userListItemTemplate) {
    'use strict';

    return defineComponent(Users);

    function Users() {
        this.usersService = new UsersService();
        this.currentUserRowKey = null;
        this.onlineUsers = [];

        this.defaultAttrs({
            usersListSelector: '.users-list',
            userListItemSelector: '.users-list .user',
            chatSelector: '.active-chat'
        });

        this.after('initialize', function () {
            this.$node.html(usersTemplate({}));

            this.$node.addClass('popover');

            Chat.attachTo(this.select('chatSelector'));

            this.on(document, 'newUserOnline', this.onNewUserOnline);
            this.on(document, 'userOnlineStatusChanged', this.onUserOnlineStatusChanged);
            this.on(document, 'chatMessage', this.onChatMessage);
            this.on(document, 'socketMessage', this.onSocketMessage);
            this.on(document, 'chatCreated', this.onChatCreated);
            this.on('click', {
                userListItemSelector: this.onUserListItemClicked
            });

            this.doGetOnline();
        });

        this.onUserListItemClicked = function (evt) {
            evt.preventDefault();

            var $target = $(evt.target).parents('li');
            var userData = $target.data('userdata');

            $target.find('.badge').text('');

            if ($target.hasClass('offline')) {
                return;
            }

            this.trigger(document, 'userSelected', userData);
        };

        this.onNewUserOnline = function (evt, userData) {
            console.log('onNewUserOnline', userData);
            var $usersList = this.select('usersListSelector');
            var html = userListItemTemplate({ user: userData });
            $usersList.find('li.status-' + userData.status).after(html);
        };

        this.onChatCreated = function (evt, chat) {
            this.createOrActivateConversation(chat);
        };

        this.createOrActivateConversation = function (chat) {
            console.log('createOrActivateConversation', chat);
            var $usersList = this.select('usersListSelector');
            var activeChat = $usersList.find('li.conversation-' + chat.rowKey);

            if (!activeChat.length && chat.users) {
                activeChat = $usersList.find('li.online.user-' + chat.users[0].rowKey);
                if (!activeChat.length) {
                    return;
                }
                activeChat = activeChat.clone();
                activeChat.removeClass('user-' + chat.users[0].rowKey);
                activeChat.addClass('conversation-' + chat.rowKey);
                $usersList.find('li.conversations').after(activeChat);
            }

            $usersList.find('.active').removeClass('active');
            activeChat.addClass('active');
        };

        this.onChatMessage = function (evt, message) {
            console.log('onChatMessage', message);
            var chat = {
                rowKey: message.chatRowKey,
                users: [message.from]
            };
            this.createOrActivateConversation(chat);

            var badge = this.select('usersListSelector').find('li.conversation-' + message.chatRowKey + ':not(.active) .badge');
            badge.text(+badge.text() + 1);
        };


        this.onUserOnlineStatusChanged = function (evt, userData) {
            console.log('onUserOnlineStatusChanged', userData);
            var $usersList = this.select('usersListSelector');
            var $user = $('.user-' + userData.rowKey, $usersList);
            if ($user.length) {
                $user.remove();
                var html = userListItemTemplate({ user: userData });
                $usersList.find('li.status-' + userData.status).after(html);
            } else {
                this.onNewUserOnline(evt, userData);
            }
        };

        this.onSocketMessage = function (evt, data) {
            var self = this;

            switch (data.type) {
                case 'userStatusChange':
                    self.updateUser(data);
                    break;
            }
        };

        this.updateUsers = function (users) {
            var self = this;
            users.forEach(self.updateUser.bind(this));
            self.onlineUsers = users;
        };

        this.updateUser = function (user) {
            var self = this;
            if (user.rowKey == self.currentUserRowKey) {
                return;
            }
            var currentOnlineUsers = self.onlineUsers.filter(function (u) {
                return u.rowKey == user.rowKey;
            });
            var currentOnlineUser = currentOnlineUsers.length ? currentOnlineUsers[0] : null;
            if (!currentOnlineUser) {
                self.trigger(document, 'newUserOnline', user);
            } else if (currentOnlineUser.status != user.status) {
                self.trigger(document, 'userOnlineStatusChanged', user);
            }
        };

        this.doGetOnline = function () {
            var self = this;
            self.usersService.getOnline(function (err, data) {
                if (err) {
                    var $usersList = self.select('usersListSelector');
                    $usersList.html('Could not get online: ' + err);
                    return;
                }

                if (data.messages && data.messages.length > 0) {
                    data.messages.forEach(function (message) {
                        self.trigger(document, 'chatMessage', message);
                    });
                }

                if (self.currentUserRowKey != data.user.rowKey) {
                    self.currentUserRowKey = data.user.rowKey;
                    self.trigger(document, 'onlineStatusChanged', data);
                }

                self.updateUsers(data.users);

                self.usersService.subscribe(self.currentUserRowKey, function (err, message) {
                    if (err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }
                    self.trigger(document, 'socketMessage', message);
                });
            });
        };
    }
});
