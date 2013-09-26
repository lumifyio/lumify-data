
define([
    'flight/lib/component',
    'service/workspace',
    './form/form',
    'tpl!./workspaces',
    'tpl!./list',
    'tpl!./item'
], function(defineComponent, WorkspaceService, WorkspaceForm, workspacesTemplate, listTemplate, itemTemplate) {
    'use strict';

    return defineComponent(Workspaces);

    function Workspaces() {
        this.workspaceService = new WorkspaceService();

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
            this.trigger( document, 'switchWorkspace', { _rowKey: _rowKey });
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

            var container = this.select('formSelector'),
                form = container.resizable({
                    handles: 'e',
                    minWidth: 120,
                    maxWidth: 250,
                    resize: function() {
                        self.trigger(document, 'paneResized');
                    }
                }).show().find('.content');
            
            var instance = form.lookupComponent(WorkspaceForm);
            if (instance && instance.attr.data._rowKey === data._rowKey) {
                container.hide();
                instance.teardown();
                return self.trigger(document, 'paneResized');
            }
            
            WorkspaceForm.teardownAll();
            WorkspaceForm.attachTo(form, {
                data: data
            });

            this.trigger(document, 'paneResized');
        };

        this.onDelete = function(event) {
            var self = this,
                $target = $(event.target),
                currentRowKey = $target.closest('ul').find('.active').data('_rowKey'),
                _rowKey = $target.closest('li').data('_rowKey'),
                $loading = $("<span>").addClass("badge loading");

            this.trigger(document, 'workspaceDeleting', { _rowKey: _rowKey });

            $target.replaceWith($loading);

            this.workspaceService['delete'](_rowKey)
                .fail(function(xhr) {
                    if (xhr.status === 403) {
                        // TODO: alert user with error:
                        // can't delete other users workspaces
                    }
                })
                .always(this.loadWorkspaceList.bind(this))
                .always(this.trigger.bind(this, document, 'workspaceDeleted', { _rowKey: _rowKey }))
                .done(function() {
                    if ( currentRowKey != _rowKey ) {
                        self.switchActive(currentRowKey);
                    }
                });
        };

        this.onWorkspaceLoad = function ( event, data ) {
            this.switchActive( data.id );
        };

        this.onWorkspaceSaved = function ( event, data ) {
            var li = this.select('workspaceListItemSelector').filter(function() {
                return $(this).data('_rowKey') == data._rowKey;
            });
            if (li.length === 0) {
                $(itemTemplate({
                    workspace: data,
                    selected: data._rowKey
                })).insertAfter( this.$node.find('li.nav-header') );
            }
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
                        self.trigger(document, 'workspaceSwitched', {
                            workspace: $(this).data()
                        });
                        return false;
                    }
                });

            if (!found) {
                this.loadWorkspaceList();
            }
        };

        this.loadWorkspaceList = function() {
            var self = this;

            return this.workspaceService.list()
                        .done(function(data) {
                            var workspaces = data.workspaces || [];
                            self.$node.html( workspacesTemplate({}) );
                            self.select('listSelector').html(
                                listTemplate({
                                    results: workspaces,
                                    selected: self.workspaceRowKey
                                })
                            );
                        });
        };

        this.onToggleMenu = function(event, data) {
            if (data.name === 'workspaces') {
                this.loadWorkspaceList();
            }
        };

        this.after( 'initialize', function() {
            this.on( document, 'workspaceLoaded', this.onWorkspaceLoad );
            this.on( document, 'workspaceSaved', this.onWorkspaceSaved );
            this.on( document, 'menubarToggleDisplay', this.onToggleMenu );
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
