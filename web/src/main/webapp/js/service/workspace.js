
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
        console.log("workspace saveNew:", data);
        this._ajaxPost({
            url: 'workspace/save',
            data: data
        }, callback);
    };

    WorkspaceService.prototype.save = function (rowKey, workspace, callback) {
        console.log("workspace save:", rowKey, workspace);
        workspace.data = JSON.stringify(workspace.data);
        this._ajaxPost({
            url: 'workspace/' + rowKey + '/save',
            data: workspace
        }, callback);
    };

    WorkspaceService.prototype.delete = function(rowKey, callback) {
        console.log("workspace delete:", rowKey);
        this._ajaxDelete({
            url: 'workspace/' + rowKey,
            data: {
                rowKey: rowKey
            }
        }, callback);
    }

    return WorkspaceService;
});

