

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
            signSelector: '.sign',
            objectSignSelector: '.object-sign',
            conceptSelector: 'select'
        });

        this.onCreateTermClicked = function(event) {
            var self = this,
                sentence = this.$node.parents('.sentence'),
                sentenceInfo = sentence.data('info'),
                sign = this.select('signSelector').text(),
                newObjectSign = $.trim(this.select('objectSignSelector').val()),
                mentionStart = sentenceInfo.start + sentence.text().indexOf(sign),
                parameters = {
                    sign: sign,
                    conceptLabel: this.select('conceptSelector').val(),
                    sentenceRowKey: sentenceInfo.rowKey,
                    artifactKey: this.attr.artifactKey,
                    mentionStart: mentionStart,
                    mentionEnd: mentionStart + sign.length
                };

            if (newObjectSign.length) {
                parameters.newObjectSign = newObjectSign;
            }

            this.entityService.createTerm(parameters, function(err, data) {
                if (err) {
                    self.trigger(document, 'error', err);
                } else {
                    // TODO: highlight the term?
                    //console.log(data.termRowKey);
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
                sign: this.attr.sign || this.attr.mentionNode.text(),
                objectSign: ''
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
                    var mentionNodeInfo = self.attr.mentionNode && self.attr.mentionNode.data('info');

                    self.select('conceptSelector').html(conceptsTemplate({
                        concepts:concepts,
                        selectedConceptLabel:mentionNodeInfo && mentionNodeInfo.subType
                    }));
                });
            });

            this.on('click', {
                entityConceptMenuSelector: this.onEntityConceptSelected,
                createTermButtonSelector: this.onCreateTermClicked
            });
        });
    }
});
