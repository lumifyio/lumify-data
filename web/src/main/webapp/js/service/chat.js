
define(
[
    'service/serviceBase',
	'atmosphere'
],
function(ServiceBase) {
    function ChatService() {
        ServiceBase.call(this);
		this.$socket = $.atmosphere;

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
        console.log('sending chat message: chatId:', userId, 'message:', message, 'myUserId:', myUserId);
		var chatMessage = {
            from: { id: myUserId },
            message: message,
            postDate: this._formatPostDate(),
			type: 'chatMessage'
        };

		var chatRequest = {
			url: "/messaging/pubsub/user-" + userId,
			transport: "websocket",
			contentType: "text/html;charset=ISO-8859-1",
			//data: "message=" + JSON.stringify(chatMessage),
			onError: function (response) {
				callback(response.error,null);
			}, 
            onOpen: function() {
                subSocket.push({data: "message=" + JSON.stringify(chatMessage)});
            }
		};
		var subSocket = this.$socket.subscribe(chatRequest);
		//callback(null,message);
    };

	ChatService.prototype._formatPostDate = function () {
		var date = new Date();
		return date.toDateString();
	}

    return ChatService;
});

