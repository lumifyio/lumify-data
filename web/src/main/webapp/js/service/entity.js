
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

    EntityService.prototype.addProperty = function(vertexId, propertyName, value, callback) {
        this._ajaxPost({
            url: 'entity/property/add',
            data: {
                vertexId: vertexId,
                propertyName: propertyName,
                value: value
            }
        },function (err, response) {
            callback (err,response);
        });
    };

    return EntityService;
});

