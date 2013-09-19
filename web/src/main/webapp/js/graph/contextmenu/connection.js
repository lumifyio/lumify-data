
define([
    'util/retina',
    'service/relationship',
    'service/ontology',
    'tpl!./relationship-options',
    'tpl!./connection'
], function(retina, RelationshipService, OntologyService, relationshipTypeTemplate, connectionTemplate) {

    return Connection;

    function Connection() {

        if (!this.relationshipService) {
            this.relationshipService = new RelationshipService();
            this.ontologyService = new OntologyService();
        }

        this.getRelationshipLabels = function (source, dest) {
            var self = this;
            var sourceConceptTypeId = source.data('_subType');
            var destConceptTypeId = dest.data('_subType');
            self.ontologyService.conceptToConceptRelationships(sourceConceptTypeId, destConceptTypeId, function (err, results){
                if (err) {
                    console.error ('Error', err);
                    return self.trigger (document, 'error', { message: err.toString () });
                }

                self.displayRelationships (results.relationships);
            });
        };

        this.displayRelationships = function (relationships) {
            var self = this;
            self.ontologyService.relationships(function(err, ontologyRelationships) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                var relationshipsTpl = [];

                relationships.forEach(function(relationship) {
                    var ontologyRelationship = ontologyRelationships.byTitle[relationship.title];
                    var displayName;
                    if(ontologyRelationship) {
                        displayName = ontologyRelationship.displayName;
                    } else {
                        displayName = relationship.title;
                    }

                    var data = {
                        title: relationship.title,
                        displayName: displayName
                    };

                    relationshipsTpl.push(data);
                });

                $(".concept-label").html(relationshipTypeTemplate({ relationships: relationshipsTpl }));
            });
        };

        this.onContextMenuConnect = function() {
            var menu = this.select('vertexContextMenuSelector');
            var graphVertexId = menu.data('currentVertexGraphVertexId');

            this.creatingStatement = true;


            this.cy(function(cy) {
                var self = this,
                    sourceVertex = cy.getElementById(graphVertexId),
                    title = sourceVertex.data('originalTitle'),
                    beginText = 'Select item to connect to "' + title + '"',
                    instructions = $('<div>')
                        .text(beginText) 
                        .addClass('instructions')
                        .appendTo(this.$node),
                    edge = null,
                    input = null,
                    targetGraphId = null,

                    complete = function(val) {
                        cy.off(tapEvents);
                        cy.off(mouseEvents);
                        cy.panningEnabled(true)
                          .zoomingEnabled(true)
                          .boxSelectionEnabled(true);
                        if (input) {
                            input.remove();
                        }
                        instructions.remove();
                        self.creatingStatement = false;

                        if (val) {
                            edge.data('label', 'Saving...');

                            var parameters = {
                                sourceGraphVertexId: graphVertexId,
                                destGraphVertexId: targetGraphId,
                                predicateLabel: val
                            };

                            self.relationshipService.createRelationship(parameters).done(function(data) {
                                self.on(document, 'relationshipsLoaded', function loaded() {
                                    if (edge) {
                                        cy.remove(edge);
                                        edge = null;
                                    }
                                    self.off(document, 'relationshipsLoaded', loaded);
                                });
                                self.trigger(document, 'refreshRelationships');
                            });
                        } else {
                            if (edge) {
                                cy.remove(edge);
                                edge = null;
                            }
                        }
                    },

                    mouseEvents = {
                        mouseover: function(event) {
                            if (event.cy == event.cyTarget) return;
                            if (event.cyTarget.id() === graphVertexId) return;
                            if (!event.cyTarget.is('node')) return;


                            targetGraphId = event.cyTarget.id();
                            instructions.text('Click to connect "' + title + '" with "' + event.cyTarget.data('originalTitle') + '"');

                            edge = cy.add({
                              group: 'edges',
                              classes: 'temp',
                              data: {
                                  source: graphVertexId,
                                  target: targetGraphId
                              }
                            });
                        },
                        mouseout: function(event) {
                            if (edge && !edge.hasClass('label')) {
                                cy.remove(edge);
                                edge = null;
                            }
                            instructions.text(beginText);
                        }
                    },

                    tapEvents = {
                        tap: function(event) {
                            if (edge) {
                                if (edge.hasClass('label')) {
                                    complete();
                                } else {
                                    instructions.text('Select the relationship, then press [Enter]');
                                    cy.off(mouseEvents);

                                    var srcPosition = retina.pixelsToPoints(cy.getElementById(edge.data('source')).renderedPosition()),
                                        dstPosition = retina.pixelsToPoints(cy.getElementById(edge.data('target')).renderedPosition()),
                                        center = {
                                            left: (dstPosition.x - srcPosition.x) / 2 + srcPosition.x,
                                            top: (dstPosition.y - srcPosition.y) / 2 + srcPosition.y
                                        };

                                    cy.panningEnabled(false)
                                        .zoomingEnabled(false)
                                        .boxSelectionEnabled(false);

                                    input = $(connectionTemplate({})).appendTo('body')
                                        .css({
                                            left: (center.left - 50) + 'px',
                                            top: (center.top - 25) + 'px',
                                            width: '175px',
                                            position: 'absolute',
                                            zIndex: 100,
                                            textAlign: 'center'
                                        })
                                        .on({
                                            keydown: function(e) {
                                                var val = $.trim($(this).val());
                                                if (e.which === $.ui.keyCode.ENTER && val.length) {
                                                    complete(val);
                                                }
                                            }
                                        });
                                    _.defer(input.focus.bind(input));
                                    edge.addClass('label');
                                    self.getRelationshipLabels (cy.getElementById(edge.data('source')), cy.getElementById(edge.data('target')));
                                }
                            } else {
                                complete();
                            }
                        }
                    };

                cy.on(mouseEvents);
                cy.on(tapEvents);
            });
        };
    }
});
