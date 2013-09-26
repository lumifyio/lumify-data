
define([
    'flight/lib/component',
    'tpl!./form',
    'tpl!./shareRow',
    'tpl!./permissions',
    'service/user'
], function(
    defineComponent,
    template,
    shareRowTemplate,
    permissionsTemplate,
    UserService) {
    'use strict';

    return defineComponent(Form);

    function Form() {

        this.userService = new UserService();

        this.defaultAttrs({
            titleSelector: '.workspace-title',
            shareListSelector: '.share-list',
            shareFormSelector: '.share-form',
            userSearchSelector: '.share-form input',
            permissionsSelector: '.permissions',
            permissionsRadioSelector: '.popover input',
            removeAccessSelector: '.remove-access'
        });

        this.after('initialize', function() {
            this.$node.html(template({
                workspace:this.attr.data,
                shareUsers: []
            }));
            this.attr.data.shareUsers = this.attr.data.shareUsers || [];
            this.updatePopovers();

            this.saveWorkspace = _.debounce(this.saveWorkspace.bind(this), 1000);
            this.setupTypeahead();
            this.on('shareWorkspaceWithUser', this.onShareWorkspaceWithUser);
            this.on('click', {
                permissionsSelector: this.onPermissionsClick,
                removeAccessSelector: this.onRemoveAccess
            });
            this.on('change', {
                permissionsRadioSelector: this.onPermissionsChange
            });
            this.select('titleSelector').on('change keyup paste', this.onChangeTitle.bind(this));
        });

        this.saveWorkspace = function() {
            console.log('saving', this.attr.data.title, this.attr.data.shareUsers);
        };

        this.updatePopovers = function() {
            this.select('permissionsSelector').popover({
                html: true,
                placement: 'bottom',
                content: function() {
                    return permissionsTemplate({});
                }
            });
        };

        this.onChangeTitle = function(event) {
            var $target = $(event.target),
                val = $target.val();

            if ($.trim(val).length === 0) {
                return;
            }

            if (val !== this.attr.data.title) {
                this.attr.data.title = val;
                this.saveWorkspace();
            }
        };

        this.onPermissionsChange = function(event) {
            debugger;
        };

        this.onPermissionsClick = function(event) {
            var $target = $(event.target).closest('.permissions');

            _.defer(function() {
                $(document).one('click', function() {
                    $target.popover('hide');
                });
            });
        };

        this.onRemoveAccess = function(event) {
            var row = $(event.target).closest('.user-row'),
                rowKey = row.data('rowKey');
            
            this.attr.data.shareUsers = _.reject(this.attr.data.shareUsers, function(user) {
                return user._rowKey === rowKey;
            });
            this.saveWorkspace();
        };

        this.onShareWorkspaceWithUser = function(event, data) {
            this.select('shareFormSelector').before(
                shareRowTemplate({ user:data.user })
            );

            this.updatePopovers();
            this.saveWorkspace();
        };

        this.setupTypeahead = function() {
            var self = this,
                userMap = {};

            this.select('userSearchSelector').typeahead({
                source: function(query, callback) {
                    self.userService.getCurrentUsers()
                        .done(function(response) {
                            var users = response.users,
                                regex = new RegExp(query, 'i'),
                                search = users.filter(function(user) {
                                    userMap[user.userName] = user;
                                    return regex.test(user.userName);
                                }),
                                names = _.pluck(search, 'userName');
                                
                            callback(names);
                        });
                },
                updater: function(userName) {
                    var user = userMap[userName];
                    if (user) {
                        self.attr.data.shareUsers.push(user);
                        self.trigger('shareWorkspaceWithUser', {
                            workspace: self.attr.data,
                            user: user
                        });
                    }
                    return '';
                }
            });
        };
    }
});
