
define(
[
    'service/serviceBase'
],
function(ServiceBase) {
    function SyncService () {
        ServiceBase.call(this);
        return this;
    }

    SyncService.prototype = Object.create(ServiceBase.prototype);

	SyncService.prototype.isCurrentlySynced = function() {
		return document.currentSync ? true : false;
	};
	
	SyncService.prototype.initiateSyncRequest = function (syncRequest, callback) {
		this._ajaxPost({
			url: 'messaging/sync',
			processData: false,
			contentType: 'application/json',
			data: JSON.stringify(syncRequest)
		},function (err, response) {
			callback (err,response);
		});
	};
	
	SyncService.prototype.acceptSyncRequest = function (syncRequest, callback) {
		this._ajaxGet({
			url: 'messaging/sync/' + syncRequest.sessionId + '/accept'
		},function (err, response) {
			callback (err,response);
		});
	};
	
	SyncService.prototype.rejectSyncRequest = function (syncRequest, callback) {
		this._ajaxGet({
			url: 'messaging/sync/' + syncRequest.sessionId + '/reject'
		},function (err, response) {
			callback (err,response);
		});
	};
	
	SyncService.prototype.removeCurrentSync = function () {
		document.currentSync = null;
	};
	
	SyncService.prototype.setCurrentSync = function (syncRequest) {
		document.currentSync = syncRequest;
	};
	
	SyncService.prototype.getCurrentSync = function () {
		return document.currentSync;
	};
	
	SyncService.prototype.publishSyncEvent = function (initiator, evt, payload) {
        if (payload && payload.syncToRemote === false) {
            return;
        }

		var syncMessage = {
			sync: {
				initiator : initiator
			},
			evt : evt,
			payload: payload
		};

        var currentSync = this.getCurrentSync();
        if (!currentSync) {
            console.warn('Sync not ready and message triggered, dropping: ', syncMessage);
        } else {
            currentSync.subSocket.push({data : JSON.stringify(syncMessage)});
        }
	};
	
	SyncService.prototype.startSync = function (syncRequest, onmessage, onclose) {
		var self = this;
		var syncChannelRequest = {
			url: "/messaging/pubsub/sync-" + syncRequest.sessionId,
			transport: "websocket",
			contentType: "text/html;charset=ISO-8859-1",
			maxReconnectOnClose: 0,
			onMessage: function (response) {
				var data = JSON.parse(response.responseBody);
				onmessage(null,data);
			},
			onError: function (response) {
				onmessage(response.error,null);
			},
			onClose: function (response) {
				self.removeCurrentSync();
				onclose(null,response);
			},
			onOpen: function (response) {
				self.setCurrentSync(syncRequest);
			}
		};
		syncRequest.subSocket = this.getSocket().subscribe(syncChannelRequest);
	};
	
	SyncService.prototype.initiateEndOfSync = function (initiator, syncRequest, callback) {
		var self = this;
		this._ajaxGet({
			url: 'messaging/sync/' + syncRequest.sessionId + '/end'
		},function (err, response) {
			var syncEndMessage = {
				sync: {
					initiator: initiator
				},
				syncEnd : true
			};

			self.getCurrentSync().subSocket.push({data : JSON.stringify(syncEndMessage)});
			self.removeCurrentSync();
			callback(err,response);
		});
	};
	
	SyncService.prototype.endSync = function (syncRequest) {
		this._unsubscribe("/messaging/pubsub/sync-" + syncRequest.sessionId);
	}

    return SyncService;
});

