

define([
    'flight/lib/component',
    'tpl!./dropdown',
    'tpl!./concept-options',
    'service/entity',
    'underscore'
], function(defineComponent, dropdownTemplate, conceptsTemplate, EntityService, _) {
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

        this.highlightTerm = function(data) {
            if ( ! this.attr.selection ) {
                return;
            }

            var textNode = this.node.previousSibling,
                offset = this.attr.selection[
                    this.attr.selection.anchor === textNode ? 'anchorOffset' : 'focusOffset'
                ];

            // Split textnode into just the selection
            textNode.splitText(offset);
            textNode = this.node.previousSibling;

            // Remove textnode
            textNode.parentNode.removeChild(textNode);

            // Add new term node
            $('<span>')
                .text(this.attr.sign)
                .data('info', data.info)
                .addClass(data.cssClasses.join(' '))
                .insertBefore(this.$node);

            this.trigger(document, 'termCreated', data);
        };

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
                    self.highlightTerm(data);
                    _.defer(self.teardown.bind(self));
                }
            });
        };

        this.after('teardown', function() {
            this.$node.remove();
        });

        this.after('initialize', function() {
            var self = this,
                node = this.$node,
                mentionData = this.attr.mentionNode && this.attr.mentionNode.parents('.entity').data('info'),
                objectSign = mentionData && mentionData.objectRowKey && mentionData.objectRowKey.sign;

            node.html(dropdownTemplate({
                type: 'Set type of term',
                sign: this.attr.sign || this.attr.mentionNode.text(),
                objectSign: objectSign
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
