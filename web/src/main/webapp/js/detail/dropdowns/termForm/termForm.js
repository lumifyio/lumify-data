

define([
    'flight/lib/component',
    '../withDropdown',
    'tpl!./termForm',
    'tpl!./concept-options',
    'service/ucd',
    'service/entity',
    'underscore'
], function(defineComponent, withDropdown, dropdownTemplate, conceptsTemplate, Ucd, EntityService, _) {
    'use strict';

    return defineComponent(TermForm, withDropdown);


    function TermForm() {
        this.entityService = new EntityService();
        this.ucd = new Ucd();

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

            if (this.graphNodeId) {
                parameters.graphNodeId = this.graphNodeId;
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

        this.after('initialize', function() {
            var self = this,
                node = this.$node,
                mentionNode = $(this.attr.mentionNode),
                objectRowKeySpan = null,
                objectRowKey = null,
                graphNodeId = null;

            mentionNode
                .parents('.sentence').addClass('focused')
                .parents('.text').addClass('focus');

            // Find first parent (including self) with objectRowKey
            mentionNode.parents('.entity').addBack('.entity').toArray().reverse().forEach(function(obj) {
                var node = $(obj),
                    info = node.data('info');
                graphNodeId = info.graphNodeId;

                if (info && info.objectRowKey) {
                    objectRowKeySpan = node;
                    objectRowKey = info.objectRowKey;
                    return false;
                }
            });

            this.objectRowKeySpan = $(objectRowKeySpan);
            this.objectRowKey = objectRowKey;
            this.graphNodeId = graphNodeId;

            node.html(dropdownTemplate({
                type: 'Set type of term',
                sign: this.attr.sign || mentionNode.text(),
                objectSign: objectRowKey && objectRowKey.sign || '',
                finishMessage: (mentionNode.hasClass('term') ? 'Update Term' : 'Create Term')
            }));


            this.on('opened', function() {
                this.setupObjectTypeAhead();
                this.loadConcepts();
            });

            this.on('click', {
                entityConceptMenuSelector: this.onEntityConceptSelected,
                createTermButtonSelector: this.onCreateTermClicked
            });
        });


        this.loadConcepts = function() {
            var self = this;
            self.entityService.concepts(function(err, concepts) {
                var mentionNode = $(self.attr.mentionNode),
                    mentionNodeInfo = mentionNode.data('info');

                self.select('conceptSelector').html(conceptsTemplate({
                    concepts:concepts,
                    selectedConceptLabel:mentionNodeInfo && mentionNodeInfo.subType || ''
                }));
            });
        };

        this.setupObjectTypeAhead = function() {
            var self = this;

            self.select('objectSignSelector').typeahead({
                source: function(query, callback) {
                    self.ucd.entitySearch(query.toLowerCase(), function(err, entities) {
                        if(err) {
                            console.error('Error', err);
                            callback([]);
                            return self.trigger(document, 'error', { message: err.toString() });
                        }

                        // Convert dictionary map with type keys into flat
                        // array
                        var types = Object.keys(entities);
                        var entityArrays = types.map(function(type) { return entities[type]; });
                        var all = Array.prototype.concat.apply([], entityArrays);

                        callback(all.map(function(e) {
                            return e.sign;
                        }));
                    });
                    return;
                }
            });
        };
    }
});
