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

        GraphService.prototype.findPath = function (data, callback) {
            this._ajaxGet({
                url: 'graph/findPath',
                data: data
            }, function (err, response) {
                callback(err, response);
            });
        };

        return GraphService;
    });

