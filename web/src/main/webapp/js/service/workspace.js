
define(
[
    'service/serviceBase'
],
function(ServiceBase) {
    'use strict';

    function WorkspaceService() {
        ServiceBase.call(this);

        return this;
    }

    WorkspaceService.prototype = Object.create(ServiceBase.prototype);

    WorkspaceService.prototype.list = function() {
        return this._ajaxGet({
            url: 'workspace/'
        });
    };

    WorkspaceService.prototype.getByRowKey = function (_rowKey) {
        return this._ajaxGet({
            url: 'workspace/' + _rowKey
        });
    };

    WorkspaceService.prototype.saveNew = function (workspace) {
        workspace.data = JSON.stringify(workspace.data);
        return this._ajaxPost({
            url: 'workspace/save',
            data: workspace
        });
    };

    WorkspaceService.prototype.save = function (_rowKey, workspace) {
        workspace.data = JSON.stringify(workspace.data);
        return this._ajaxPost({
            url: 'workspace/' + _rowKey + '/save',
            data: workspace
        });
    };

    WorkspaceService.prototype.delete = function(_rowKey) {
        return this._ajaxDelete({
            url: 'workspace/' + _rowKey,
            data: {
                _rowKey: _rowKey
            }
        });
    };

    return WorkspaceService;
});

