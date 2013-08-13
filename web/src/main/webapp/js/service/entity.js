
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

    var cachedConcepts;
    EntityService.prototype.concepts = function(callback) {
        if (cachedConcepts) {
            callback(null, cachedConcepts);
        } else {
            this._ajaxGet({
                url: 'ontology/concept'
            }, function(err, response) {
                if (!err) {
                    cachedConcepts = response;
                }
                callback(err, response);
            });
        }
    };
	
    return EntityService;
});

