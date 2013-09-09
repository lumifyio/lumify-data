
define([
    'flight/lib/component',
    './image/image',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./entity',
    'tpl!./properties',
    'tpl!./relationships',
    'detail/dropdowns/propertyForm/propForm',
    'service/ontology',
    'service/vertex',
    'sf'
], function(defineComponent, Image, withTypeContent, withHighlighting, template, propertiesTemplate, relationshipsTemplate, PropertyForm, OntologyService, VertexService, sf) {

    'use strict';

    var ontologyService = new OntologyService();
    var vertexService = new VertexService();

    return defineComponent(Entity, withTypeContent, withHighlighting);

    function Entity(withDropdown) {

        this.defaultAttrs({
            glyphIconSelector: '.entity-glyphIcon',
            propertiesSelector: '.properties',
            relationshipsSelector: '.relationships',
            addNewPropertiesSelector: '.add-new-properties',
            addPropertySelector: '.add-property'
        });

        this.after('teardown', function() {
            this.$node.off('click.paneClick');
        });

        this.after('initialize', function() {
            var self = this;
            this.$node.on('click.paneClick', this.onPaneClicked.bind(this));
            this.on('click', {
                addNewPropertiesSelector: this.onAddNewPropertiesClicked
            });
            this.on('addProperty', this.onAddProperty);
            this.on(document, 'socketMessage', this.onSocketMessage);

            this.handleCancelling(ontologyService.concepts(function(err, concepts) {
                if (err) {
                    return self.trigger(document, 'error', err);
                }

                var concept = concepts.byId[self.attr.data._subType];

                self.$node.html(template({
                    title: self.attr.data.originalTitle || self.attr.data.title || 'No title avaliable',
                    highlightButton: self.highlightButton(),
                    fullscreenButton: self.fullscreenButton([self.attr.data.id || self.attr.data.graphVertexId])
                }));

                Image.attachTo(self.select('glyphIconSelector'), {
                    data: self.attr.data,
                    service: self.entityService,
                    defaultIconSrc: concept && concept.glyphIconHref || ''
                });

                self.loadEntity();
            }));
        });

        this.onSocketMessage = function (evt, message) {
            var self = this;
            switch (message.type) {
                case 'propertiesChange':
                    for(var i=0; i<message.data.properties.length; i++) {
                        var propertyChangeData = message.data.properties[i];
                        self.onPropertyChange(propertyChangeData);
                    }
                    break;
            }
        };

        this.onPropertyChange = function(propertyChangeData) {
            if(propertyChangeData.graphVertexId != this.attr.data.graphVertexId) {
                return;
            }
            this.select('propertiesSelector')
                .find('.property-' + propertyChangeData.propertyName + ' .value')
                .html(propertyChangeData.value);
        };

        this.loadEntity = function() {
            var self = this;
            var vertexInfo = {
                id: this.attr.data.id || this.attr.data.graphVertexId,
                properties: {
                    title: this.attr.data.originalTitle || this.attr.data.title || 'No title avaliable',
                    graphVertexId: this.attr.data.graphVertexId,
                    _type: this.attr.data._type,
                    _subType: this.attr.data._subType,
                    _rowKey: this.attr.data._rowKey
                }
            };

            this.getProperties(this.attr.data.id || this.attr.data.graphVertexId, function(properties) {
                self.displayProperties (properties);
            });

            this.getRelationships(this.attr.data.id || this.attr.data.graphVertexId, function(relationships) {
                self.handleCancelling(self.ontologyService.relationships(function(err, ontologyRelationships) {
                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }

                    var relationshipsTplData = [];

                    relationships.forEach(function(relationship) {
                        var ontologyRelationship = ontologyRelationships.byTitle[relationship.relationship.label];
                        var displayName;
                        if(ontologyRelationship) {
                            displayName = ontologyRelationship.displayName;
                        } else {
                            displayName = relationship.relationship.label;
                        }

                        var data = {};
                        data.displayName = displayName;
                        data.relationship = relationship.relationship;
                        data.dataInfo = JSON.stringify({
                            source: relationship.relationship.sourceVertexId,
                            target: relationship.relationship.destVertexId,
                            _type: 'relationship',
                            relationshipType: relationship.relationship.label
                        });

                        relationship.vertex.properties.graphVertexId = relationship.vertex.id;

                        if(vertexInfo.id == relationship.relationship.sourceVertexId) {
                            data.sourceVertex = vertexInfo;
                            data.sourceVertex.cssClasses = self.classesForVertex(vertexInfo);

                            data.destVertex = relationship.vertex;
                            data.destVertex.cssClasses = self.classesForVertex(relationship.vertex);
                        } else {
                            data.sourceVertex = relationship.vertex;
                            data.sourceVertex.cssClasses = self.classesForVertex(relationship.vertex);

                            data.destVertex = vertexInfo;
                            data.destVertex.cssClasses = self.classesForVertex(vertexInfo);
                        }

                        relationshipsTplData.push(data);
                    });

                    var $rels = self.select('relationshipsSelector');
                    $rels.find('ul').html(relationshipsTemplate({relationships:relationshipsTplData}));
                    $rels.find('.loading').remove();
                }));
            });
        };

        this.displayProperties = function (properties){
            var self = this;
            this.handleCancelling( ontologyService.properties(function(err, ontologyProperties) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                var propertiesTpl = [];
                properties.forEach(function(property) {
                    var displayName, value,
                        ontologyProperty = ontologyProperties.byTitle[property.key];

                    if (ontologyProperty) {
                        displayName = ontologyProperty.displayName;
                        if(ontologyProperty.dataType == 'date') {
                            value = sf("{0:yyyy/MM/dd}", new Date(property.value));
                        } else {
                            value = property.value;
                        }
                    } else {
                        displayName = property.key;
                        value = property.value;
                    }

                    var data = {
                        key: property.key,
                        value: value,
                        displayName: displayName
                    };

                    if(/^[^_]/.test(property.key)) {
                        propertiesTpl.push(data);
                    }

                    if(property.key === '_glyphIcon') {
                        self.trigger(self.select('glyphIconSelector'), 'iconUpdated', { src: property.value });
                    }
                });

                var props = propertiesTemplate({properties:propertiesTpl});

                var $props = self.select('propertiesSelector');
                $props.find('ul').html(props);
                $props.find('.loading').remove();
            }));
        };

        this.getProperties = function(graphVertexId, callback) {
            var self = this;

            this.handleCancelling(this.ucdService.getVertexProperties(encodeURIComponent(graphVertexId), function(err, properties) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                callback(properties);
            }));
        };

        this.getRelationships = function(graphVertexId, callback) {
            var self = this;

            this.handleCancelling(this.ucdService.getVertexRelationships(encodeURIComponent(graphVertexId), function(err, data) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                return callback(data.relationships);
            }));
        };

        this.onPaneClicked = function(evt) {
            var $target = $(evt.target);

            if ($target.not('.add-new-properties') && $target.parents('.underneath').length === 0) {
                PropertyForm.teardownAll();
            }

            if ($target.is('.entity, .artifact, span.relationship')) {
                this.trigger('verticesSelected', $target.data('info'));
                evt.stopPropagation();
            }

        };

        this.onAddNewPropertiesClicked = function (evt){

            var root = $('<div class="underneath">').insertAfter(evt.target);

            PropertyForm.attachTo(root, {
                service: ontologyService,
                data: this.attr.data
            });
        };

        this.onAddProperty = function(event, data) {
            var self = this;

            vertexService.setProperty(
                    this.attr.data.id || this.attr.data.graphVertexId,
                    data.property.name,
                    data.property.value, 
                    function (err, properties){
                        if(err) {
                            if (err.xhr.status == 400){
                                console.error('Validation error');
                                self.trigger(self.$node.find('.underneath'), 'addPropertyError', {});
                                return;
                            }
                            console.error('Error', err);
                            return self.trigger(document, 'error', { message: err.toString() });
                        }

                        self.displayProperties (properties);
                    }
            );
        };
    }
});

