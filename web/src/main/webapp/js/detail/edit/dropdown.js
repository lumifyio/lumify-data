

define([
    'flight/lib/component',
    'tpl!./dropdown',
    'service/entity'
], function(defineComponent, dropdownTemplate, EntityService) {
    'use strict';

    return defineComponent(EditDropdown);


    function EditDropdown() {
        this.entityService = new EntityService();

        this.defaultAttrs({
            entityConceptMenuSelector: '.underneath .dropdown-menu a',
            createTermButtonSelector: '.create-term',
            termInputSelector: 'input',
            conceptSelector: 'select'
        });

        this.onCreateTermClicked = function(event) {
            this.entityService.createEntity({
                termText: this.select('termInputSelector').val(),
                conceptType: this.select('conceptSelector').val()
            }, function(err, data) {
                // TODO:
                console.log(err, data);
            });
        };

        this.after('teardown', function() {
            this.$node.remove();
        });

        this.after('initialize', function() {
            var node = this.$node;

            node.html(dropdownTemplate({
                type: 'Set type of term',
                text: this.attr.term || this.attr.mentionNode.text(),
                entityTypes: 'organization person date location money'.split(' ')
            }));

            _.defer(function() {
                node.one('transitionend', function() {
                    node.css({
                        transition: 'none',
                        height:'auto',
                        overflow: 'visible'
                    });
                });
                node.css({height:node.children('div').outerHeight() + 'px'});
            });

            this.on('click', {
                entityConceptMenuSelector: this.onEntityConceptSelected,
                createTermButtonSelector: this.onCreateTermClicked
            });
        });
    }
});
