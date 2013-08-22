define(
    [
        'service/serviceBase'
    ],
    function (ServiceBase) {
        function VertexService() {
            ServiceBase.call(this);
            return this;
        }

        VertexService.prototype = Object.create(ServiceBase.prototype);

        VertexService.prototype.addProperty = function (vertexId, propertyName, value, callback) {
            this._ajaxPost({
                url: 'vertex/' + vertexId + '/property/add',
                data: {
                    propertyName: propertyName,
                    value: value
                }
            }, function (err, response) {
                callback(err, response);
            });
        };

        return VertexService;
    });

