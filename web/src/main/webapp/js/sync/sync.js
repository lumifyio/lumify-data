define([
    'flight/lib/component',
    'service/sync',
	'service/chat',
	'tpl!./syncButton',
	'tpl!./syncRequest'
], function(defineComponent, SyncService, ChatService, syncButtonTemplate, syncRequestTemplate) {
    'use strict';

    return defineComponent(Sync);

	function Sync () {
		this.syncService = new SyncService();
		this.chatService = new ChatService();
		this.currentSyncRequest = null;
		
		//PUT EVENTS YOU WANT TO SYNC HERE!
		this.events = [
			'search',
			'showSearchResults'
		];
		
		this.defaultAttrs({
			syncButtonSelector: '.btn-sync',
			syncAcceptButtonSelector: '.btn-sync-accept',
			syncRejectButtonSelector: '.btn-sync-reject'
		});
		
		this.after('initialize',function () {
			console.log("A new sync was created for " + this.attr.chatUser);
			this.$node.html($(syncButtonTemplate({})));
			this.on('syncEnded',this.onSyncEnd);
			
			this.on(document,'incomingSyncRequest',this.onIncomingSyncRequest);
			this.on(document,'incomingSyncAccept',this.onIncomingSyncAccept);
			this.on(document,'incomingSyncReject',this.onIncomingSyncReject);
			if (this.syncService.isCurrentlySynced()) {
				this.select('syncButtonSelector').addClass('disabled');
			}
            this.on('click', {
				syncButtonSelector: this.onSyncButtonClick,
				syncAcceptButtonSelector: this.onSyncAcceptClick,
				syncRejectButtonSelector: this.onSyncRejectClick
			});
			
			for (var i in this.events) {
				this.on(document,this.events[i],this.onSyncedEvent);
			}
		});
		
		
		//Local User Actions
		this.onSyncButtonClick = function (evt, data) {
			var self = this;
			var syncButton = this.select('syncButtonSelector');
			if (syncButton.hasClass('disabled')) {
				return;
			}
			
			if (this.currentSyncRequest && syncButton.hasClass('btn-sync-end')) {
				this.syncService.initiateEndOfSync(this.attr.me, this.currentSyncRequest,function (err, data) {
					if (err) {
						console.error ('There was an error attempting to close this sync session! ' + err);
					}
					self.syncService.endSync(self.currentSyncRequest);
					self.trigger('syncEnded',{});
				});
			} else {
				this._initiateRequest();
				$('.active-chat').find('.btn-sync').addClass('disabled');
				syncButton.addClass('btn-sync-requested').text('Sync Requested');
			}
		};
		
		this.onSyncAcceptClick = function (evt, data) {
			var self = this;
			this.syncService.acceptSyncRequest(this.currentSyncRequest,function (err, response) {
				$('.active-chat').find('.btn-sync').addClass('disabled');
				self._setEndButton();
				response.from = self.attr.me;
				self.chatService.acceptSyncRequest(response,function (err, callback) {
					if (err) {
						console.error('Error accepting the sync request!: ' + err);
					}
				});
				self.syncService.startSync(self.currentSyncRequest, self.onSyncMessage.bind(self), function (err, data) {
					self.trigger("syncEnded",{});
				}.bind(self));
			});
		};
		
		this.onSyncRejectClick = function (evt, data) {
			var self = this;
			this.syncService.rejectSyncRequest(this.currentSyncRequest,function (err, response) {
				$('.active-chat').find('.btn-sync').removeClass('disabled');
				self._resetSyncButton();
				response.from = self.attr.me;
				self.chatService.rejectSyncRequest(response,function (err, callback) {
					if (err) {
						console.error('Error rejecting the sync request!: ' + err);
					}
				});
				self.currentSyncRequest = null;
			});
		};
		
		//Local event that needs to be published
		this.onSyncedEvent = function (evt, data) {
			if (data.syncEvent) {
				return;
			}
			this.syncService.publishSyncEvent(this.attr.me,evt.type,data);
		};
		
		//Incoming sync-related msgs
		this.onIncomingSyncRequest = function (evt, data) {
			//if this isn't from who we care about, ignore it
			if (data.from != this.attr.chatUser) {
				return;
			}
			
			this.currentSyncRequest = data;
			var dom = $(syncRequestTemplate({ initiator : data.initiatorId }));
			this.$node.html(dom);
		};
		
		this.onIncomingSyncAccept = function (evt, data) {
			//if this isn't from who we care about, ignore it
			if (data.from != this.attr.chatUser) {
				return;
			}
			
			this.syncService.startSync(this.currentSyncRequest, this.onSyncMessage.bind(this),function (err,data) {
				this.trigger("syncEnded",{});
			}.bind(this));
			this._setEndButton();
		};
		
		this.onIncomingSyncReject = function (evt, data) {
			//if this isn't from who we care about, ignore it
			if (data.from != this.attr.chatUser) {
				return;
			}
			
			$('.active-chat').find('.btn-sync').removeClass('disabled');
			this._resetSyncButton();
			self.currentSyncRequest = null;
		};
		
		
		//Incoming sync event
		this.onSyncMessage = function (err, data) {
			if (err) {
				console.err('There was an error on the sync channel ' + err);
			}
			//if it came from me, who cares
			if (data.sync.initiator == this.attr.me) {
				return;
			}
			
			if (data.syncEnd) {
				this.syncService.endSync(this.currentSyncRequest);
				this.trigger('syncEnded',{});
				return;
			}
			
			data.payload.syncEvent = true;
			this.trigger(data.evt, data.payload);
		};
		
		//Clean up after a sync ends
		this.onSyncEnd = function (evt, data) {
			$('.active-chat').find('.btn-sync').removeClass('disabled');
			this._resetSyncButton();
			this.currentSyncRequest = null;
		};
		
		this._initiateRequest = function () {
			var self = this;
			var request = {
				initiatorId : this.attr.me,
				userIds : [ this.attr.chatUser ]
			};
			
			this.syncService.initiateSyncRequest (request, function (err, syncResponse) {
				if (err) {
					console.err("Error attempting to initiate sync request!: " + err);
					this._resetSyncButton();
					return;
				}
				
				self.currentSyncRequest = syncResponse;
				
				syncResponse.from = self.attr.me;
				self.chatService.sendSyncRequest(syncResponse,function (err,data) {
					//this callback should only cover the error case
					if (err) {
						console.err("Error sending the sync request to your buddy! " + err);
					}
				});
			});
		}
		
		this._setEndButton = function () {
			var dom = $(syncButtonTemplate({}));
			this.$node.html(dom);
			this.select('syncButtonSelector').addClass('btn-sync-end btn-danger').text('End Sync Session');
		}
		
		this._resetSyncButton = function () {
			var dom = $(syncButtonTemplate({}));
			this.$node.html(dom);	
		};
		
	}

});