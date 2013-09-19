define(
    [
        'service/serviceBase'
    ],
    function (ServiceBase) {

        function GraphService() {
            ServiceBase.call(this);
            return this;
        }

        GraphService.prototype = Object.create(ServiceBase.prototype);

        GraphService.prototype.findPath = function(data) {
            return this._ajaxGet({
                url: 'graph/findPath',
                data: data
            });
        };

        return GraphService;
    });

