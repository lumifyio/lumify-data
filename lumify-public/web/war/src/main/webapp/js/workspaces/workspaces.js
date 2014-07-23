
define([
    'flight/lib/component',
    'data',
    'service/workspace',
    'service/user',
    './form/form',
    'tpl!./workspaces',
    'tpl!./list',
    'tpl!./item',
    'util/jquery.flight',
    'util/withAsyncQueue'
], function(defineComponent,
    appData,
    WorkspaceService,
    UserService,
    WorkspaceForm,
    workspacesTemplate,
    listTemplate,
    itemTemplate,
    jqueryFlight,
    withAsyncQueue) {
    'use strict';

    return defineComponent(Workspaces, withAsyncQueue);

    function Workspaces() {
        this.workspaceService = new WorkspaceService();
        this.userService = new UserService();

        this.defaultAttrs({
            listSelector: 'ul.nav-list',
            workspaceListItemSelector: 'ul.nav-list li',
            addNewInputSelector: 'input.new',
            addNewSelector: 'button.new',
            disclosureSelector: 'button.disclosure',
            formSelector: '.workspace-form'
        });

        this.onWorkspaceItemClick = function(event) {
            var $target = $(event.target);

            if ($target.is('input') || $target.is('button') || $target.is('.new-workspace')) return;
            if ($target.closest('.workspace-form').length) return;

            var li = $(event.target).closest('li'),
                workspaceId = li.data('workspaceId').toString();

            if (workspaceId) {
                this.switchToWorkspace(workspaceId);
            }
        };

        this.switchToWorkspace = function(workspaceId) {
            if (workspaceId === appData.workspaceId) return;
            this.trigger(document, 'switchWorkspace', { workspaceId: workspaceId });
        };

        this.onAddNew = function(event) {
            var self = this,
                title = $(event.target).parents('li').find('input').val();

            if (!title) return;

            this.workspaceService.saveNew(title)
                .done(function(workspace) {
                    self.loadWorkspaceList()
                        .done(function() {
                            self.onWorkspaceLoad(null, workspace);
                            self.switchToWorkspace(workspace.workspaceId)
                        });
                });
        };

        this.onInputKeyUp = function(event) {
            switch (event.which) {
                case $.ui.keyCode.ENTER:
                    this.onAddNew(event);
            }
        };

        this.onDisclosure = function(event) {
            var self = this,
                $target = $(event.target),
                data = $target.closest('li').data();

            event.preventDefault();

            this.switchToWorkspace(data.workspaceId);

            var container = this.select('formSelector'),
                form = container.resizable({
                        handles: 'e',
                        minWidth: 120,
                        maxWidth: 250,
                        resize: function() {
                            self.trigger(document, 'paneResized');
                        }
                    }).show().find('.content'),
                instance = form.lookupComponent(WorkspaceForm);

            if (instance && instance.attr.data.workspaceId === data.workspaceId) {
                container.hide();
                instance.teardown();
                return self.trigger(document, 'paneResized');
            }

            WorkspaceForm.teardownAll();
            WorkspaceForm.attachTo(form, {
                data: data
            });

            self.trigger(document, 'paneResized');
        };

        this.collapseEditForm = function() {
            if (this.select('formSelector').is(':visible')) {
                WorkspaceForm.teardownAll();
                this.select('formSelector').hide();
                this.trigger(document, 'paneResized');
            }
        };

        this.onWorkspaceDeleting = function(event, data) {
            this.deletingWorkspace = $.extend({}, data);
        };

        this.onWorkspaceDeleted = function(event, data) {
            this.collapseEditForm();
            this.loadWorkspaceList();

            if (this.deletingWorkspace) {
                this.trigger(document, 'workspaceRemoteSave', this.deletingWorkspace);
            }
        };

        this.onSwitchWorkspace = function(event, data) {
            var li = this.findWorkspaceRow(data.workspaceId);
            if (li.length) {
                this.switchActive(data.workspaceId);
                li.find('.badge').addClass('loading').show().next().hide();
            }
            this.collapseEditForm();
        };

        this.onWorkspaceLoad = function(event, data) {
            this.updateListItemWithData(data);
            this.switchActive(data.workspaceId);
        };

        this.findWorkspaceRow = function(workspaceId) {
            return this.select('workspaceListItemSelector').filter(function() {
                return $(this).data('workspaceId') == workspaceId;
            });
        };

        this.onWorkspaceSaving = function(event, data) {
            var li = this.findWorkspaceRow(data.workspaceId);
            li.find('.badge').addClass('loading').show().next().hide();
        };

        this.onWorkspaceSaved = function(event, data) {
            this.updateListItemWithData(data);
            this.trigger(document, 'workspaceRemoteSave', data);
        };

        this.updateListItemWithData = function(data, timestamp) {
            if (!this.usersById) return;

            this.currentUserReady(function(currentUser) {
                var li = this.findWorkspaceRow(data.workspaceId);
                if (data.createdBy === currentUser.id ||
                    _.contains(_.pluck(data.users, 'userId'), currentUser.id)
                ) {
                    li.find('.badge').removeClass('loading').hide().next().show();
                    data = this.workspaceDataForItemRow(data);
                    var content = $(itemTemplate({ workspace: data, selected: this.workspaceId }));
                    if (li.length === 0) {
                        this.$node.find('li.nav-header').eq(data.isSharedToUser ? 1 : 0).after(content);
                    } else {
                        li.replaceWith(content);
                    }

                    // Sort section because title might be renamed
                    var lis = this.getWorkspaceListItemsInSection(data.isSharedToUser),
                        titleGetter = function() {
                            return $(this).data('title');
                        },
                        lowerCase = function(s) {
                            return s.toLowerCase();
                        },
                        titles = _.sortBy(lis.map(titleGetter).get(), lowerCase),
                        insertIndex = _.indexOf(titles, data.title),
                        currentIndex = lis.index(content);

                    if (currentIndex < insertIndex) {
                        content.insertAfter(lis.eq(insertIndex));
                    } else if (currentIndex > insertIndex) {
                        content.insertBefore(lis.eq(insertIndex));
                    }
                } else li.remove();
            });
        };

        this.getWorkspaceListItemsInSection = function(shared) {
            if (shared) {
                return this.select('listSelector')
                    .children('.nav-header').eq(1)
                    .nextUntil();
            } else {
                return this.select('listSelector')
                    .children('.nav-header').eq(0)
                    .nextUntil('.new-workspace');
            }
        };

        this.onWorkspaceCopied = function(event, data) {
            this.collapseEditForm();
            this.loadWorkspaceList();
        };

        this.onWorkspaceRemoteSave = function(event, data) {
            var self = this;

            if (!data || !data.remoteEvent) return;

            this.currentUserReady(function(currentUser) {
                self.workspaceService.getByRowKey(data.workspaceId)
                    .fail(function() {
                        if (self.workspaceId === data.workspaceId) {
                            self.loadWorkspaceList(true);
                        } else {
                            self.loadWorkspaceList();
                        }
                    })
                    .done(function(data) {
                        var userAccess = _.findWhere(data.users, { userId: currentUser.id });
                        data.isEditable = (/write/i).test(userAccess && userAccess.access);
                        data.isSharedToUser = data.createdBy !== currentUser.id;

                        if (self.workspaceId === data.workspaceId) {
                            appData.loadWorkspace(data);
                        } else {
                            self.updateListItemWithData(data);
                        }
                    });
            });
        };

        this.onWorkspaceNotAvailable = function(event, data) {
            this.loadWorkspaceList();
            this.trigger('displayInformation', { message: 'Workspace not found' });
        };

        this.switchActive = function(workspaceId) {
            var self = this;
            this.workspaceId = workspaceId;

            var found = false;
            this.select('workspaceListItemSelector')
                .removeClass('active')
                .each(function() {
                    if ($(this).data('workspaceId') == workspaceId) {
                        found = true;
                        $(this).addClass('active');
                        return false;
                    }
                });

            if (!found) {
                this.loadWorkspaceList();
            }
        };

        this.loadWorkspaceList = function(switchToFirst) {
            var self = this;

            return $.when(
                    this.userService.getCurrentUsers(),
                    this.workspaceService.list()
                   )
                   .done(function(usersResponse, workspaceResponse) {
                       var users = usersResponse[0].users || [],
                           workspaces = workspaceResponse[0].workspaces || [],
                           usersById = _.indexBy(users, function(u) {
                               return u.id;
                           });

                        self.usersById = usersById;
                        self.$node.html(workspacesTemplate({}));
                        self.select('listSelector').html(
                            listTemplate({
                                results: _.chain(workspaces)
                                    .map(self.workspaceDataForItemRow.bind(self))
                                    .sortBy(function(w) {
                                        return w.title.toLowerCase()
                                    })
                                    .groupBy(function(w) {
                                        return w.isSharedToUser ? 'shared' : 'mine';
                                    })
                                    .value(),
                                selected: self.workspaceId
                            })
                        );
                        self.trigger(document, 'paneResized');
                        if (switchToFirst) {
                            self.switchToWorkspace(workspaces[0].workspaceId);
                        }
                    });
        };

        this.workspaceDataForItemRow = function(w) {
            var row = $.extend({}, w),
                createdBy = this.usersById[row.createdBy].displayName,
                text = row.isSharedToUser ? 'Shared by ' + createdBy + ' to' : 'Shared with',
                usersNotCurrent = row.users.filter(function(u) {
                    return u.userId != window.currentUser.id;
                }),
                people = usersNotCurrent.length;

            if (people === 1 && row.isSharedToUser) {
                row.sharingSubtitle = text + ' you';
            } else if (people === 1) {
                row.sharingSubtitle = text + ' 1 person';
            } else if (people) {
                row.sharingSubtitle = text + ' ' + people + ' people';
            } else {
                row.sharingSubtitle = null;
            }
            return row;
        };

        this.onToggleMenu = function(event, data) {
            if (data.name === 'workspaces') {
                if (this.$node.closest('.visible').length) {
                    this.loadWorkspaceList();
                } else {
                    this.collapseEditForm();
                }
            }
        };

        this.registerForCurrentUser = function() {
            var self = this;

            this.setupAsyncQueue('currentUser');
            if (window.currentUser) {
                this.currentUserMarkReady(window.currentUser);
            } else {
                this.on(document, 'currentUserChanged', function(event, data) {
                    self.currentUserMarkReady(data.user);
                });
            }
        }

        this.after('initialize', function() {

            this.registerForCurrentUser();

            this.on(document, 'workspaceLoaded', this.onWorkspaceLoad);
            this.on(document, 'workspaceSaving', this.onWorkspaceSaving);
            this.on(document, 'workspaceSaved', this.onWorkspaceSaved);
            this.on(document, 'workspaceDeleted', this.onWorkspaceDeleted);
            this.on(document, 'workspaceCopied', this.onWorkspaceCopied);
            this.on(document, 'workspaceRemoteSave', this.onWorkspaceRemoteSave);
            this.on(document, 'workspaceNotAvailable', this.onWorkspaceNotAvailable);

            this.on(document, 'menubarToggleDisplay', this.onToggleMenu);
            this.on(document, 'switchWorkspace', this.onSwitchWorkspace);

            this.on('workspaceDeleting', this.onWorkspaceDeleting);
            this.on('click', {
                workspaceListItemSelector: this.onWorkspaceItemClick,
                addNewSelector: this.onAddNew,
                disclosureSelector: this.onDisclosure
            });
            this.on('keyup', {
                addNewInputSelector: this.onInputKeyUp
            });
        });
    }

});
