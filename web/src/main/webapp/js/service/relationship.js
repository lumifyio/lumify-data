define(
    [
        'service/serviceBase'
    ],
    function (ServiceBase) {
        function RelationshipService() {
            ServiceBase.call(this);
            return this;
        }

        RelationshipService.prototype = Object.create(ServiceBase.prototype);

        RelationshipService.prototype.setProperty = function (propertyName, value, sourceId, destId, label, callback) {
            this._ajaxPost({
                url: 'relationship/property/set',
                data: {
                    propertyName: propertyName,
                    value: value,
                    source: sourceId,
                    dest: destId,
                    relationshipLabel: label
                }
            }, function (err, response) {
                callback(err, response);
            });
        };

        RelationshipService.prototype.createRelationship = function (createRequest, callback) {
            this._ajaxPost({
                url: 'relationship/create',
                data: createRequest
            }, function (err, response) {
                callback(err, response);
            });
        };

        return RelationshipService;
    });

