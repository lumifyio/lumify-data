

define([
    'flight/lib/component',
    '../withDropdown',
    '../../entity/withProperties',
    'tpl!./termForm',
    'tpl!./concept-options',
    'tpl!./entity',
    'service/ucd',
    'service/entity',
    'service/ontology',
    'underscore'
], function(defineComponent, withDropdown, withProperties, dropdownTemplate, conceptsTemplate, entityTemplate, Ucd, EntityService, OntologyService, _) {
    'use strict';

    return defineComponent(TermForm, withDropdown, withProperties);


    function TermForm() {
        this.entityService = new EntityService();
        this.ontologyService = new OntologyService();
        this.ucd = new Ucd();

        this.defaultAttrs({
            entityConceptMenuSelector: '.underneath .dropdown-menu a',
            createTermButtonSelector: '.create-term',
            buttonDivSelector: '.buttons',
            objectSignSelector: '.object-sign',
            graphVertexSelector: '.graphVertexId',
            conceptSelector: 'select',
            helpSelector: '.help'
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
            this.deferredConcepts = $.Deferred();
            this.setupContent();
            this.registerEvents();
        });

        this.showTypeahead = function() {
            this.select('objectSignSelector').typeahead('lookup');
        };

        this.onKeyPress = function(event) {
            if (!this.lastQuery || this.lastQuery === this.select('objectSignSelector').val()) {
                return;
            }

            if (!this.debouncedLookup) {
                this.debouncedLookup = _.debounce(function() {
                    this.select('objectSignSelector').typeahead('lookup');
                }.bind(this), 100);
            }

            this.debouncedLookup();
        };

        this.graphVertexChanged = function(newGraphVertexId, item, initial) {
            var self = this;

            this.currentGraphVertexId = newGraphVertexId;
            if (!initial || newGraphVertexId) {
                this.select('graphVertexSelector').val(newGraphVertexId);
                var info = _.isObject(item) ? item.properties || item : $(this.attr.mentionNode).data('info');
                this.updateConceptSelect(info && info._subType || '').show();
                this.select('createTermButtonSelector')
                    .text(newGraphVertexId && initial ? 'Update' : newGraphVertexId ? 'Resolve to Existing' : 'Resolve as New')
                    .show();
                this.select('helpSelector').hide();
            }

            if (newGraphVertexId) {
                this.ucd.getGraphVertexById(newGraphVertexId)
                    .done(this.updateResolveImageIcon.bind(this));
            } else this.updateResolveImageIcon();
        };

        this.updateConceptSelect = function(val) {
            var conceptSelect = this.select('conceptSelector').val(val).show();

            if (val) {
                this.select('createTermButtonSelector').removeAttr('disabled');
            } else {
                this.select('createTermButtonSelector').attr('disabled', true);
            }

            return conceptSelect;
        };

        this.onCreateTermClicked = function(event) {
            var self = this,
                $mentionNode = $(this.attr.mentionNode),
                newObjectSign = $.trim(this.select('objectSignSelector').val()),
                mentionStart,
                mentionEnd;

            if (this.attr.existing){
                var dataInfo = $mentionNode.data('info');
                mentionStart = dataInfo.start;
                mentionEnd = dataInfo.end;
            } else {
                mentionStart = this.selectedStart;
                mentionEnd = this.selectedEnd;
            }
            var parameters = {
                    sign: newObjectSign,
                    conceptId: this.select('conceptSelector').val(),
                    artifactKey: this.attr.artifactKey,
                    mentionStart: mentionStart,
                    mentionEnd: mentionEnd,
                    artifactId: this.attr.artifactId
                },
                $loading = $("<span>")
                    .addClass("badge")
                    .addClass("loading");

            if (this.currentGraphVertexId) {
                parameters.graphVertexId = this.currentGraphVertexId;
            }

            // TODO: extract button loading and disabling to withDropdown mixin
            this.select('buttonDivSelector').prepend($loading);
            this.select('createTermButtonSelector')
                .attr('disabled', true)
                .addClass('disabled');

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
                    console.error('createTerm', err);
                    return self.trigger(document, 'error', err);
                }
                self.highlightTerm(data);
                self.trigger(document, 'termCreated', data);

                var vertices = [];
                vertices.push(data.info);
                self.trigger(document, 'updateVertices', { vertices: vertices });

                _.defer(self.teardown.bind(self));
            });
        };

        this.onConceptChanged = function(event) {
            var select = $(event.target);
            
            this.updateConceptLabel(select.val());
        };

        this.updateConceptLabel = function(conceptId, vertex) {
            if (conceptId === '') {
                this.select('createTermButtonSelector').attr('disabled', true);
                return;
            }
            this.select('createTermButtonSelector').removeAttr('disabled');

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
                existingEntity = this.attr.existing ? mentionVertex.addClass('focused').hasClass('resolved') : false,
                objectSign = '';

            if (this.attr.selection && !existingEntity) {
                this.trigger(document, 'ignoreSelectionChanges.detail');
                this.promoted = this.promoteSelectionToSpan();

                // Promoted span might have been auto-expanded to avoid nested
                // spans
                sign = this.promoted.text();

                setTimeout(function() {
                    self.trigger(document, 'resumeSelectionChanges.detail');
                }, 10);
            }

            if (existingEntity && mentionVertex.hasClass('resolved')) {
                objectSign = title;
            }


            vertex.html(dropdownTemplate({
                sign: $.trim(sign),
                graphVertexId: data && data.graphVertexId,
                objectSign: $.trim(objectSign) || '',
                buttonText: existingEntity ? 'Update' : 'Resolve'
            }));

            this.graphVertexChanged(data && data.graphVertexId, data, true);

            this.runQuery(sign).done(function(vertices) {
                self.$node.find('.badge')
                    .attr('title', vertices.length + ' match' + (vertices.length === 1 ? '' : 'es') + ' found')
                    .text(vertices.length);
            });

            this.sign = sign;
            this.startSign = sign;
        };

        this.updateResolveImageIcon = function(vertex) {
            var self = this,
                info = $(self.attr.mentionNode).data('info');

            if (vertex) {
                $.extend(vertex, {}, vertex.properties);
            }

            if (!vertex && info) {
                self.deferredConcepts.done(function(allConcepts) {
                    var concept = self.conceptForSubType(info._subType, allConcepts);
                    if (concept) {
                        updateCss(concept.glyphIconHref);
                    }
                });
            } else if (!vertex) {
                updateCss();
            } else if (vertex._glyphIcon) {
                updateCss(vertex && vertex._glyphIcon);
            } else {
                self.deferredConcepts.done(function(allConcepts) {
                    var concept = self.conceptForSubType(vertex._subType, allConcepts);
                    if (concept) {
                        updateCss(concept.glyphIconHref);
                    }
                });
            }

            function updateCss(src) {
                var url = 'url("' + (src || "/img/glyphicons/glyphicons_194_circle_question_mark@2x.png")  + '")',
                    preview = self.$node.find('.resolve-wrapper .preview');
                
                if (preview.css('background-image') !== url) {
                    preview.css('background-image', url);
                }
            }
        };

        this.conceptForSubType = function(subType, allConcepts) {
            return _.findWhere(allConcepts, { id:subType });
        };

        this.registerEvents = function() {

            this.on('change', {
                conceptSelector: this.onConceptChanged
            });

            this.on('click', {
                entityConceptMenuSelector: this.onEntityConceptSelected,
                createTermButtonSelector: this.onCreateTermClicked,
                objectSignSelector: this.showTypeahead,
                helpSelector: function() { 
                    this.select('objectSignSelector').focus(); 
                    this.showTypeahead();
                }
            });

            this.on('keydown', {
                objectSignSelector: this.onKeyPress
            });

            this.on('opened', function() {
                this.loadConcepts()
                    .done(this.setupObjectTypeAhead.bind(this))
                    .done(function() { 
                        this.deferredConcepts.resolve(this.allConcepts);
                    }.bind(this));
            });
        };

        this.loadConcepts = function() {
            var self = this;
            self.allConcepts = [];
            return self.ontologyService.concepts(function(err, concepts) {
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


        this.runQuery = function(query) {
            return this.ucd.graphVertexSearch(query)
                .then(function(response) {
                    return _.filter(response.vertices, function(v) { return v.properties._type === 'entity'; });
                });
        };


        this.setupObjectTypeAhead = function() {
            var self = this,
                items = {},
                createNewText = 'Resolve as new entity';

            self.ontologyService.properties().done(function(ontologyProperties) {
                var field = self.select('objectSignSelector').typeahead({
                    source: function(query, callback) {
                        if (!self.sourceCache) self.sourceCache = {};
                        else if (self.sourceCache[query]) {
                            self.sourceCache[query](callback);
                            return;
                        }
                    
                        self.lastQuery = query;
                        var instance = this;

                        self.runQuery(query).done(function(entities) {
                            var all = _.map(entities, function(e) {
                                    return $.extend({
                                        toLowerCase: function() { return e.properties.title.toLowerCase(); },
                                        toString: function() { return e.id; },
                                        indexOf: function(s) { return e.properties.title.indexOf(s); }
                                    }, e);
                                });

                            items = _.groupBy(all, 'id');
                            items[createNewText] = [query];

                            self.sourceCache[query] = function(aCallback) {
                                var list = [createNewText].concat(all);
                                aCallback(list);

                                var selectedId = self.currentGraphVertexId;
                                if (selectedId) {
                                    var shouldSelect = instance.$menu.find('.gId-' + selectedId).closest('li');
                                    if (shouldSelect.length) {
                                        instance.$menu.find('.active').not(shouldSelect).removeClass('active');
                                        shouldSelect.addClass('active');
                                    }
                                }
                            };

                            self.sourceCache[query](callback);
                        });
                    },
                    matcher: function(item) {
                        if (item === createNewText) return true;
                        return Object.getPrototypeOf(this).matcher.apply(this, [item.properties.title]);
                    },
                    sorter: function(items) {
                        var sorted = Object.getPrototypeOf(this).sorter.apply(this, arguments),
                            index;

                        sorted.forEach(function(item, i) {
                            if (item === createNewText) {
                                index = i;
                                return false;
                            }
                        });

                        if (index) {
                            sorted.splice(0, 0, sorted.splice(index, 1)[0]);
                        }

                        return sorted;
                    },
                    updater: function(item) {
                        var matchingItem = items[item],
                            graphVertexId = '',
                            label = item;

                        if (matchingItem && matchingItem.length) {
                            graphVertexId = item;
                            label = matchingItem[0].properties ? matchingItem[0].properties.title : matchingItem[0];

                            if (graphVertexId == createNewText) {
                                graphVertexId = '';
                            } else {
                                self.sign = label;
                            }

                            matchingItem = matchingItem[0];
                        }

                        self.graphVertexChanged(graphVertexId, matchingItem);
                        return label;
                    },
                    highlighter: function(item) {

                        var html = (item === createNewText) ? 
                                item : Object.getPrototypeOf(this).highlighter.apply(this, [item.properties.title]),
                            icon = '',
                            concept = _.find(self.allConcepts, function(c) { 
                                    return item.properties && c.id === item.properties._subType; 
                            });

                        if (item.properties) {
                            icon = item._glyphIcon || item.properties._glyphIcon || concept.glyphIconHref;
                        }
                        return entityTemplate({
                            html: html,
                            item: item,
                            properties: item.properties && self.filterPropertiesForDisplay(item.properties, ontologyProperties),
                            iconSrc: icon,
                            concept: concept
                        });
                    }
                });

                var typeahead = field.data('typeahead'),
                    show = typeahead.show,
                    hide = typeahead.hide;

                typeahead.$menu.on('mousewheel DOMMouseScroll', function(e) {
                    var delta = e.wheelDelta || (e.originalEvent && e.originalEvent.wheelDelta) || -e.detail,
                        bottomOverflow = this.scrollTop + $(this).outerHeight() - this.scrollHeight >= 0,
                        topOverflow = this.scrollTop <= 0;

                    if ((delta < 0 && bottomOverflow) || (delta > 0 && topOverflow)) {
                        e.preventDefault();
                    }
                });

                typeahead.hide = function() {
                    hide.apply(typeahead);
                    typeahead.$menu.css('max-height', 'none');
                };

                typeahead.show = function() {
                    show.apply(typeahead);

                    if (~typeahead.$menu.css('max-height').indexOf('px')) {
                        typeahead.$menu.css('max-height', 'none');
                        _.defer(scrollToShow);
                        return;
                    } else {
                        scrollToShow();
                    }

                    function scrollToShow() {

                        var scrollParent = typeahead.$element.scrollParent(),
                            scrollTotalHeight = scrollParent[0].scrollHeight,
                            scrollTop = scrollParent.scrollTop(),
                            scrollHeight = scrollParent.outerHeight(true),
                            menuHeight = Math.min(scrollHeight - 100, typeahead.$menu.outerHeight(true)), 
                            menuMaxY = menuHeight + typeahead.$menu.offset().top,
                            bottomSpace = scrollHeight - menuMaxY,
                            padding = 10;

                        typeahead.$menu.css({
                            maxHeight: (menuHeight - padding) + 'px',
                            overflow: 'auto'
                        });

                        if (bottomSpace < 0) {
                            var scrollNeeded = scrollTop + Math.abs(bottomSpace) + padding;
                            if (scrollNeeded > scrollTotalHeight) {
                                // TODO: add padding or grow upwards?
                            }

                            scrollParent.animate({
                                scrollTop: scrollNeeded
                            });
                        }
                    }
                };
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

            var r = range.cloneRange();
            r.selectNodeContents($(".detail-pane .text").get(0));
            r.setEnd(range.startContainer, range.startOffset);
            var l = r.toString().length;

            this.selectedStart = l;
            this.selectedEnd = l + range.toString().length;

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
