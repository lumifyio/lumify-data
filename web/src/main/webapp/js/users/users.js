
define([
    'flight/lib/component',
    'service/user',
    'chat/chat',
    'tpl!./users',
    'tpl!./userListItem'
], function(defineComponent, UsersService, Chat, usersTemplate, userListItemTemplate) {
    'use strict';

    return defineComponent(Users);

    function Users() {
        this.usersService = new UsersService();
        this.currentUserId = null;
        this.onlineUsers = [];

        this.defaultAttrs({
            usersListSelector: '.users-list',
            userListItemSelector: '.users-list .user',
            chatSelector: '.active-chat',
        });

        this.after('initialize', function() {
            this.$node.html(usersTemplate({}));

            Chat.attachTo(this.select('chatSelector'));

            this.on(document, 'newUserOnline', this.onNewUserOnline);
            this.on(document, 'userOnlineStatusChanged', this.onUserOnlineStatusChanged);
            this.on(document, 'userSelected', this.onUserSelected);
            this.on(document, 'message', this.onMessage);
            this.on('click', {
                userListItemSelector: this.onUserListItemClicked
            });

            this.doGetOnline();
            setInterval(this.doGetOnline.bind(this), 500); // TODO use long polling
        });

        this.onUserListItemClicked = function(evt) {
            var $target = $(evt.target).parents('li');
            var userId = $target.data('userid');
            
            if ($target.hasClass('offline')) {
                return;
            }

            this.trigger(document, 'userSelected', { userId: userId });
        };

        this.onNewUserOnline = function(evt, userData) {
            var $usersList = this.select('usersListSelector');
            var html = userListItemTemplate({ user: userData });
            $usersList.find('li.status-' + userData.status).after(html);
        };

        this.onUserSelected = function(evt, userData) {
            this.createOrActivateConverstation(userData.userId, { activate: true });
        };

        this.createOrActivateConverstation = function(userId, options) {
            var $usersList = this.select('usersListSelector');
            var activeChat = $usersList.find('li.conversation-' + userId);

            if (!activeChat.length) {
                activeChat = $usersList.find('li.online.user-' + userId).clone();
                if (!activeChat.length) {
                    return;
                }
                activeChat.addClass('conversation-' + userId);
                $usersList.find('li.conversations').after(activeChat);
            }

            if (options && options.activate) {
                $usersList.find('.active').removeClass('active');
                activeChat.addClass('active');
            }
        }

        this.onMessage = function(evt, message) {
            if(message.type == 'chat') {
                var user = message.chat.users[0] === this.currentUserId ? message.chat.users[1] : message.chat.users[0];
                this.createOrActivateConverstation(user.id);
            } else if(message.type == 'chatMessage') {
                // TODO: add badge number
            }
        };


        this.onUserOnlineStatusChanged = function(evt, userData) {
            var $usersList = this.select('usersListSelector');
            var $user = $('.user-' + userData.id, $usersList);
            if ($user.length) {
                $user.remove();
                var html = userListItemTemplate({ user: userData });
                $usersList.find('li.status-' + userData.status).after(html);
            } else {
                this.onNewUserOnline(evt, userData);
            }
        };

        this.doGetOnline = function() {
            var self = this;
            self.usersService.getOnline(function(err, data) {
                if (err) {
                    var $usersList = self.select('usersListSelector');
                    $usersList.html('Could not get online: ' + err);
                    return;
                }

                if(data.messages && data.messages.length > 0) {
                    data.messages.forEach(function(message) {
                        self.trigger(document, 'message', message);
                    });
                }

                if(self.currentUserId != data.user.id) {
                    self.currentUserId = data.user.id;
                    self.trigger(document, 'onlineStatusChanged', data);
                }

                data.users.forEach(function(user) {
                    var currentOnlineUsers = self.onlineUsers.filter(function(u) { return u.id == user.id; });
                    var currentOnlineUser = currentOnlineUsers.length ? currentOnlineUsers[0] : null;

                    if(!currentOnlineUser) {
                        self.trigger(document, 'newUserOnline', user);
                    } else if(currentOnlineUser.status != user.status) {
                        self.trigger(document, 'userOnlineStatusChanged', user);
                    }
                });
                self.onlineUsers = data.users;
            });
        };
    }
});
