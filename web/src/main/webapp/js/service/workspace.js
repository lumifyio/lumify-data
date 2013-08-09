
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

    WorkspaceService.prototype.getByRowKey = function (_rowKey, callback) {
        return this._ajaxGet({
            url: 'workspace/' + _rowKey
        }, callback);
    };

    WorkspaceService.prototype.saveNew = function (workspace, callback) {
        console.log("workspace saveNew:", workspace);
        workspace.data = JSON.stringify(workspace.data);
        this._ajaxPost({
            url: 'workspace/save',
            data: workspace
        }, callback);
    };

    WorkspaceService.prototype.save = function (_rowKey, workspace, callback) {
        console.log("workspace save:", _rowKey, workspace);
        workspace.data = JSON.stringify(workspace.data);
        this._ajaxPost({
            url: 'workspace/' + _rowKey + '/save',
            data: workspace
        }, callback);
    };

    WorkspaceService.prototype.delete = function(_rowKey, callback) {
        console.log("workspace delete:", _rowKey);
        this._ajaxDelete({
            url: 'workspace/' + _rowKey,
            data: {
                _rowKey: _rowKey
            }
        }, callback);
    }

    return WorkspaceService;
});

