define([
    'flight/lib/component',
    'service/sync',
    './syncCursor'
], function (defineComponent, SyncService, SyncCursor) {
    'use strict';

    return defineComponent(Sync);

    function Sync() {
        this.syncService = new SyncService();

        //PUT EVENTS YOU WANT TO SYNC HERE!
        this.events = [
            'search',
            'showSearchResults',
            'searchQueryChanged',
            'verticesAdded',
            'verticesUpdated',
            'verticesDeleted',
            'menubarToggleDisplay',
            'mapUpdateBoundingBox',
            'syncCursorMove',
            'syncCursorFocus',
            'syncCursorBlur'
        ];

        this.after('initialize', function () {
            console.log("A new sync was created for ", this.attr);
            this.me = this.attr.me;

            this.on(document, 'workspaceSwitched', this.onWorkspaceSwitched);
            this.on(document, 'socketMessage', this.onSocketMessage);

            for (var i in this.events) {
                this.on(document, this.events[i], this.onSyncedEvent);
            }
        });

        this.onWorkspaceSwitched = function (evt, data) {
            this.currentWorkspaceRowKey = data.workspace._rowKey;
        };

        this.onSocketMessage = function (evt, message) {
            switch (message.type) {
                case 'sync':
                    message.data.eventData.remoteEvent = true;
                    this.trigger(document, message.data.eventName, message.data.eventData);
                    break;
            }
        };

        this.onSyncedEvent = function (evt, data) {
            if (!this.currentWorkspaceRowKey) {
                return;
            }
            if (data.remoteEvent) {
                return;
            }
            this.syncService.publishSyncEvent(evt.type, this.currentWorkspaceRowKey, data);
        };
    }

});
