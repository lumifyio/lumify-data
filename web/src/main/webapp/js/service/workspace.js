
define(
[
    'service/serviceBase'
],
function(ServiceBase) {
    function WorkspaceService() {
        ServiceBase.call(this);

        return this;
    }

    WorkspaceService.prototype = Object.create(ServiceBase.prototype);

    WorkspaceService.prototype.list = function (callback) {
        return this._ajaxGet({
            url: 'workspace/'
        }, callback);
    };

    WorkspaceService.prototype.getByRowKey = function (rowKey, callback) {
        return this._ajaxGet({
            url: 'workspace/' + rowKey
        }, callback);
    };

    WorkspaceService.prototype.saveNew = function (data, callback) {
        var self = this;
        console.log("workspace saveNew:", data);
        if(data instanceof Object) {
            data = JSON.stringify(data);
        }
        return this._validateData(data, function(err) {
            if(err) {
                return callback(err);
            }
            return self._ajaxPost({
                url: 'workspace/save',
                data: {
                    data: data
                }
            }, callback);
        });
    };

    WorkspaceService.prototype.save = function (rowKey, data, callback) {
        var self = this;
        console.log("workspace save:", rowKey, data);
        if(data instanceof Object) {
            data = JSON.stringify(data);
        }
        return this._validateData(data, function(err) {
            if(err) {
                return callback(err);
            }
            return self._ajaxPost({
                url: 'workspace/' + rowKey + '/save',
                data: {
                    data: data
                }
            }, callback);
        });
    };

    WorkspaceService.prototype._validateData = function(data, callback) {
        try {
            JSON.parse(data);
            return callback();
        } catch(e) {
            console.error("Failed to validate data:", data);
            return callback(e);
        }
    };

    return WorkspaceService;
});

