
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
			if ($(event.target).is('input') || $(event.target).is('button') || $(event.target).is('.new-workspace')) {
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
				this.loadWorkspaceList(function () {
				    this.onWorkspaceLoad(null, workspace);
				    this.trigger( document, 'switchWorkspace', { _rowKey: workspace.workspaceId });
				}.bind(this));
			}.bind(this));
        };

        this.onInputKeyUp = function(event) {
            switch (event.which) {
                case $.ui.keyCode.ENTER:
                    this.onAddNew(event);
            }
        };

        this.onDelete = function( event ) {
            var currentRowKey = this.select('listSelector').find('li.active').data('_rowKey');
            var _rowKey = $(event.target).parents('li').data('_rowKey'),
                loading = $("<span>")
                            .addClass("badge")
                            .addClass("loading");
            this.trigger(document, 'workspaceDeleting', { _rowKey: _rowKey });
            $(event.target).replaceWith(loading);
            this.workspaceService.delete(_rowKey, function() {
                this.loadWorkspaceList(function () {
                    this.trigger(document, 'workspaceDeleted', { _rowKey: _rowKey });
                    if ( currentRowKey != _rowKey ) {
                        this.switchActive(currentRowKey);
                    }
                }.bind(this));
            }.bind(this));
        };

        this.onWorkspaceLoad = function ( event, data ) {
            this.switchActive( data.id );
        }

        this.switchActive = function( rowKey ) {
            this.select( 'workspaceListItemSelector' )
                .removeClass('active')
                .each(function() {
                    if ($(this).data('_rowKey') == rowKey) {
                        $(this).addClass('active');
                        return false;
                    }
                });
        };

        this.loadWorkspaceList = function(callback) {
            this.workspaceService.list(function(err, workspaces) {
                workspaces = workspaces || [];
                this.$node.html( workspacesTemplate({}) );
                this.select( 'listSelector' ).html(
                    listTemplate({
                        results: workspaces,
                        selected: this.workspaceRowKey
                    })
                );
                if (callback) {
                    callback(err,workspaces);
                }
            }.bind(this));
        };

        this.after( 'initialize', function() {
            this.loadWorkspaceList();
            this.on( document, 'workspaceLoaded', this.onWorkspaceLoad );
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
