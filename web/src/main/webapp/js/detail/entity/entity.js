
define([
    'flight/lib/component',
    './image/image',
    '../withProperties',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./entity',
    'tpl!./relationships',
    'detail/dropdowns/propertyForm/propForm',
    'service/ontology',
    'sf'
], function(defineComponent, Image, withProperties, withTypeContent, withHighlighting, template, relationshipsTemplate, PropertyForm, OntologyService, sf) {

    'use strict';

    var ontologyService = new OntologyService();

    return defineComponent(Entity, withTypeContent, withHighlighting, withProperties);

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

            this.handleCancelling(ontologyService.concepts(function(err, concepts) {
                if (err) {
                    console.error('handleCancelling', err);
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

        this.loadEntity = function() {
            var self = this;
            var vertexInfo = {
                id: this.attr.data.id || this.attr.data.graphVertexId,
                properties: {
                    title: this.attr.data.originalTitle || this.attr.data.title || 'No title available',
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

        this.getProperties = function(graphVertexId, callback) {
            var self = this;

            this.handleCancelling(this.ucdService.getVertexProperties(encodeURIComponent(graphVertexId), function(err, data) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                callback(data.properties);
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
    }
});

