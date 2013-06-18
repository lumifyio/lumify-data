
define([
    'flight/lib/component',
    'tpl!./workspaces',
    'tpl!./list'
], function(defineComponent, workspacesTemplate, listTemplate) {
    'use strict';

    return defineComponent(Workspaces);

    function Workspaces() {

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


        this.after( 'initialize', function() {

            // TODO: subscribe to workspace server events instead
            var results = [
             { rowKey: 'chrome' },
             { rowKey: 'chrome2' },
             { rowKey: 'chrome3' },
             { rowKey: 'chrome4' }
            ];

            this.$node.html( workspacesTemplate({}) );

            this.select( 'listSelector' ).html( 
                listTemplate({
                    results:results,
                    selected: 'chrome'
                }) 
            );

            this.on( document, 'switchWorkspace', this.onWorkspaceSwitch );
            this.on( 'click', {
                workspaceListItemSelector: this.onWorkspaceItemClick,
                addNewSelector: this.onAddNew,
                deleteSelector: this.onDelete
            });
        });
    }

});
