define([
    'flight/lib/component',
    '../withTypeContent',
    '../withHighlighting',
    'service/vertex',
    'service/ontology',
    'sf',
    'tpl!./multiple',
    'tpl!./histogram',
], function (defineComponent, withTypeContent, withHighlighting, VertexService, OntologyService, sf, template, histogramTemplate) {

    'use strict';

    return defineComponent(Multiple, withTypeContent, withHighlighting);

    function Multiple() {
        this.vertexService = new VertexService();
        this.ontologyService = new OntologyService();

        this.defaultAttrs({
            histogramSelector: '.multiple .histogram'
        });

        this.after('initialize', function () {
            var self = this;
            var vertices = this.attr.data
                .filter(function (v) {
                    return v._type != 'relationship';
                });
            this.$node.html(template({getClasses: this.classesForVertex, vertices: vertices}));
            this.updateEntityAndArtifactDraggables();

            var vertexIds = vertices.map(function (v) {
                return v.id;
            });
            this.vertexService.getMultiple(vertexIds, function (err, vertices) {
                if (err) {
                    return self.trigger(document, 'error', err);
                }

                self.ontologyService.concepts(function (err, concepts) {
                    if (err) {
                        return self.trigger(document, 'error', err);
                    }

                    self.ontologyService.properties(function (err, properties) {
                        if (err) {
                            return self.trigger(document, 'error', err);
                        }

                        var byProperty = calculateByProperty(vertices);
                        var data = {
                            vertices: vertices,
                            byProperty: byProperty,
                            concepts: concepts,
                            properties: properties,
                            sf: sf,
                            shouldDisplay: shouldDisplay,
                            getPropertyDisplayName: getPropertyDisplayName.bind(null, properties),
                            getPropertyValueDisplay: getPropertyValueDisplay.bind(null, concepts, properties),
                            sortPropertyValues: sortPropertyValues.bind(null, properties, byProperty)
                        };
                        console.log(data);
                        self.select('histogramSelector').html(histogramTemplate(data));
                    });
                });
            });

            function shouldDisplay(propertyName) {
                if (propertyName == '_type' || propertyName == '_subType') {
                    return true;
                } else if (propertyName[0] == '_') {
                    return false;
                }
                return true;
            }

            function sortPropertyValues(properties, byProperty, propertyName, a, b) {
                return genericCompare(byProperty[propertyName][a].length, byProperty[propertyName][b].length);
            }

            function genericCompare(a, b) {
                if (a == b) {
                    return 0;
                }
                return a > b ? -1 : 1;
            }

            function getPropertyDisplayName(properties, propertyName) {
                var propertyNameDisplay = propertyName;
                if (properties.byTitle[propertyNameDisplay]) {
                    propertyNameDisplay = properties.byTitle[propertyNameDisplay].displayName;
                }
                return propertyNameDisplay;
            }

            function getPropertyValueDisplay(concepts, properties, propertyName, propertyValue) {
                var propertyValueDisplay = propertyValue;
                if (propertyName == '_subType' && concepts.byId[propertyValue]) {
                    propertyValueDisplay = concepts.byId[propertyValue].title;
                } else if (properties.byTitle[propertyName]) {
                    switch (properties.byTitle[propertyName].dataType) {
                        case 'date':
                            propertyValueDisplay = sf("{0:yyyy/MM/dd}", new Date(propertyValue));
                            break;
                    }
                }
                return propertyValueDisplay;
            }

            function calculateByProperty(vertices) {
                var byProperty = {};
                vertices.forEach(function (v) {
                    Object.keys(v.properties).forEach(function (propertyName) {
                        if (!byProperty[propertyName]) {
                            byProperty[propertyName] = {};
                        }
                        appendBin(byProperty[propertyName], v.properties[propertyName], v);
                    });
                });
                return byProperty;
            }

            function appendBin(hash, binName, val) {
                if (!hash[binName]) {
                    hash[binName] = [];
                }
                hash[binName].push(val);
            }
        });
    }
});
