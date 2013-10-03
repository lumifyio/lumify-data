define([
    'flight/lib/component',
    'data',
    'service/sync',
    './syncCursor',
    'util/withAsyncQueue'
], function (defineComponent, appData, SyncService, SyncCursor, withAsyncQueue) {
    'use strict';

    return defineComponent(Sync, withAsyncQueue);

    function Sync() {
        this.syncCursors = false; // TODO should we sync cursors? Maybe allow enabling/disabling.
        this.syncService = new SyncService();

        //PUT EVENTS YOU WANT TO SYNC HERE!
        this.events = [
            'addVertices',
            'updateVertices',
            'deleteVertices',
            'workspaceRemoteSave'
        ];

        if (this.syncCursors) {
            this.events.push('syncCursorMove');
            this.events.push('syncCursorFocus');
            this.events.push('syncCursorBlur');
        }

        this.after('initialize', function () {
            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on(document, 'socketMessage', this.onSocketMessage);

            this.on(document, 'onlineStatusChanged', this.onOnlineStatusChanged);
            this.on(document, 'verticesSelected', this.onVerticesSelected);

            for (var i in this.events) {
                this.on(document, this.events[i], this.onSyncedEvent);
            }
            if (this.syncCursors) {
                SyncCursor.attachTo(window);
            }

            this.setupAsyncQueue('users');
        });

        this.onWorkspaceLoaded = function(evt, workspace) {
            this.workspaceEditable = workspace.isEditable;
            this.currentWorkspaceRowKey = workspace.id;

            this.usersReady(function(usersData) {
                var user = usersData.user,
                    data = {
                        type: 'changedWorkspace',
                        permissions: {
                        },
                        data: {
                            userRowKey: user.rowKey,
                            workspaceRowKey: this.currentWorkspaceRowKey
                        }
                    };
                this.syncService.socketPush(data);
            });
        };

        this.onSocketMessage = function (evt, message) {
            message.data = message.data || {};
            message.data.eventData = message.data.eventData || {};
            switch (message.type) {
                case 'sync':
                    console.log('sync onSocketMessage (remote: ' + (message.data.eventData.remoteEvent ? 'true' : 'false') + ')', message);
                    message.data.eventData.remoteEvent = true;
                    this.trigger(document, message.data.eventName, message.data.eventData);
                    break;
            }
        };

        this.onSyncedEvent = function (evt, data) {
            if (!this.currentWorkspaceRowKey) {
                return;
            }
            if (!this.workspaceEditable && !data.remoteEvent) {
                return;
            }
            if (data.remoteEvent) {
                return;
            }

            console.log('onSyncedEvent', this.currentWorkspaceRowKey, evt.type, data);
            if (data && data.vertices) {
                data.vertices = data.vertices.map(function(vertex) {
                    return {
                        id: vertex.id,
                        workspace: vertex.workspace
                    };
                });
            }

            this.syncService.publishWorkspaceSyncEvent(evt.type, this.currentWorkspaceRowKey, data);
        };

        this.onOnlineStatusChanged = function (evt, data) {
            this.usersMarkReady(data);
            this.currentUser = data.user;
        };

        // This function is to support sync'ing between multiple devices with the same user
        this.onVerticesSelected = function (evt, data) {
            if (!this.currentUser) {
                return;
            }
            data = data || {};
            if (data.remoteEvent) {
                return;
            }
            this.syncService.publishUserSyncEvent(evt.type, [this.currentUser.rowKey], data);
        };
    }

});
