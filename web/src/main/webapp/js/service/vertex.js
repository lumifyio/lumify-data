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

        VertexService.prototype.setProperty = function (vertexId, propertyName, value, callback) {
            return this._ajaxPost({
                url: 'vertex/' + vertexId + '/property/set',
                data: {
                    propertyName: propertyName,
                    value: value
                }
            }, function (err, response) {
                if (err) {
                    return callback(err);
                }
                return callback(null, response);
            });
        };

        VertexService.prototype.getMultiple = function (vertexIds, callback) {
            return this._ajaxGet({
                url: 'vertex/multiple',
                data: {
                    vertexIds: vertexIds
                }
            }, callback);
        };

        return VertexService;
    });

