define(
    [
        'service/serviceBase'
    ],
    function (ServiceBase) {
        'use strict';

        function SyncService() {
            ServiceBase.call(this);
            return this;
        }

        SyncService.prototype = Object.create(ServiceBase.prototype);

        SyncService.prototype.publishWorkspaceSyncEvent = function (eventName, workspaceRowKey, eventData) {
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
            return null;
        };

        SyncService.prototype.publishUserSyncEvent = function (eventName, userRowKeys, eventData) {
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
            return null;
        };

        return SyncService;
    }
);

