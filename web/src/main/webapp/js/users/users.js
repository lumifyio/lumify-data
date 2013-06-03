
define([
    'flight/lib/component',
    'service/user',
    'tpl!./users',
    'tpl!./userListItem'
], function(defineComponent, UsersService, usersTemplate, userListItemTemplate) {
    'use strict';

    return defineComponent(Users);

    function Users() {
        this.usersService = new UsersService();
        this.currentUserId = null;
        this.onlineUsers = [];

        this.defaultAttrs({
            usersListSelector: '#users-list',
            userListItemSelector: '#users-list .user'
        });

        this.after('initialize', function() {
            this.$node.html(usersTemplate({}));
            this.on(document, 'newUserOnline', this.onNewUserOnline);
            this.on(document, 'userOnlineStatusChanged', this.onUserOnlineStatusChanged);
            this.on('click', {
                userListItemSelector: this.onUserListItemClicked
            });

            this.doGetOnline();
            setInterval(this.doGetOnline.bind(this), 500); // TODO use long polling
        });

        this.onUserListItemClicked = function(evt) {
            var $target = $(evt.target);
            if(!$target.attr('user-id')) {
                $target = $target.parents('.user');
            }
            var userId = $target.attr('user-id');

            this.trigger(document, 'userSelected', { userId: userId });
        };

        this.onNewUserOnline = function(evt, userData) {
            var $usersList = this.select('usersListSelector');
            var html = userListItemTemplate({ user: userData });
            $usersList.append(html);
        };

        this.onUserOnlineStatusChanged = function(evt, userData) {
            var $usersList = this.select('usersListSelector');
            var $user = $('.user-' + userData.id, $usersList);
            if($user.length == 0) {
                this.onNewUserOnline(evt, userData);
            } else {
                var html = userListItemTemplate({ user: userData });
                $user.replaceWith(html);
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
                    var currentOnlineUser = currentOnlineUsers.length == 0 ? null : currentOnlineUsers[0];

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
