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

        RelationshipService.prototype.setProperty = function (propertyName, value, sourceId, destId, label) {
            return this._ajaxPost({
                url: 'relationship/property/set',
                data: {
                    propertyName: propertyName,
                    value: value,
                    source: sourceId,
                    dest: destId,
                    relationshipLabel: label
                }
            });
        };

        RelationshipService.prototype.createRelationship = function (createRequest) {
            this._ajaxPost({
                url: 'relationship/create',
                data: createRequest
            });
        };

        return RelationshipService;
    });

