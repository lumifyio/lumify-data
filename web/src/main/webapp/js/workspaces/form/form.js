
define([
    'flight/lib/component',
    'tpl!./form',
    'tpl!./shareRow',
    'tpl!./permissions',
    'service/user',
    'service/workspace'
], function(
    defineComponent,
    template,
    shareRowTemplate,
    permissionsTemplate,
    UserService,
    WorkspaceService) {
    'use strict';

    return defineComponent(Form);

    function Form() {

        this.userService = new UserService();
        this.workspaceService = new WorkspaceService();

        this.defaultAttrs({
            titleSelector: '.workspace-title',
            shareListSelector: '.share-list',
            shareFormSelector: '.share-form',
            userSearchSelector: '.share-form input',
            permissionsSelector: '.permissions',
            permissionsRadioSelector: '.popover input',
            deleteSelector: '.delete',
            removeAccessSelector: '.remove-access'
        });

        this.after('teardown', function() {
            $(document).off('click.permPopover');
        });

        this.after('initialize', function() {
            var self = this;

            this.$node.html(template({
                workspace:this.attr.data,
                shareUsers: []
            }));
            this.attr.data.shareUsers = this.attr.data.shareUsers || [];
            this.updatePopovers();

            this.setupTypeahead();
            $(document).on('click.permPopover', function(event) {
                var $target = $(event.target);

                if ($target.closest('.permissions').length === 0) {
                    self.$node.find('.permissions').popover('hide');
                }
            });
            this.on('shareWorkspaceWithUser', this.onShareWorkspaceWithUser);
            this.on('click', {
                deleteSelector: this.onDelete,
                removeAccessSelector: this.onRemoveAccess
            });
            this.on('change', {
                permissionsRadioSelector: this.onPermissionsChange
            });
            this.select('titleSelector').on('change keyup paste', this.onChangeTitle.bind(this));
        });

        var timeout, deferred = [];
        this.saveWorkspace = function() {
            var self = this,
                d = $.Deferred();
            deferred.push(d);

            clearTimeout(timeout);
            timeout = setTimeout(function() {
                console.log('saving', deferred, self.attr.data.title, self.attr.data.shareUsers);
                _.invoke(deferred, 'resolve', { workspace: self.attr.data });
                deferred.length = 0;
            }, 1000);

            return d;
        };

        this.updatePopovers = function() {
            this.makePopover(this.select('permissionsSelector'));
        };

        this.makePopover = function(el) {
            el.popover({
                html: true,
                placement: 'bottom',
                container: this.$node,
                content: function() {
                    var row = $(this).closest('.user-row');
                    return $(permissionsTemplate({})).data('userRow', row);
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

        this.onDelete = function(event) {
            var self = this,
                _rowKey = this.attr.data._rowKey,
                $target = $(event.target),
                previousText = $target.text();

            this.trigger(document, 'workspaceDeleting', { _rowKey:_rowKey });

            $target.text('Deleting...').attr('disabled', true);

            this.workspaceService['delete'](_rowKey)
                .fail(function(xhr) {
                    if (xhr.status === 403) {
                        // TODO: alert user with error:
                        // can't delete other users workspaces
                    }
                })
                .always(function() {
                    $target.text(previousText).removeAttr('disabled');
                })
                .done(function() {
                    self.trigger('workspaceDeleted', { _rowKey:_rowKey });
                });
        };

        this.onRemoveAccess = function(event) {
            var list = $(event.target).closest('.permissions-list'),
                row = list.data('userRow'),
                rowKey = row.data('rowKey');
            
            row.find('.permissions').popover('disable').addClass('loading');
            this.attr.data.shareUsers = _.reject(this.attr.data.shareUsers, function(user) {
                return user.rowKey === rowKey;
            });
            this.saveWorkspace()
                .done(function() {
                    row.remove();
                });
        };

        this.onShareWorkspaceWithUser = function(event, data) {

            var self = this,
                form = this.select('shareFormSelector'),
                row = $(shareRowTemplate({ user:data.user })).insertBefore(form),
                badge = row.find('.permissions');

            badge.addClass('loading');
            this.saveWorkspace().
                done(function() {
                    badge.removeClass('loading');
                    badge.popover('destroy');
                    _.defer(function() {
                        self.makePopover(badge);
                    });
                });
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
