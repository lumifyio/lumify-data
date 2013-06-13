
define(
[
    'service/serviceBase'
],
function(ServiceBase) {
    function ChatService() {
        ServiceBase.call(this);

        return this;
    }

    ChatService.prototype = Object.create(ServiceBase.prototype);

	/*
    ChatService.prototype.createChat = function (userId, callback) {
        return this._ajaxPost({
            url: 'chat/new',
            data: {
                userId: userId
            }
        }, callback);
    };
	*/

    ChatService.prototype.sendChatMessage = function(userId, myUserId, message, callback) {
		var chatMessage = {
            from: { id: myUserId },
            message: message,
            postDate: this._formatPostDate(),
			type: 'chatMessage'
        };

		this._sendMessage(userId,chatMessage,callback);
    };

    ChatService.prototype.sendSyncRequest = function(syncRequest, callback) {
		this._sendSyncMessage(syncRequest,syncRequest.userIds[0],'syncRequest',callback);
    };

    ChatService.prototype.acceptSyncRequest = function(syncRequest, callback) {
		this._sendSyncMessage(syncRequest,syncRequest.initiatorId,'syncRequestAcceptance',callback);
    };

    ChatService.prototype.rejectSyncRequest = function(syncRequest, callback) {
		this._sendSyncMessage(syncRequest,syncRequest.initiatorId,'syncRequestRejection',callback);
    };

	ChatService.prototype._sendSyncMessage = function (syncRequest, user, type, callback) {
		syncRequest.type = type;
		this._sendMessage(user,syncRequest,function (err, data) {
			//only need to handle error case here, don't do anything else
			if (err) {
				callback(err,null);
			}
		});
	}

	ChatService.prototype._sendMessage = function (userId, messageObj, callback, options) {
		this._publishMessage ("/messaging/pubsub/user-" + userId, messageObj, callback, options);
	}

	ChatService.prototype._formatPostDate = function () {
		var date = new Date();
		return date.toDateString();
	}

    return ChatService;
});

