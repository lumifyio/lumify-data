

define([
    'flight/lib/component',
    'tpl!./dropdown',
    'tpl!./concept-options',
    'service/entity'
], function(defineComponent, dropdownTemplate, conceptsTemplate, EntityService) {
    'use strict';

    return defineComponent(EditDropdown);


    function EditDropdown() {
        this.entityService = new EntityService();

        this.defaultAttrs({
            entityConceptMenuSelector: '.underneath .dropdown-menu a',
            createTermButtonSelector: '.create-term',
            termSelector: '.sign',
            conceptSelector: 'select'
        });

        this.onCreateTermClicked = function(event) {
            var self = this,
                sentence = this.$node.parents('.sentence'),
                sentenceInfo = sentence.data('info'),
                sign = this.select('termSelector').text(),
                mentionStart = sentenceInfo.start + sentence.text().indexOf(sign);

            this.entityService.createTerm({
                sign: sign,
                conceptLabel: this.select('conceptSelector').val(),
                artifactKey: this.attr.artifactKey,
                mentionStart: mentionStart,
                mentionEnd: mentionStart + sign.length
            }, function(err, data) {
                if (err) {
                    self.trigger(document, 'error', err);
                } else {
                    console.log(data.termRowKey);
                    self.teardown();
                }
            });
        };

        this.after('teardown', function() {
            this.$node.remove();
        });

        this.after('initialize', function() {
            var self = this,
                node = this.$node;

            node.html(dropdownTemplate({
                type: 'Set type of term',
                text: this.attr.term || this.attr.mentionNode.text()
            }));

            _.defer(function() {
                node.one('transitionend', function() {
                    node.css({
                        transition: 'none',
                        height:'auto',
                        overflow: 'visible'
                    });
                });
                var form = node.find('.term-form');
                node.css({ height:form.outerHeight(true) + 'px' });

                self.entityService.concepts(function(err, concepts) {
                    self.select('conceptSelector').html(conceptsTemplate({concepts:concepts}));
                });
            });

            this.on('click', {
                entityConceptMenuSelector: this.onEntityConceptSelected,
                createTermButtonSelector: this.onCreateTermClicked
            });
        });
    }
});
