define(
    [
        'service/serviceBase'
    ],
    function (ServiceBase) {

        function OntologyService() {
            ServiceBase.call(this);
            return this;
        }

        OntologyService.prototype = Object.create(ServiceBase.prototype);

        OntologyService.prototype.clearCaches = function () {
            cachedConcepts = null;
            cachedRelationships = null;
            cachedProperties = null;
        };

        var cachedConcepts;
        OntologyService.prototype.concepts = function (refresh, callback) {
            if (typeof refresh === 'function') {
                callback = refresh;
                refresh = false;
            }
            if (!callback) callback = function() { };

            if (!refresh && cachedConcepts) {
                cachedConcepts.done(function(v) {callback(null, v); });
                return cachedConcepts;
            }

            cachedConcepts = this._ajaxGet({
                url: 'ontology/concept'
            }).then(function(response) {
                var entityConcept = findConceptByTitle(response, 'entity');
                var artifactConcept = findConceptByTitle(response, 'artifact');
                return {
                    tree: response,
                    entityConcept: entityConcept,
                    artifactConcept: artifactConcept,
                    byId: buildConceptMapById(response, {}),
                    byTitle: flattenConcepts(entityConcept)
                };
            }).always(function(data) {callback(null, data); });

            return cachedConcepts;

            function findConceptByTitle(concept, title) {
                if (concept.title == title) {
                    return concept;
                }
                if (concept.children) {
                    for (var i = 0; i < concept.children.length; i++) {
                        var child = concept.children[i];
                        var c = findConceptByTitle(child, title);
                        if (c) {
                            return c;
                        }
                    }
                }
                return null;
            }

            function buildConceptMapById(concept, map) {
                map[concept.id] = concept;
                if (concept.children) {
                    for (var i = 0; i < concept.children.length; i++) {
                        buildConceptMapById(concept.children[i], map);
                    }
                }
                return map;
            }

            function flattenConcepts(concept) {
                var childIdx, child, grandChildIdx;
                var flattenedConcepts = [];
                for (childIdx in concept.children) {
                    child = concept.children[childIdx];
                    if (concept.flattenedTitle) {
                        child.flattenedTitle = concept.flattenedTitle + "/" + child.title;
                        child.flattenedDisplayName = concept.flattenedDisplayName + "/" + child.displayName;
                    } else {
                        child.flattenedTitle = child.title;
                        child.flattenedDisplayName = child.displayName;
                    }
                    flattenedConcepts.push(child);
                    var grandChildren = flattenConcepts(child);
                    for (grandChildIdx in grandChildren) {
                        flattenedConcepts.push(grandChildren[grandChildIdx]);
                    }
                }
                return flattenedConcepts;
            }
        };

        var cachedRelationships;
        OntologyService.prototype.relationships = function (refresh, callback) {
            if (typeof refresh === 'function') {
                callback = refresh;
                refresh = false;
            }
            if (!callback) callback = function() { };

            if (!refresh && cachedRelationships) {
                cachedRelationships.done(function(v) {callback(null, v); });
                return cachedRelationships;
            }

            cachedRelationships = this._ajaxGet({
                url: 'ontology/relationship'
            }).then(function(response) {
                return {
                    list: response.relationships,
                    byTitle: buildRelationshipsByTitle(response.relationships)
                };
            }).always(function(data) {callback(null, data); });

            return cachedRelationships;

            function buildRelationshipsByTitle(relationships) {
                var result = {};
                relationships.forEach(function (relationship) {
                    result[relationship.title] = relationship;
                });
                return result;
            }
        };

        OntologyService.prototype.conceptToConceptRelationships = function (sourceConceptTypeId, destConceptTypeId, callback) {
            console.log('getting relationships (sourceConceptTypeId:', sourceConceptTypeId, ', destConceptTypeId:', destConceptTypeId, ')');
            this._ajaxGet({
                url: 'statement/relationship',
                data: {
                    sourceConceptTypeId: sourceConceptTypeId,
                    destConceptTypeId: destConceptTypeId
                }
            }, function (err, response) {
                callback(err, response);
            });
        };

        var cachedProperties;
        OntologyService.prototype.properties = function (refresh, callback) {
            if (typeof refresh === 'function') {
                callback = refresh;
                refresh = false;
            }
            if (!callback) callback = function() { };

            if (!refresh && cachedProperties) {
                cachedProperties.done(function(v) {callback(null, v); });
                return cachedProperties;
            }

            cachedProperties = this._ajaxGet({
                url: 'ontology/property'
            }).then(function(response) {
                return {
                    list: response.properties,
                    byTitle: buildPropertiesByTitle(response.properties)
                };
            }).always(function(data) {callback(null, data); });

            return cachedProperties;
        };

        OntologyService.prototype.propertiesByConceptId = function (conceptId, callback) {
            return this._ajaxGet({
                url: 'ontology/concept/' + conceptId + '/properties'
            }, function (err, response) {
                if (err) {
                    return callback(err);
                }

                var props = {
                    list: response.properties,
                    byTitle: buildPropertiesByTitle(response.properties)
                };
                console.log('propertiesByConceptId', props);

                return callback(null, props);
            });
        };

        OntologyService.prototype.propertiesByRelationshipLabel = function (relationshipLabel, callback) {
            this._ajaxGet({
                url: 'ontology/' + relationshipLabel + '/properties'
            }, function (err, response) {
                if (err) {
                    return callback(err);
                }

                var props = {
                    list: response.properties,
                    byTitle: buildPropertiesByTitle(response.properties)
                };
                console.log('propertiesByRelationshipLabel', props);

                return callback(null, props);
            });
        };

        function buildPropertiesByTitle(properties) {
            var result = {};
            properties.forEach(function (property) {
                result[property.title] = property;
            });
            return result;
        }

        return OntologyService;
    });

