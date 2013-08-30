
define([
    'flight/lib/component',
    '../withDropdown',
    'detail/artifact/artifact',
    'tpl!./entityForm',
    'tpl!detail/dropdowns/termForm/concept-options',
    'service/ucd',
    'service/entity',
    'service/ontology'
], function(defineComponent, withDropdown, artifact, template, options, Ucd, EntityService, OntologyService) {

    return defineComponent(EntityForm, withDropdown);

    function EntityForm() {
        this.ucd = new Ucd ();
        this.ontologyService = new OntologyService ();
        this.entityService = new EntityService ();

        this.defaultAttrs({
            entitySignSelector: '.entity-sign',
            conceptSelector: 'select',
            entityConceptMenuSelector: '.underneath .dropdown-menu a',
            createEntityButtonSelector: '.create-entity',
            buttonDivSelector: '.buttons',
            entityNameInputSelector: 'input',
            draggablesSelector: '.resolved'
        });

        this.after('initialize', function() {
            this.setupContent ();
            this.registerEvents ();
        });

        this.setupContent = function (){
            var self = this,
                vertex = this.$node,
                resolvedVertex = $(this.attr.resolvedVertex),
                entitySign = '',
                existingEntity = this.attr.existing ? resolvedVertex.hasClass('entity') : false;

            vertex.html(template({
                entitySign: '',
                buttonText: existingEntity ? 'Update' : 'Create'
            }));
        };

        this.registerEvents = function () {
            var self = this;

            this.on('opened', function () {
                this.setupObjectTypeAhead();
                this.loadConcepts();
            });

            this.on('click', {
                createEntityButtonSelector: this.onCreateEntityClicked
            });

            this.on('change', {
                conceptSelector: this.onConceptChanged
            });
        };

        this.setupObjectTypeAhead = function () {
            var self = this;

            self.select('entitySignSelector').typeahead({
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

        this.loadConcepts = function() {
            var self = this;
            self.allConcepts = [];
            self.ontologyService.concepts(function(err, concepts) {
                var resolvedVertex = $(self.attr.resolvedVertex),
                    resolvedVertexInfo = resolvedVertex.data('info');

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

        this.onCreateEntityClicked = function (event) {
            var self = this,
                $resolvedVertex = $(this.attr.resolvedVertex),
                newSign = $.trim(this.select('entitySignSelector').val()),
                parameters = {
                    sign: newSign,
                    conceptId: this.select('conceptSelector').val(),
                    graphVertexId: $resolvedVertex.graphVertexId,
                    artifactKey: this.attr.artifactData._rowKey,
                    artifactId: this.attr.artifactData.graphVertexId,
                    coordsX1: this.attr.coords.x1,
                    coordsX2: this.attr.coords.x2,
                    coordsY1: this.attr.coords.y1,
                    coordsY2: this.attr.coords.y2
                },
                $loading = $("<span>")
                    .addClass("badge")
                    .addClass("loading");

            this.select('createEntityButtonSelector').addClass('disabled');

            this.entityService.createEntity(parameters, function(err, data) {
                if (err) {
                    self.trigger(document, 'error', err);
                } else {
                    $('.detected-object-labels .label').addClass('entity resolved generic-draggable');
                    $('.detected-object-labels .label').data('info', data);
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