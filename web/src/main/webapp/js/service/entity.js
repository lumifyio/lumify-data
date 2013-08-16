
define(
[
    'service/serviceBase'
],
function(ServiceBase) {
    function EntityService () {
        ServiceBase.call(this);
        return this;
    }

    EntityService.prototype = Object.create(ServiceBase.prototype);

	EntityService.prototype.createTerm = function(createRequest, callback) {
		this._ajaxPost({
			url: 'entity/create',
			data: createRequest
		},function (err, response) {
			callback (err,response);
		});
	};

    var cachedConcepts, cachedConceptMap;
    EntityService.prototype.concepts = function(callback) {
        if (cachedConcepts) {
            callback(null, cachedConcepts, cachedConceptMap);
        } else {
            this._ajaxGet({
                url: 'ontology/concept'
            }, function(err, response) {
                if (err) {
                    return callback(err);
                }

                cachedConcepts = response;
                cachedConceptMap = buildConceptMap(response, {})

                callback(null, cachedConcepts, cachedConceptMap);
            });
        }

        function buildConceptMap(concept, map) {
            map[concept.id] = concept;
            if(concept.children) {
                for(var i=0; i<concept.children.length; i++) {
                    buildConceptMap(concept.children[i], map);
                }
            }
            return map;
        }
    };

    return EntityService;
});

