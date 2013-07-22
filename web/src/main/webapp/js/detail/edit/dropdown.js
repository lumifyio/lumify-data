

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
            var mentionNode = $(this.attr.mentionNode),
                termExists = mentionNode.is('.entity'),
                associatedObjectExists = this.objectRowKeySpan.length,
                associatedObject = data.info.objectRowKey;

            if ( associatedObjectExists ) {

                this.objectRowKeySpan.data('info', data.info);

            } else if ( termExists && associatedObject ) {

                mentionNode.wrap( $('<span>')
                    .data('info', data.info)
                    .addClass(data.cssClasses.join(' ')));

            } else if ( termExists ) {

                mentionNode.data('info', data.info);

            } else {

                // Must be a new term (possibly with an associated object)
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
            }
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

            if ( !parameters.conceptLabel || parameters.conceptLabel.length === 0) {
                this.select('conceptSelector').focus();
                return;
            }

            if (this.objectRowKey) {
                parameters.objectRowKey = this.objectRowKey.value;
            }

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
                mentionNode = $(this.attr.mentionNode),
                objectRowKeySpan = null,
                objectRowKey = null;

            // Find first parent (including self) with objectRowKey
            mentionNode.parents('.entity').addBack('.entity').toArray().reverse().forEach(function(obj) {
                var node = $(obj),
                    info = node.data('info');

                console.log('checking', obj, info);
                if (info && info.objectRowKey) {
                    objectRowKeySpan = node;
                    objectRowKey = info.objectRowKey;
                    return false;
                }
            });

            this.objectRowKeySpan = $(objectRowKeySpan);
            this.objectRowKey = objectRowKey;

            node.html(dropdownTemplate({
                type: 'Set type of term',
                sign: this.attr.sign || mentionNode.text(),
                objectSign: objectRowKey && objectRowKey.sign || ''
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
                    var mentionNodeInfo = mentionNode.data('info');

                    self.select('conceptSelector').html(conceptsTemplate({
                        concepts:concepts,
                        selectedConceptLabel:mentionNodeInfo && mentionNodeInfo.subType || ''
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
