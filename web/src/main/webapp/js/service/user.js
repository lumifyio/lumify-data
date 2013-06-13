
define(
[
    'service/serviceBase'
],
function(ServiceBase, atmosphere) {
    function UserService() {
        ServiceBase.call(this);

        return this;
    }

    UserService.prototype = Object.create(ServiceBase.prototype);

    UserService.prototype.getOnline = function (callback, onUserChange, onChatMessage) {
		var self = this;
		var result = {};
		
		self._ajaxGet({
            url: 'user/me'
        }, function (err, response) {
			
			if (err) {
				return callback(err);
			}
			
			result.user = response.user;

			self.getCurrentUsers(function (err, users) {
				result.users = users;
				console.log(result);
				return callback(err,result);
			});
		});
    };

	UserService.prototype.subscribeToChatChannel = function (userId,onmessage) {
		var chatRequest = {
			url: "/messaging/pubsub/user-" + userId,
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
		this.getSocket().subscribe(chatRequest);
	};
	
	UserService.prototype.subscribeToUserChangeChannel = function (userId,onmessage) {
		var userChangeRequest = {
			url: "/messaging/pubsub/userChanges",
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
		this.getSocket().subscribe(userChangeRequest);
	};
	
	UserService.prototype.getCurrentUsers = function (callback) {
		this._ajaxGet({
            url: 'messaging/user'
        }, function (err, response) {
			if (err) {
				return callback(err);
			}
			
			return callback(null,response.users);
		});
	}

    return UserService;
});

