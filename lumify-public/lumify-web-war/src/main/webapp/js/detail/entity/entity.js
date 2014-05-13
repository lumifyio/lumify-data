
define([
    'flight/lib/component',
    'data',
    './image/image',
    '../properties/properties',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./entity',
    'tpl!./relationships',
    'tpl!util/alert',
    'util/vertex/list',
    'util/vertex/formatters',
    'detail/dropdowns/propertyForm/propForm',
    'service/ontology',
    'service/vertex',
    'service/config',
    'sf'
], function(defineComponent,
    appData,
    Image,
    Properties,
    withTypeContent,
    withHighlighting,
    template,
    relationshipsTemplate,
    alertTemplate,
    VertexList,
    F,
    PropertyForm,
    OntologyService,
    VertexService,
    ConfigService,
    sf) {
    'use strict';

    var ontologyService = new OntologyService(),
        vertexService = new VertexService(),
        configService = new ConfigService();

    return defineComponent(Entity, withTypeContent, withHighlighting);

    function defaultSort(x,y) {
        return x === y ? 0 : x < y ? -1 : 1;
    }

    function Entity(withDropdown) {

        this.defaultAttrs({
            glyphIconSelector: '.entity-glyphIcon',
            propertiesSelector: '.properties',
            relationshipsSelector: '.relationships',
            titleSelector: '.entity-title'
        });

        this.after('teardown', function() {
            this.$node.off('click.paneClick');
        });

        this.after('initialize', function() {
            var self = this;
            this.$node.on('click.paneClick', this.onPaneClicked.bind(this));

            this.on(document, 'verticesUpdated', this.onVerticesUpdated);
            this.on('infiniteScrollRequest', this.handleReferenceLoadingRequest);

            self.loadEntity();
        });

        this.onVerticesUpdated = function(event, data) {
            var self = this;

            data.vertices.forEach(function(vertex) {
                if (vertex.id === self.attr.data.id) {
                    self.select('titleSelector').html(F.vertex.title(vertex));
                }
            });
        };

        this.loadEntity = function() {
            var self = this;

            this.handleCancelling(appData.refresh(this.attr.data))
                .done(function(vertex) {

                    self.$node.html(template({
                        vertex: vertex,
                        fullscreenButton: self.fullscreenButton([vertex.id]),
                        auditsButton: self.auditsButton(),
                        F: F
                    }));

                    Image.attachTo(self.select('glyphIconSelector'), {
                        data: vertex,
                        service: vertexService
                    });

                   Properties.attachTo(self.select('propertiesSelector'), {
                       data: vertex
                   });

                   self.updateEntityAndArtifactDraggables();

                    $.when(
                            self.handleCancelling(ontologyService.relationships()),
                            self.handleCancelling(vertexService.getVertexRelationships(vertex.id))
                        )
                        .fail(function() {
                            self.select('relationshipsSelector').html(alertTemplate({
                                error: 'Unable to load relationships'
                            }));
                        })
                        .done(self.loadRelationships.bind(self, vertex));
                });
        };

        this.loadRelationships = function(vertex, ontologyRelationships, vertexRelationships) {
            var self = this,
                totalReferences = vertexRelationships[0].totalReferences,
                allRelationships = vertexRelationships[0].relationships,
                relationships = [];

            configService.getProperties().done(function(config) {
                // Create source/dest/other properties
                allRelationships.forEach(function (r) {
                    if (ontologyRelationships.byTitle[r.relationship.label]) {
                        r.displayLabel = ontologyRelationships.byTitle[r.relationship.label].displayName;
                        var src, dest, other;
                        if (vertex.id == r.relationship.sourceVertexId) {
                            src = vertex;
                            dest = other = r.vertex;
                        } else {
                            src = other = r.vertex;
                            dest = vertex;
                        }

                        r.vertices = {
                            src: src,
                            dest: dest,
                            other: other,
                            classes: {
                                src: self.classesForVertex(src),
                                dest: self.classesForVertex(dest),
                                other: self.classesForVertex(other)
                            }
                        };

                        r.relationshipInfo = {
                            id: r.relationship.id,
                            properties: $.extend({}, r.relationship.properties, {
                                'http://lumify.io#conceptType': 'relationship',
                                id: r.relationship.id,
                                relationshipType: r.relationship.label,
                                source: r.relationship.sourceVertexId,
                                target: r.relationship.destVertexId
                            })
                        };
                        relationships.push(r);
                    }
                });

                var groupedByType = _.groupBy(relationships, function(r) {

                        // Has Entity are collected into references (no matter
                        // relationship direction
                        if (r.relationship.label === config['ontology.iri.artifactHasEntity']) {
                            return 'references';
                        }

                        return r.displayLabel;
                    }),
                    sortedKeys = Object.keys(groupedByType);

                sortedKeys.forEach(function (section) {
                    if (section !== 'references') {
                        groupedByType[section].sort(function (a, b) {
                            var direction = defaultSort(
                                    a.vertex.id === a.vertices.src.id ? 0 : 1,
                                    b.vertex.id === b.vertices.src.id ? 0 : 1
                            )
                            if (direction === 0) {
                                return defaultSort(
                                    F.vertex.title(a.vertex).toLowerCase(),
                                    F.vertex.title(b.vertex).toLowerCase()
                                );
                            } else {
                                return direction;
                            }
                        });
                    }
                });

                sortedKeys.sort(function(a,b) {
                    // If in references group sort by the title
                    if (a === b && a === 'references') {
                        return defaultSort(
                            F.vertex.title(a.vertex),
                            F.vertex.title(b.vertex)
                        );
                    }

                    // Specifies the special group sort order
                    var groups = { references: 1 };
                    if (groups[a] && groups[b]) {
                        return defaultSort(groups[a], groups[b]);
                    } else if (groups[a]) {
                        return 1;
                    } else if (groups[b]) {
                        return -1;
                    }

                    return defaultSort(a, b);
                });

                var $rels = self.select('relationshipsSelector');
                $rels.html(relationshipsTemplate({
                    relationshipsGroupedByType: groupedByType,
                    sortedKeys: sortedKeys,
                    F: F
                }));

                VertexList.attachTo($rels.find('.references'), {
                    vertices: _.map(groupedByType.references, function(r) {
                        return r.vertices.other;
                    }),
                    infiniteScrolling: (groupedByType.references && groupedByType.references.length) > 0,
                    total: totalReferences
                });
            });
        };

        this.handleReferenceLoadingRequest = function(evt, data) {
            var self = this;

            this.handleCancelling(vertexService.getVertexRelationships(this.attr.data.id, data.paging))
                .done(function(response) {
                    var relationships = response.relationships,
                        total = response.totalReferences;

                    self.trigger(
                        self.select('relationshipsSelector').find('.references'),
                        'addInfiniteVertices',
                        {
                            vertices: _.pluck(relationships, 'vertex'),
                            total: total
                        }
                    );

                });
        };

        this.onPaneClicked = function(evt) {
            var $target = $(evt.target);

            if (!$target.is('.add-new-properties,button') &&
                $target.parents('.underneath').length === 0) {
                PropertyForm.teardownAll();
            }

            if ($target.is('.entity, .artifact')) {
                var id = $target.data('vertexId');
                this.trigger('selectObjects', { vertices: [appData.vertex(id)] });
                evt.stopPropagation();
            } else if ($target.is('.relationship')) {
                var info = $target.data('info');
                if (info) {
                    this.trigger('selectObjects', { vertices: [info] });
                }
                evt.stopPropagation();
            }
        };

    }
});
