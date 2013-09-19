
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

	EntityService.prototype.updateTerm = function(updateRequest, callback) {
        this._ajaxPost({
            url: 'entity/updateTerm',
            data: updateRequest
        },function (err, response) {
            callback (err,response);
        });
    };

	EntityService.prototype.resolveDetectedObject = function(createRequest, callback) {
        this._ajaxPost({
            url: 'entity/createResolvedDetectedObject',
            data: createRequest
        },function (err, response) {
            callback (err,response);
        });
    };

    EntityService.prototype.updateDetectedObject = function(updateRequest, callback) {
        this._ajaxPost({
            url: 'entity/updateResolvedDetectedObject',
            data: updateRequest
        },function (err, response) {
            callback (err,response);
        });
    };

    EntityService.prototype.deleteDetectedObject = function(deleteRequest, callback) {
        this._ajaxPost({
            url: 'entity/deleteResolvedDetectedObject',
            data: deleteRequest
        },function (err, response) {
            callback (err,response);
        });
    };

    return EntityService;
});

