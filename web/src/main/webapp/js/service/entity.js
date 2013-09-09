
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
			url: 'entity/createTerm',
			data: createRequest
		},function (err, response) {
			callback (err,response);
		});
	};

	EntityService.prototype.createEntity = function(createRequest, callback) {
        this._ajaxPost({
            url: 'entity/createEntity',
            data: createRequest
        },function (err, response) {
            callback (err,response);
        });
    };

    return EntityService;
});

