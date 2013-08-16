

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

        this.after('teardown', function() {
            if (this.promoted && this.promoted.length) {
                this.demoteSpanToTextNode(this.promoted);
            }

            var info = $(this.attr.mentionNode).removeClass('focused').data('info');
            if (info) {
                this.updateConceptLabel('subType-' + info._subType);
            }
            
            // Remove extra textNodes
            if (this.node.parentNode) {
                this.node.parentNode.normalize();
            }
        });

        this.after('initialize', function() {
            this.setupContent();
            this.registerEvents();
        });

        this.onCreateTermClicked = function(event) {
            var self = this,
                $mentionNode = $(this.attr.mentionNode),
                sentence = this.$node.parents('.sentence'),
                sentenceInfo = sentence.data('info'),
                sign = this.select('signSelector').text(),
                newObjectSign = $.trim(this.select('objectSignSelector').val()),
                mentionStart = sentenceInfo.start + sentence.text().indexOf(sign),
                parameters = {
                    sign: sign,
                    conceptId: this.select('conceptSelector').val(),
                    artifactKey: this.attr.artifactKey,
                    mentionStart: mentionStart,
                    mentionEnd: mentionStart + sign.length
                };

            if ( !parameters.conceptId || parameters.conceptId.length === 0) {
                this.select('conceptSelector').focus();
                return;
            }

            if (newObjectSign.length) {
                parameters.objectSign = newObjectSign;
                $mentionNode.attr('title', newObjectSign);
            }

            $mentionNode.addClass('resolved');

            this.entityService.createTerm(parameters, function(err, data) {
                if (err) {
                    self.trigger(document, 'error', err);
                } else {
                    self.highlightTerm(data);
                    console.log(data);
                    self.trigger(document, 'termCreated', data);
                    _.defer(self.teardown.bind(self));
                }
            });
        };

        this.onConceptChanged = function(event) {
            var select = $(event.target);
            
            this.updateConceptLabel(select.val());
        };

        this.updateConceptLabel = function(conceptLabel) {
            if (this.allConcepts && this.allConcepts.length) {

                var node = $(this.promoted || this.attr.mentionNode),
                    labels = this.allConcepts.map(function(c) { 
                        return 'subType-' + c.id; 
                    });

                node.removeClass(labels.join(' '))
                    .addClass(conceptLabel);
            }
        };

        this.setupContent = function() {
            var self = this,
                node = this.$node,
                mentionNode = $(this.attr.mentionNode),
                sign = this.attr.sign || mentionNode.text(),
                data = mentionNode.data('info'),
                title = $.trim(data && data.title || ''),
                existingEntity = mentionNode.addClass('focused').hasClass('entity'),
                objectSign = '';

            this.graphNodeId = data && data.graphNodeId;

            if (this.attr.selection && !existingEntity) {
                this.promoted = this.promoteSelectionToSpan();
            }

            if (mentionNode.hasClass('resolved')) {
                objectSign = title;
            }

            node.html(dropdownTemplate({
                // Promoted span might have been auto-expanded to avoid nested
                // spans
                sign: this.promoted ? this.promoted.text() : sign,
                objectSign: objectSign || '',
                buttonText: existingEntity ? 'Update' : 'Create'
            }));
        };

        this.registerEvents = function() {

            this.on('opened', function() {
                this.setupObjectTypeAhead();
                this.loadConcepts();
            });

            this.on('change', {
                conceptSelector: this.onConceptChanged
            });

            this.on('click', {
                entityConceptMenuSelector: this.onEntityConceptSelected,
                createTermButtonSelector: this.onCreateTermClicked
            });
        };

        this.loadConcepts = function() {
            var self = this;
            self.allConcepts = [];
            self.entityService.concepts(function(err, rootConcept) {
                var mentionNode = $(self.attr.mentionNode),
                    mentionNodeInfo = mentionNode.data('info');

                self.allConcepts = self.flattenConcepts(rootConcept);
                self.select('conceptSelector').html(conceptsTemplate({
                    concepts: self.allConcepts,
                    selectedConceptId: mentionNodeInfo && mentionNodeInfo._subType || ''
                }));
            });
        };

        this.flattenConcepts = function(concept) {
            var childIdx, child, grandChildIdx;
            var flattenedConcepts = [];
            for(childIdx in concept.children) {
                child = concept.children[childIdx];
                if(concept.flattenedTitle) {
                    child.flattenedTitle = concept.flattenedTitle + "/" + child.title;
                } else {
                    child.flattenedTitle = child.title;
                }
                flattenedConcepts.push(child);
                var grandChildren = this.flattenConcepts(child);
                for(grandChildIdx in grandChildren) {
                    flattenedConcepts.push(grandChildren[grandChildIdx]);
                }
            }
            return flattenedConcepts;
        };

        this.setupObjectTypeAhead = function() {
            var self = this;

            self.select('objectSignSelector').typeahead({
                source: function(query, callback) {
                    self.ucd.entitySearch(query, function(err, entities) {
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
                            return e.properties.title;
                        }));
                    });
                    return;
                }
            });
        };

        this.highlightTerm = function(data) {
            var mentionNode = $(this.attr.mentionNode),
                updatingEntity = mentionNode.is('.entity');


            if (updatingEntity) {

                mentionNode.data('info', data.info)
                           .removeClass('subType-44')
                           .removeClass('subType-52')
                           .removeClass('subType-60')
                           .removeClass('subType-68')
                           .addClass(data.cssClasses.join(' '))
                           .removeClass('focused');

            } else if (this.promoted) {

                this.promoted.data('info', data.info)
                             .addClass(data.cssClasses.join(' '))
                             .removeClass('focused');
                this.promoted = null;
            }
        };

        this.promoteSelectionToSpan = function() {
            var textNode = this.node,
                range = this.attr.selection.range,
                el,
                tempTextNode;

            range.startContainer.splitText(range.startOffset);
            if (range.endOffset < range.endContainer.textContent.length) {
                range.endContainer.splitText(range.endOffset);
            }

            var span = document.createElement('span');
            span.className = 'entity focused';

            // Special case where the start/end is inside an inner span
            // (surroundsContents will fail so expand the selection
            if (/entity/.test(range.startContainer.parentNode.className)) {
                el = range.startContainer.parentNode;
                var previous = el.previousSibling;

                if (previous && previous.nodeType === 3) {
                    range.setStart(previous, previous.textContent.length);
                } else {
                    tempTextNode = document.createTextNode('');
                    el.parentNode.insertBefore(tempTextNode, el);
                    range.setStart(tempTextNode, 0);
                }
            }
            if (/entity/.test(range.endContainer.parentNode.className)) {
                el = range.endContainer.parentNode;
                var next = el.nextSibling;

                if (next && next.nodeType === 3) {
                    range.setEnd(next, 0);
                } else {
                    tempTextNode = document.createTextNode('');
                    if (next) {
                        el.parentNode.insertBefore(tempTextNode, next);
                    } else {
                        el.appendChild(tempTextNode);
                    }
                    range.setEnd(tempTextNode, 0);
                }
            }
            range.surroundContents(span);

            return $(span).find('.entity').addClass('focused').end();
        };

        this.demoteSpanToTextNode = function(node) {

            while (node[0].childNodes.length) {
                $(node[0].childNodes[0]).removeClass('focused');
                node[0].parentNode.insertBefore(node[0].childNodes[0], node[0]);
            }
            node.remove();
        };
    }
});
