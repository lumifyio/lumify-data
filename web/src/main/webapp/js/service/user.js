
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

    UserService.prototype.getOnline = function (callback) {
        return this._ajaxGet({
            url: 'user/messages'
        }, callback);
    };

    return UserService;
});

