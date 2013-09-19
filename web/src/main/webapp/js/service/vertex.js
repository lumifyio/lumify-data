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

        VertexService.prototype.setProperty = function (vertexId, propertyName, value) {
            return this._ajaxPost({
                url: 'vertex/' + vertexId + '/property/set',
                data: {
                    propertyName: propertyName,
                    value: value
                }
            });
        };

        VertexService.prototype.getMultiple = function (vertexIds) {
            return this._ajaxGet({
                url: 'vertex/multiple',
                data: {
                    vertexIds: vertexIds
                }
            });
        };

        return VertexService;
    });

