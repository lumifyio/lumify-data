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

        OntologyService.prototype.clearCaches = function() {
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

            if (!refresh && cachedConcepts) {
                return callback(null, cachedConcepts);
            }

            this._ajaxGet({
                url: 'ontology/concept'
            }, function (err, response) {
                if (err) {
                    return callback(err);
                }

                cachedConcepts = {
                    tree: response,
                    byId: buildConceptMapById(response, {}),
                    byTitle: flattenConcepts(response)
                };

                console.log('concepts', cachedConcepts);
                return callback(null, cachedConcepts);
            });

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
                    } else {
                        child.flattenedTitle = child.title;
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

            if (!refresh && cachedRelationships) {
                return callback(null, cachedRelationships);
            }

            this._ajaxGet({
                url: 'ontology/relationship'
            }, function (err, response) {
                if (err) {
                    return callback(err);
                }

                cachedRelationships = {
                    list: response.relationships,
                    byTitle: buildRelationshipsByTitle(response.relationships)
                };
                console.log('relationships', cachedRelationships);

                return callback(null, cachedRelationships);
            });

            function buildRelationshipsByTitle(relationships) {
                var result = {};
                relationships.forEach(function(relationship) {
                    result[relationship.title] = relationship;
                });
                return result;
            }
        };

        var cachedProperties;
        OntologyService.prototype.properties = function (refresh, callback) {
            if (typeof refresh === 'function') {
                callback = refresh;
                refresh = false;
            }

            if (!refresh && cachedProperties) {
                return callback(null, cachedProperties);
            }

            this._ajaxGet({
                url: 'ontology/property'
            }, function (err, response) {
                if (err) {
                    return callback(err);
                }

                cachedProperties = {
                    list: response.properties,
                    byTitle: buildPropertiesByTitle(response.properties)
                };
                console.log('properties', cachedProperties);

                return callback(null, cachedProperties);
            });
        };

        OntologyService.prototype.propertiesByConceptId = function (conceptId, callback) {
            this._ajaxGet({
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

        function buildPropertiesByTitle(properties) {
            var result = {};
            properties.forEach(function(property) {
                result[property.title] = property;
            });
            return result;
        }

        return OntologyService;
    });

