
define(
[
    'service/serviceBase'
],
function(ServiceBase) {

    function StatementService() {
        ServiceBase.call(this);
        return this;
    }

    StatementService.prototype = Object.create(ServiceBase.prototype);

	StatementService.prototype.createStatement = function(createRequest, callback) {
		this._ajaxPost({
			url: 'statement/create',
			data: createRequest
		},function (err, response) {
			callback (err,response);
		});
	};

    StatementService.prototype.predicates = function(callback) {
        this._ajaxGet({
            url: 'predicate',
        }, function(err, response) {
            callback(err, response);
        });
    };

	
    return StatementService;
});

