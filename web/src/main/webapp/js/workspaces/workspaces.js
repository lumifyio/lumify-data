
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
            var rowKey = $(event.target).parents('li').data('rowkey');
            this.trigger( document, 'switchWorkspace', { rowKey: rowKey });
        };

        this.onAddNew = function() {
            var title = $(event.target).parents('li').children('input')[0].value;
            if (!title) return;
            var data = { title: title };
            this.workspaceService.saveNew(data, this.loadWorkspaceList.bind(this));
        };

        this.onInputKeyUp = function(event) {
            switch (event.which) {
                case $.ui.keyCode.ENTER:
                    this.onAddNew(event);
            }
        };

        this.onDelete = function( event ) {
            var rowKey = $(event.target).parents('li').data('rowkey');
            this.workspaceService.delete(rowKey, this.loadWorkspaceList.bind(this));
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
            this.on( 'keyup', {
                addNewInputSelector: this.onInputKeyUp
            });
        });
    }

});
