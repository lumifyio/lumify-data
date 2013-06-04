
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

    ChatService.prototype.createChat = function (userId, callback) {
        return this._ajaxPost({
            url: 'chat/new',
            data: {
                userId: userId
            }
        }, callback);
    };

    ChatService.prototype.sendChatMessage = function(chatId, message, callback) {
        console.log('sending chat message: chatId:', chatId, 'message:', message);
        return this._ajaxPost({
            url: 'chat/' + chatId + '/post',
            data: {
                message: message
            }
        }, callback);
    };

    return ChatService;
});

