
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

	EntityService.prototype.createEntity = function(createRequest, callback) {
		this._ajaxPost({
			url: 'entity/create',
			contentType: 'application/json',
			data: JSON.stringify(createRequest)
		},function (err, response) {
			callback (err,response);
		});
	};
	
    return EntityService;
});

