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

        var cachedConcepts;
        OntologyService.prototype.concepts = function(callback) {
            if (cachedConcepts) {
                return callback(null, cachedConcepts);
            } else {
                this._ajaxGet({
                    url: 'ontology/concept'
                }, function(err, response) {
                    if (err) {
                        return callback(err);
                    }

                    cachedConcepts = {
                        tree: response,
                        byId: buildConceptMapById(response, {}),
                        byTitle: flattenConcepts(response)
                    };

                    return callback(null, cachedConcepts);
                });
            }

            function buildConceptMapById(concept, map) {
                map[concept.id] = concept;
                if(concept.children) {
                    for(var i=0; i<concept.children.length; i++) {
                        buildConceptMapById(concept.children[i], map);
                    }
                }
                return map;
            }

            function flattenConcepts(concept) {
                var childIdx, child, grandChildIdx;
                var flattenedConcepts = [];
                for(childIdx in concept.children) {
                    child = concept.children[childIdx];
                    if(concept.flattenedTitle) {
                        child.flattenedTitle = concept.flattenedTitle + "/" + child.title;
                    } else {
                        child.flattenedTitle = child.title;
                    }
                    flattenedConcepts.push(child);
                    var grandChildren = flattenConcepts(child);
                    for(grandChildIdx in grandChildren) {
                        flattenedConcepts.push(grandChildren[grandChildIdx]);
                    }
                }
                return flattenedConcepts;
            }
        };

        return OntologyService;
    });

