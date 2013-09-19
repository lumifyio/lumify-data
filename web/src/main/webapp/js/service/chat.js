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

        ChatService.prototype.sendChatMessage = function (users, messageData) {
            messageData.postDate = Date.now();
            var data = {
                type: 'chatMessage',
                permissions: {
                    users: users
                },
                data: messageData
            };

            this.socketPush(data);

            var deferred = $.Deferred();

            deferred.resolve(messageData);

            return deferred;
        };

        return ChatService;
    });

