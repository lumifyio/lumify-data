

define([
    'flight/lib/component',
    '../withDropdown',
    'tpl!./termForm',
    'tpl!./concept-options',
    'service/ucd',
    'service/entity',
    'service/ontology',
    'underscore'
], function(defineComponent, withDropdown, dropdownTemplate, conceptsTemplate, Ucd, EntityService, OntologyService, _) {
    'use strict';

    return defineComponent(TermForm, withDropdown);


    function TermForm() {
        this.entityService = new EntityService();
        this.ontologyService = new OntologyService();
        this.ucd = new Ucd();

        this.defaultAttrs({
            entityConceptMenuSelector: '.underneath .dropdown-menu a',
            createTermButtonSelector: '.create-term',
            buttonDivSelector: '.buttons',
            termNameInputSelector: 'input',
            signSelector: '.sign',
            objectSignSelector: '.object-sign',
            conceptSelector: 'select'
        });

        this.after('teardown', function() {
            if (this.promoted && this.promoted.length) {
                this.demoteSpanToTextVertex(this.promoted);
            }

            var info = $(this.attr.mentionNode).removeClass('focused').data('info');
            if (info) {
                this.updateConceptLabel(info._subType);
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

        this.onInputKeyUp = function (event) {
            switch (event.which) {
                case $.ui.keyCode.ENTER:
                    this.onCreateTermClicked(event);
            }
        }

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
                },
                $loading = $("<span>")
                    .addClass("badge")
                    .addClass("loading");
            this.select('buttonDivSelector').prepend($loading);
            this.select('createTermButtonSelector').addClass('disabled');
            if ( !parameters.conceptId || parameters.conceptId.length === 0) {
                this.select('conceptSelector').focus();
                return;
            }

            if (newObjectSign.length) {
                parameters.objectSign = newObjectSign;
                $mentionNode.attr('title', newObjectSign);
            }

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

        this.updateConceptLabel = function(conceptId, vertex) {
            if (conceptId == '') {
                this.select('createTermButtonSelector').attr('disabled', true);
                return;
            }
            this.select('createTermButtonSelector').attr('disabled', false);

            if (this.allConcepts && this.allConcepts.length) {

                vertex = $(vertex || this.promoted || this.attr.mentionNode);
                var classPrefix = 'subType-',
                    labels = this.allConcepts.map(function(c) {
                        return classPrefix + c.id;
                    });

                vertex.removeClass(labels.join(' '))
                    .addClass(classPrefix + conceptId);
            }
        };

        this.setupContent = function() {
            var self = this,
                vertex = this.$node,
                mentionVertex = $(this.attr.mentionNode),
                sign = this.attr.sign || mentionVertex.text(),
                data = mentionVertex.data('info'),
                title = $.trim(data && data.title || ''),
                existingEntity = this.attr.existing ? mentionVertex.addClass('focused').hasClass('entity') : false,
                objectSign = '';

            this.graphVertexId = data && data.graphVertexId;

            if (this.attr.selection && !existingEntity) {
                this.trigger(document, 'ignoreSelectionChanges.detail');
                this.promoted = this.promoteSelectionToSpan();
                setTimeout(function() {
                    self.trigger(document, 'resumeSelectionChanges.detail');
                }, 10);
            }

            if (existingEntity && mentionVertex.hasClass('resolved')) {
                objectSign = title;
            }

            vertex.html(dropdownTemplate({
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

            this.on('keyup', {
                termNameInputSelector: this.onInputKeyUp
            })
        };

        this.loadConcepts = function() {
            var self = this;
            self.allConcepts = [];
            self.ontologyService.concepts(function(err, concepts) {
                var mentionVertex = $(self.attr.mentionNode),
                    mentionVertexInfo = mentionVertex.data('info');

                self.allConcepts = concepts.byTitle;
                
                self.select('conceptSelector').html(conceptsTemplate({
                    concepts: self.allConcepts,
                    selectedConceptId: (self.attr.existing && mentionVertexInfo && mentionVertexInfo._subType) || ''
                }));

                if (self.select('conceptSelector').val() === '') {
                    self.select('createTermButtonSelector').attr('disabled', true);
                }
            });
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
            var mentionVertex = $(this.attr.mentionNode),
                updatingEntity = this.attr.existing;

            if (updatingEntity) {

                this.updateConceptLabel(data.cssClasses.join(' '), mentionVertex);
                mentionVertex.data('info', data.info).removeClass('focused');

            } else if (this.promoted) {

                this.promoted.data('info', data.info)
                             .addClass(data.cssClasses.join(' '))
                             .removeClass('focused');
                this.promoted = null;
            }
        };

        this.promoteSelectionToSpan = function() {
            var textVertex = this.node,
                range = this.attr.selection.range,
                el,
                tempTextNode;


            var span = document.createElement('span');
            span.className = 'entity focused';

            var newRange = document.createRange();
            newRange.setStart(range.startContainer, range.startOffset);
            newRange.setEnd(range.endContainer, range.endOffset);

            // Special case where the start/end is inside an inner span
            // (surroundsContents will fail so expand the selection
            if (/entity/.test(range.startContainer.parentNode.className)) {
                el = range.startContainer.parentNode;
                var previous = el.previousSibling;

                if (previous && previous.nodeType === 3) {
                    newRange.setStart(previous, previous.textContent.length);
                } else {
                    tempTextNode = document.createTextNode('');
                    el.parentNode.insertBefore(tempTextNode, el);
                    newRange.setStart(tempTextNode, 0);
                }
            }
            if (/entity/.test(range.endContainer.parentNode.className)) {
                el = range.endContainer.parentNode;
                var next = el.nextSibling;

                if (next && next.nodeType === 3) {
                    newRange.setEnd(next, 0);
                } else {
                    tempTextNode = document.createTextNode('');
                    if (next) {
                        el.parentNode.insertBefore(tempTextNode, next);
                    } else {
                        el.appendChild(tempTextNode);
                    }
                    newRange.setEnd(tempTextNode, 0);
                }
            }
            newRange.surroundContents(span);

            return $(span).find('.entity').addClass('focused').end();
        };

        this.demoteSpanToTextVertex = function(vertex) {

            while (vertex[0].childNodes.length) {
                $(vertex[0].childNodes[0]).removeClass('focused');
                vertex[0].parentNode.insertBefore(vertex[0].childNodes[0], vertex[0]);
            }
            vertex.remove();
        };
    }
});
