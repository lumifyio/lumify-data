define(
    [
        'service/serviceBase'
    ],
    function (ServiceBase, atmosphere) {
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

                result.user = response;

                self.getCurrentUsers(function (err, users) {
                    result.users = users;
                    return callback(err, result);
                });
            });
        };

        UserService.prototype.getCurrentUsers = function (callback) {
            this._ajaxGet({
                url: '/user/'
            }, function (err, response) {
                if (err) {
                    return callback(err);
                }

                return callback(null, response.users);
            });
        }

        return UserService;
    });

