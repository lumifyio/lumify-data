define(
    [
        'service/serviceBase'
    ],
    function (ServiceBase) {
        function SyncService() {
            ServiceBase.call(this);
            return this;
        }

        SyncService.prototype = Object.create(ServiceBase.prototype);

        SyncService.prototype.publishSyncEvent = function (eventName, workspaceRowKey, eventData, callback) {
            var data = {
                type: 'sync',
                permissions: {
                    workspaces: [workspaceRowKey]
                },
                data: {
                    eventName: eventName,
                    eventData: eventData
                }
            };

            this.socketPush(data);
            if (callback) {
                return callback(null, messageData);
            }
            return null;
        };

        return SyncService;
    }
);

