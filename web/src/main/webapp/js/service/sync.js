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

        SyncService.prototype.publishWorkspaceSyncEvent = function (eventName, workspaceRowKey, eventData, callback) {
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
                return callback(null, data);
            }
            return null;
        };

        SyncService.prototype.publishUserSyncEvent = function (eventName, userRowKeys, eventData, callback) {
            var data = {
                type: 'sync',
                permissions: {
                    users: userRowKeys
                },
                data: {
                    eventName: eventName,
                    eventData: eventData
                }
            };

            this.socketPush(data);
            if (callback) {
                return callback(null, data);
            }
            return null;
        };

        return SyncService;
    }
);

