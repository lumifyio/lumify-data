define(
    [
        'service/serviceBase'
    ],
    function (ServiceBase) {

        function StatementService() {
            ServiceBase.call(this);
            return this;
        }

        StatementService.prototype = Object.create(ServiceBase.prototype);

        StatementService.prototype.createStatement = function (createRequest, callback) {
            this._ajaxPost({
                url: 'statement/create',
                data: createRequest
            }, function (err, response) {
                callback(err, response);
            });
        };

        StatementService.prototype.relationships = function (sourceConceptTypeId, destConceptTypeId, callback) {
            console.log('getting relationships (sourceConceptTypeId:', sourceConceptTypeId, ', destConceptTypeId:', destConceptTypeId, ')');
            this._ajaxGet({
                url: 'statement/relationship',
                data: {
                    sourceConceptTypeId: sourceConceptTypeId,
                    destConceptTypeId: destConceptTypeId
                }
            }, function (err, response) {
                callback(err, response);
            });
        };

        return StatementService;
    });

