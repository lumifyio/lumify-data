
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
            addNewSelector: 'button.new',
            deleteSelector: 'button.delete'
        });

        this.onWorkspaceItemClick = function( event ) {
            var rowKey = $(event.target).parents('li').data('rowkey');
            this.trigger( document, 'switchWorkspace', { rowKey: rowKey });
        };

        this.onAddNew = function() {
            // TODO
        };

        this.onDelete = function( event ) {
            var rowKey = $(event.target).parents('li').data('rowkey');
            // TODO: delete workspace
        };

        this.onWorkspaceSwitch = function( event, data ) {
            this.select( 'workspaceListItemSelector' )
                .removeClass('active')
                .each(function() {
                    if ($(this).data('rowkey') == data.rowKey) {
                        $(this).addClass('active');
                        return false;
                    }
                });
        };

        this.loadWorkspaceList = function() {
            this.workspaceService.list(function(err, workspaces) {
                workspaces = workspaces || [];
                this.$node.html( workspacesTemplate({}) );
                this.select( 'listSelector' ).html(
                    listTemplate({
                        results: workspaces,
                        selected: 'chrome'
                    })
                );
            }.bind(this));
        };

        this.after( 'initialize', function() {
            this.loadWorkspaceList();
            this.on( document, 'switchWorkspace', this.onWorkspaceSwitch );
            this.on( 'click', {
                workspaceListItemSelector: this.onWorkspaceItemClick,
                addNewSelector: this.onAddNew,
                deleteSelector: this.onDelete
            });
        });
    }

});
