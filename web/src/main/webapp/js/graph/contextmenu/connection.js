
define([
    'util/retina',
    'service/statement',
    'tpl!./relationship-options',
    'tpl!./connection'
], function(retina, StatementService, relationshipTypeTemplate, connectionTemplate) {

    return Connection;

    function Connection() {

        if (!this.statementService) {
            this.statementService = new StatementService();
        }

        this.getRelationshipLabels = function (source, dest) {
            var self = this;
            var sourceConceptTypeId = source.data('_subType');
            var destConceptTypeId = dest.data('_subType');
            self.statementService.relationships (sourceConceptTypeId, destConceptTypeId, function (err, results){
                if (err) {
                    console.error ('Error', err);
                    return self.trigger (document, 'error', { message: err.toString () });
                }

                console.log ('relationships results', results);
                $(".concept-label").html(relationshipTypeTemplate({
                    relationships: results.relationships || ''
                }));
            });
        };

        this.onContextMenuConnect = function() {
            var menu = this.select('nodeContextMenuSelector');
            var graphNodeId = menu.data('currentNodeGraphNodeId');

            this.creatingStatement = true;


            this.cy(function(cy) {
                var self = this,
                    sourceNode = cy.getElementById(graphNodeId),
                    title = sourceNode.data('originalTitle'),
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
                                sourceGraphNodeId: graphNodeId,
                                destGraphNodeId: targetGraphId,
                                predicateLabel: val
                            };

                            self.statementService.createStatement(parameters, function(err, data) {
                                if (err) {
                                    self.trigger(document, 'error', err);
                                } else {
                                    self.on(document, 'relationshipsLoaded', function loaded() {
                                        if (edge) {
                                            cy.remove(edge);
                                            edge = null;
                                        }
                                        self.off(document, 'relationshipsLoaded', loaded);
                                    });
                                    self.trigger(document, 'refreshRelationships');
                                }
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
                            if (event.cyTarget.id() === graphNodeId) return;
                            if (!event.cyTarget.is('node')) return;


                            targetGraphId = event.cyTarget.id();
                            instructions.text('Click to connect "' + title + '" with "' + event.cyTarget.data('originalTitle') + '"');

                            edge = cy.add({
                              group: 'edges',
                              classes: 'temp',
                              data: {
                                  source: graphNodeId,
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
                                            top: (center.top - 15) + 'px',
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
