
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
		var syncMessage = {
			sync: {
				initiator : initiator
			},
			evt : evt,
			payload: payload
		};
		
		this._publishMessage("/messaging/pubsub/sync-" + this.getCurrentSync().sessionId, syncMessage, function (err, data) {
			if (err) {
				console.err("There was an error publishing a sync event! " + evt + " " + err);
			}
		});
	};
	
	SyncService.prototype.startSync = function (syncRequest, onmessage, onclose) {
		this.setCurrentSync(syncRequest);
		var syncChannelRequest = {
			url: "/messaging/pubsub/sync-" + syncRequest.sessionId,
			transport: "websocket",
			contentType: "text/html;charset=ISO-8859-1",
			onMessage: function (response) {
				var data = JSON.parse(response.responseBody);
				onmessage(null,data);
			},
			onError: function (response) {
				onmessage(response.error,null);
			}
		};
		this.getSocket().subscribe(syncChannelRequest);
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

			self._publishMessage("/messaging/pubsub/sync-" + self.getCurrentSync().sessionId, syncEndMessage, function (err, data) {
				if (err) {
					console.err("There was an error publishing the sync end event! " + evt + " " + err);
				}
			});
			self.removeCurrentSync();
			callback(err,response);
		});
	};
	
	SyncService.prototype.endSync = function (syncRequest) {
		this._unsubscribe("/messaging/pubsub/sync-" + syncRequest.sessionId);
	}

    return SyncService;
});

