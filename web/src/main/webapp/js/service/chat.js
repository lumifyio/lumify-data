define(
    [
        'service/serviceBase'
    ],
    function (ServiceBase) {
        function ChatService() {
            ServiceBase.call(this);

            return this;
        }

        ChatService.prototype = Object.create(ServiceBase.prototype);

        ChatService.prototype.sendChatMessage = function (messageData, callback) {
            messageData.postDate = Date.now();
            var data = {
                type: 'chatMessage',
                data: messageData
            };

            this.socketPush(data);
            return callback(null, messageData);
        };

        ChatService.prototype.sendSyncRequest = function (syncRequest, callback) {
            this._sendSyncMessage(syncRequest, syncRequest.userIds[0], 'syncRequest', callback);
        };

        ChatService.prototype.acceptSyncRequest = function (syncRequest, callback) {
            this._sendSyncMessage(syncRequest, syncRequest.initiatorId, 'syncRequestAcceptance', callback);
        };

        ChatService.prototype.rejectSyncRequest = function (syncRequest, callback) {
            this._sendSyncMessage(syncRequest, syncRequest.initiatorId, 'syncRequestRejection', callback);
        };

        ChatService.prototype._sendSyncMessage = function (syncRequest, user, type, callback) {
            syncRequest.type = type;
            this._sendMessage(user, syncRequest, function (err, data) {
                //only need to handle error case here, don't do anything else
                if (err) {
                    callback(err, null);
                } else {
                    callback(null, data);
                }
            });
        };

        return ChatService;
    });

