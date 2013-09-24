
define([
    'flight/lib/component',
    '../withDropdown',
    'detail/artifact/artifact',
    'tpl!./objectDetectionForm',
    'tpl!detail/dropdowns/termForm/concept-options',
    'service/ucd',
    'service/entity',
    'service/ontology'
], function(defineComponent, withDropdown, artifact, template, options, Ucd, EntityService, OntologyService) {
    'use strict';

    return defineComponent(ObjectDetectionForm, withDropdown);

    function ObjectDetectionForm() {
        this.ucd = new Ucd ();
        this.ontologyService = new OntologyService ();
        this.entityService = new EntityService ();

        this.defaultAttrs({
            entitySignSelector: '.entity-sign',
            conceptSelector: 'select',
            entityConceptMenuSelector: '.underneath .dropdown-menu a',
            resolveButtonSelector: '.resolve',
            buttonDivSelector: '.buttons',
            entityNameInputSelector: 'input',
            draggablesSelector: '.resolved',
            detectedObjectTagSelector: '.detected-object-labels .focused'
        });

        this.after('initialize', function() {
            this.setupContent ();
            this.registerEvents ();
        });

        this.onInputKeyUp = function (event) {
            switch (event.which) {
                case $.ui.keyCode.ENTER:
                    this.onResolveClicked(event);
            }
        }

        this.setupContent = function (){
            var self = this,
                vertex = this.$node,
                resolvedVertex = this.attr.resolvedVertex,
                entitySign = this.attr.resolvedVertex.title || '',
                existingEntity = this.attr.existing;

            vertex.html(template({
                entitySign: entitySign,
                buttonText: existingEntity ? 'Resolve to Existing' : 'Resolve to New'
            }));
        };

        this.registerEvents = function () {
            var self = this;

            this.on('opened', function () {
                this.setupObjectTypeAhead();
                this.loadConcepts();
            });

            this.on('click', {
                resolveButtonSelector: this.onResolveClicked
            });

            this.on('change', {
                conceptSelector: this.onConceptChanged
            });

            this.on('keyup', {
                entityNameInputSelector: this.onInputKeyUp
            });
        };

        this.setupObjectTypeAhead = function () {
            var self = this;

            self.select('entitySignSelector').typeahead({
                source: function(query, callback) {
                    self.ucd.entitySearch(query)
                        .done(function(entities) {
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

        this.loadConcepts = function() {
            var self = this;
            self.allConcepts = [];
            self.ontologyService.concepts()
                .done(function(concepts) {
                    var resolvedVertexInfo = self.attr.resolvedVertex;

                    self.allConcepts = concepts.byTitle;

                    self.select('conceptSelector').html(options({
                        concepts: self.allConcepts,
                        selectedConceptId: (self.attr.existing && resolvedVertexInfo && resolvedVertexInfo._subType) || ''
                    }));

                    if (self.select('conceptSelector').val() === '') {
                        self.select('createEntityButtonSelector').attr('disabled', true);
                    }
                });
        };

        this.onResolveClicked = function (event){
            var self = this,
                newSign = $.trim(this.select('entitySignSelector').val()),
                parameters = {
                    sign: newSign,
                    conceptId: this.select('conceptSelector').val(),
                    model: this.attr.model,
                    graphVertexId: this.attr.graphVertexId,
                    artifactKey: this.attr.artifactData._rowKey,
                    artifactId: this.attr.artifactData.graphVertexId,
                    x1: this.attr.coords.x1,
                    y1: this.attr.coords.y1,
                    x2: this.attr.coords.x2,
                    y2: this.attr.coords.y2,
                    detectedObjectRowKey: this.attr.detectedObjectRowKey
                };

            _.defer(this.buttonLoading.bind(this));

            if (this.attr.existing) {
                self.updateEntity (parameters);
            } else {
                self.createEntity (parameters);
            }

        }

        this.createEntity = function (parameters) {
            var self = this;

            this.entityService.resolveDetectedObject(parameters)
                .done(function(data) {

                    var resolvedVertex ={
                        graphVertexId: data.graphVertexId,
                        _rowKey: data._rowKey,
                        _subType: data._subType,
                        _type: data._type,
                        title: data.title,
                        info: data.info
                    };

                    // Temporarily creating a new tag to show on ui prior to backend update
                    var classes = $('.detected-object-labels .label').attr('class') + ' focused resolved entity';
                    var newTag = ' <a class="' + classes + '" href="#">' + data.title +' </a><button class="delete-tag" type="button">X</button>';
                    var added = false;

                if ($('.detected-object-labels .label').hasClass('focused')) {
                    self.updateEntityTag (data, parameters.conceptId);
                } else {
                    $('.detected-object-labels .label').each(function(){
                        if(parseFloat($(this).data("info").info.coords.x1) > data.info.coords.x1){
                            $(newTag).insertBefore(this).removePrefixedClasses('subType-').addClass('subType-' + parameters.conceptId).after(' ');
                            added = true;
                            return false;
                        }
                    });
                    if (!added){
                        $('.detected-object-labels').append ($(newTag));
                    }

                    $('.detected-object-labels .focused').data('info', data);
                    $('.detected-object-labels .focused').removeClass('focused');
                    self.trigger(document, 'termCreated', data);

                    var vertices = [];
                    vertices.push(resolvedVertex);
                    self.trigger(document, 'updateVertices', { vertices: vertices });
                    self.trigger(document, 'refreshRelationships');

                    if ($('.artifact').data('Jcrop')) {
                        $('.artifact').data('Jcrop').release ();
                    } else {
                        _.defer(self.teardown.bind(self));
                    }
                }
            });
        };

        this.updateEntity = function (parameters) {
            var self = this;
            this.entityService.updateDetectedObject(parameters).done(function(data) {
                self.updateEntityTag(data, parameters.conceptId);
            });
        };

        this.updateEntityTag = function (data, conceptId) {
            var self = this;
            var resolvedVertex = {
                graphVertexId: data.graphVertexId,
                _rowKey: data._rowKey,
                _subType: data._subType,
                _type: data._type,
                title: data.title,
                info: data.info
            };

            $('.detected-object-labels .focused').text(data.title).data('info', data).removePrefixedClasses('subType-');
            $('.detected-object-labels .focused').addClass('resolved entity subType-' + conceptId);

            if (!$('.detected-object-labels .focused').next().hasClass('delete-tag')){
                var buttonTag = '<button class="delete-tag" type="button">X</button>';
                $(buttonTag).insertAfter($('.detected-object-labels .focused'));
                self.trigger(document, 'termCreated', data);
            }
            $('.detected-object-labels .focused').removeClass('focused');

            var vertices = [];
            vertices.push(resolvedVertex);
            self.trigger(document, 'updateVertices', { vertices: vertices });

            if ($('.artifact').data('Jcrop')) {
                $('.artifact').data('Jcrop').release ();
            } else {
                _.defer(self.teardown.bind(self));
            }
        }

        this.onConceptChanged = function(event) {
            var select = $(event.target);

            this.updateConceptLabel(select.val());
        };

        this.updateConceptLabel = function(conceptId, vertex) {
            if (conceptId == '') {
                this.select('createEntityButtonSelector').attr('disabled', true);
                return;
            }
            this.select('createEntityButtonSelector').attr('disabled', false);

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
    }
});
