
define([
    'flight/lib/component',
    'data',
    'service/workspace',
    'service/user',
    './form/form',
    'tpl!./workspaces',
    'tpl!./list',
    'tpl!./item'
], function(defineComponent,
    appData,
    WorkspaceService,
    UserService,
    WorkspaceForm,
    workspacesTemplate,
    listTemplate,
    itemTemplate) {
    'use strict';

    return defineComponent(Workspaces);

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

        this.onWorkspaceItemClick = function( event ) {
            var $target = $(event.target);

			if ($target.is('input') || $target.is('button') || $target.is('.new-workspace')) return;
            if ($target.closest('.workspace-form').length) return;

            var _rowKey = $(event.target).parents('li').data('_rowKey');
            if (_rowKey) {
                this.trigger( document, 'switchWorkspace', { _rowKey: _rowKey });
            }
        };

        this.onAddNew = function(event) {
            var self = this;

            var title = $(event.target).parents('li').find('input').val();
            if (!title) return;

            var data = { title: title };
            this.workspaceService.saveNew(data)
                .done(function(workspace) {
                    self.loadWorkspaceList()
                        .done(function() {
                            self.onWorkspaceLoad(null, workspace);
                            self.trigger( document, 'switchWorkspace', { _rowKey: workspace._rowKey });
                        });
                });
        };

        this.onInputKeyUp = function(event) {
            switch (event.which) {
                case $.ui.keyCode.ENTER:
                    this.onAddNew(event);
            }
        };

        this.onDisclosure = function( event ) {
            var self = this,
                $target = $(event.target),
                data = $target.closest('li').data();

            event.preventDefault();

            this.trigger( document, 'switchWorkspace', { _rowKey: data._rowKey });

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

            if (instance && instance.attr.data._rowKey === data._rowKey) {
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

        this.onSwitchWorkspace = function ( event, data ) {
            this.collapseEditForm();
        };

        this.onWorkspaceDeleted = function ( event, data ) {
            this.collapseEditForm();

            this.loadWorkspaceList();
        };

        this.onWorkspaceLoad = function ( event, data ) {
            this.updateListItemWithData(data);
            this.switchActive( data.id );
        };

        this.findWorkspaceRow = function(rowKey) {
            return this.select('workspaceListItemSelector').filter(function() {
                return $(this).data('_rowKey') == rowKey;
            });
        };

        this.onWorkspaceSaving = function ( event, data ) {
            var li = this.findWorkspaceRow(data._rowKey);
            li.find('.badge').addClass('loading').show().next().hide();
        };

        this.updateListItemWithData = function(data) {
            if (!this.usersByRowKey) return;
            var li = this.findWorkspaceRow(data._rowKey);
            li.find('.badge').removeClass('loading').hide().next().show();
            data = this.workspaceDataForItemRow(data);
            var content = $(itemTemplate({ workspace: data, selected: this.workspaceRowKey }));
            if (li.length === 0) {
                this.$node.find('li.nav-header').eq(+data.isSharedToUser).after(content);
            } else {
                li.replaceWith(content);
            }
        };

        this.onWorkspaceSaved = function ( event, data ) {
            this.updateListItemWithData(data);

            this.trigger(document, 'workspaceRemoteSave', data);
        };

        this.onWorkspaceRemoteSave = function ( event, data) {
            if (!data || !data.remoteEvent) return;

            if (this.workspaceRowKey === data._rowKey) {
                appData.loadWorkspace(data);
            } else {
                this.updateListItemWithData(data);
            }
        };

        this.onWorkspaceNotAvailable = function ( event, data) {
            this.loadWorkspaceList();
        };

        this.switchActive = function( rowKey ) {
            var self = this;
            this.workspaceRowKey = rowKey;

            var found = false;
            this.select( 'workspaceListItemSelector' )
                .removeClass('active')
                .each(function() {
                    if ($(this).data('_rowKey') == rowKey) {
                        found = true;
                        $(this).addClass('active');
                        return false;
                    }
                });

            if (!found) {
                this.loadWorkspaceList();
            }
        };

        this.loadWorkspaceList = function() {
            var self = this;

            return $.when(
                    this.userService.getCurrentUsers(),
                    this.workspaceService.list()
                   )
                   .done(function(usersResponse, workspaceResponse) {
                       var users = usersResponse[0].users || [],
                           workspaces = workspaceResponse[0].workspaces || [],
                           usersByRowKey = _.groupBy(users, function(u) { return u.rowKey; }); 

                        self.usersByRowKey = usersByRowKey;
                        self.$node.html( workspacesTemplate({}) );
                        self.select('listSelector').html(
                            listTemplate({
                                results: _.groupBy(workspaces, function(w) { 
                                    w = self.workspaceDataForItemRow(w);
                                    return w.isSharedToUser ? 'shared' : 'mine';
                                }),
                                selected: self.workspaceRowKey
                            })
                        );
                        self.trigger(document, 'paneResized');
                    });
        };

        this.workspaceDataForItemRow = function(w) {
            var createdBy = w.createdBy,
                foundUsers = this.usersByRowKey[w.createdBy];

            if (foundUsers && foundUsers.length) {
                createdBy = foundUsers[0].userName;
            }

            var text = w.isSharedToUser ? 'Shared by ' + createdBy + ' to': 'Shared with',
                people = (w.permissions && w.permissions.length) ||
                        (w.users && w.users.length) || 0;

            if (people === 1) {
                w.sharingSubtitle = text + ' 1 person';
            } else if (people) {
                w.sharingSubtitle = text + ' ' + people + ' people';
            } else {
                w.sharingSubtitle = null;
            }
            return w;
        };

        this.onToggleMenu = function(event, data) {
            if (data.name === 'workspaces') {
                this.loadWorkspaceList();
            }
        };

        this.after( 'initialize', function() {
            this.on( document, 'workspaceLoaded', this.onWorkspaceLoad );
            this.on( document, 'workspaceSaving', this.onWorkspaceSaving );
            this.on( document, 'workspaceSaved', this.onWorkspaceSaved );
            this.on( document, 'workspaceDeleted', this.onWorkspaceDeleted );
            this.on( document, 'workspaceRemoteSave', this.onWorkspaceRemoteSave );
            this.on( document, 'workspaceNotAvailable', this.onWorkspaceNotAvailable );

            this.on( document, 'menubarToggleDisplay', this.onToggleMenu );
            this.on( document, 'switchWorkspace', this.onSwitchWorkspace );
            this.on( 'click', {
                workspaceListItemSelector: this.onWorkspaceItemClick,
                addNewSelector: this.onAddNew,
                disclosureSelector: this.onDisclosure
            });
            this.on( 'keyup', {
                addNewInputSelector: this.onInputKeyUp
            });
        });
    }

});
