define(
[
    'service/serviceBase'
],
function(ServiceBase) {
    function Ucd() {
        ServiceBase.call(this);

        return this;
    }

    Ucd.prototype = Object.create(ServiceBase.prototype);

    Ucd.prototype.getRelationships = function(ids, callback) {
        return this._ajaxPost({
            url: 'entity/relationships',
            data: {
                ids: ids
            }
        }, callback);
    };

    Ucd.prototype.getEntityToEntityRelationshipDetails = function(source, target, callback) {
        return this._ajaxGet({
            url: 'entity/relationship',
            data: {
                source: source,
                target: target
            }
        }, callback);
    };

    Ucd.prototype.getStatementByRowKey = function(statementRowKey, callback) {
        return this._get("statement", statementRowKey, callback);
    };

    Ucd.prototype.artifactSearch = function(query, callback) {
        return this._search("artifact", query, callback);
    };

    Ucd.prototype.getArtifactById = function (id, callback) {
        return this._get("artifact", id, callback);
    };

    Ucd.prototype.getRawArtifactById = function (id, callback) {
        //maybe it's an object for future options stuff?
        var i = typeof id == "object" ? id.id : id;

        return this._ajaxGet({
            url: "artifact/" + i + "/raw",
        }, callback);
    },

    Ucd.prototype.artifactRelationships = function (id, options, callback) {
        return this._relationships("artifact", id, options, callback);
    };

    Ucd.prototype.entitySearch = function (query, callback) {
        return this._search("entity", query, callback);
    };

    Ucd.prototype.getEntityById = function (id, callback) {
        return this._get("entity", id.replace(/\./g, '$2E$'), callback);
    };

    Ucd.prototype.getEntityMentionsByRange = function (url, callback) {
        return this._ajaxGet({ url: url }, callback);
    };

    Ucd.prototype.getEntityRelationshipsBySubject = function(id, callback) {
        return this._ajaxGet({ url: 'entity/' + id + '/relationships' }, callback);
    };

    Ucd.prototype.getRelatedEntitiesBySubject = function(id, callback) {
        return this._ajaxGet({ url: 'entity/' + encodeURIComponent(id) + '/relatedEntities' }, callback);
    };

    Ucd.prototype.getRelatedNodes = function(graphNodeId, callback) {
        return this._ajaxGet({ url: 'graph/' + encodeURIComponent(graphNodeId) + '/relatedNodes' }, callback);
    };

    Ucd.prototype.getSpecificEntityRelationship = function (e1, e2, callback) {
        return this._ajaxGet({
            url: 'entity/relationship',
            data: {
                entity1: e1,
                entity2: e2
            }
        }, callback);
    };

    Ucd.prototype.entityRelationships = function (id, options, callback) {
        return this._relationships("entity", id, options, callback);
    };

    Ucd.prototype._relationship = function (resource, id, options, callback) {
        var data = {};
        var success = callback;
        if (callback && $.isFunction(callback)) {
            data = options;
        } else if (options && $.isFunction(options)) {
            success = options;
        }

        return this._ajaxGet({
            url: resource + id + "/relationships",
            data: data
        }, success);
    };

    Ucd.prototype._search = function (resource, query, callback) {
        //maybe it's an object for future options stuff?
        var q = typeof query == "object" ? query.query : query;
        var url = resource + "/search";

        return this._ajaxGet({
            url: url,
            data: {
                'q' : q
            }
        }, callback);
    };

    Ucd.prototype._get = function (resource, id, callback) {
        if(!id) {
            return callback(new Error("Invalid or no id specified for resource '" + resource + "'"));
        }

        //maybe it's an object for future options stuff?
        var i = encodeURIComponent(typeof id == "object" ? id.id : id);
        return this._ajaxGet({
            url: resource + "/" + i,
        }, callback);
    };

    return Ucd;
});
