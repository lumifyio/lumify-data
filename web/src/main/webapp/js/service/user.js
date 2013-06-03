
define(
[
    'service/serviceBase'
],
function(ServiceBase) {
    function UserService() {
        ServiceBase.call(this);

        return this;
    }

    UserService.prototype = Object.create(ServiceBase.prototype);

    UserService.prototype.createChat = function (userId, callback) {
        return this._ajaxPost({
            url: 'chat/new',
            data: {
                userId: userId
            }
        }, callback);
    };

    UserService.prototype.sendChatMessage = function(chatId, message, callback) {
        console.log('sending chat message: chatId:', chatId, 'message:', message);
        return this._ajaxPost({
            url: 'chat/' + chatId + '/post',
            data: {
                message: message
            }
        }, callback);
    };

    UserService.prototype.getOnline = function (callback) {
        var url = this._resolveUrl();

        return this._ajaxGet({
            url: 'user/messages'
        }, callback);
    };

    return UserService;
});

