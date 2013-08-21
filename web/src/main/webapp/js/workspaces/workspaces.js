
define([
    'flight/lib/component',
    'service/workspace',
    'tpl!./workspaces',
    'tpl!./list'
], function(defineComponent, WorkspaceService, workspacesTemplate, listTemplate) {
    'use strict';

    return defineComponent(Workspaces);

    function Workspaces() {
        this.workspaceService = new WorkspaceService();

        this.defaultAttrs({
            listSelector: 'ul.nav-list',
            workspaceListItemSelector: 'ul.nav-list li',
            addNewInputSelector: 'input.new',
            addNewSelector: 'button.new',
            deleteSelector: 'button.delete'
        });

        this.onWorkspaceItemClick = function( event ) {
			if ($(event.target).is('input') || $(event.target).is('button')) {
				return;
			}
            var _rowKey = $(event.target).parents('li').data('_rowKey');
            this.trigger( document, 'switchWorkspace', { _rowKey: _rowKey });
        };

        this.onAddNew = function(event) {
            var title = $(event.target).parents('li').children('input')[0].value;
            if (!title) return;
            var data = { title: title };
            this.workspaceService.saveNew(data, function (err, workspace) {
				this.loadWorkspaceList();
				this.trigger( document, 'switchWorkspace', { _rowKey: workspace.workspaceId });
			}.bind(this));
        };

        this.onInputKeyUp = function(event) {
            switch (event.which) {
                case $.ui.keyCode.ENTER:
                    this.onAddNew(event);
            }
        };

        this.onDelete = function( event ) {
            var _rowKey = $(event.target).parents('li').data('_rowKey'),
                loading = $("<span>")
                            .addClass("badge")
                            .addClass("loading");
            this.trigger(document, 'workspaceDeleting', { _rowKey: _rowKey });
            $(event.target).replaceWith(loading);
            this.workspaceService.delete(_rowKey, function() {
                this.trigger(document, 'workspaceDeleted', { _rowKey: _rowKey });
                this.loadWorkspaceList.apply(this, arguments);
            }.bind(this));
        };

        this.onWorkspaceSwitch = function( event, data ) {
            this.select( 'workspaceListItemSelector' )
                .removeClass('active')
                .each(function() {
                    if ($(this).data('_rowKey') == data._rowKey) {
                        $(this).addClass('active');
                        return false;
                    }
                });
        };

		this.onWorkspaceSaved = function ( event, data ) {
			this.loadWorkspaceList();
		};

        this.loadWorkspaceList = function() {
            this.workspaceService.list(function(err, workspaces) {
                workspaces = workspaces || [];
                this.$node.html( workspacesTemplate({}) );
                this.select( 'listSelector' ).html(
                    listTemplate({
                        results: workspaces,
                        selected: this.workspaceRowKey
                    })
                );
            }.bind(this));
        };

        this.after( 'initialize', function() {
            this.loadWorkspaceList();
            this.on( document, 'switchWorkspace', this.onWorkspaceSwitch );
			this.on( document, 'workspaceSaved', this.onWorkspaceSaved );
            this.on( 'click', {
                workspaceListItemSelector: this.onWorkspaceItemClick,
                addNewSelector: this.onAddNew,
                deleteSelector: this.onDelete
            });
            this.on( 'keyup', {
                addNewInputSelector: this.onInputKeyUp
            });
        });
    }

});
