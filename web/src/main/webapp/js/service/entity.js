
define(
[
    'service/serviceBase'
],
function(ServiceBase) {
    'use strict';

    function EntityService () {
        ServiceBase.call(this);
        return this;
    }

    EntityService.prototype = Object.create(ServiceBase.prototype);

	EntityService.prototype.createTerm = function(createRequest) {
		return this._ajaxPost({
			url: 'entity/createTerm',
			data: createRequest
		});
	};

	EntityService.prototype.createEntity = function(createRequest) {
        return this._ajaxPost({
            url: 'entity/createEntity',
            data: createRequest
        });
    };

    return EntityService;
});

