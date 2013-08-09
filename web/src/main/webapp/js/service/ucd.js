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

    Ucd.prototype.deleteEdge = function(sourceId, targetId, callback) {
        return this._ajaxGet({
            url: '/node/removeRelationship',
            data: {
                sourceId: sourceId,
                targetId: targetId
            }
        }, callback);
    };

    Ucd.prototype.getNodeToNodeRelationshipDetails = function (source, target, callback){
        return this._ajaxGet({
            url: 'node/relationship',
            data: {
                source: source,
                target: target
            }
        }, callback);
    };

    Ucd.prototype.locationSearch = function(lat, lon, radiuskm, callback) {
        return this._ajaxGet({
            url: 'graph/node/geoLocationSearch',
            data: {
                lat: lat,
                lon: lon,
                radius: radiuskm
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
        return this._search("graph/node", query, callback);
    };

    Ucd.prototype.getGraphNodeById = function (id, callback) {
        return this._get("graph/node", id, callback);
    };

    Ucd.prototype.getRelatedEntitiesBySubject = function(id, callback) {
        return this._ajaxGet({ url: 'entity/' + encodeURIComponent(id) + '/relatedEntities' }, callback);
    };

    Ucd.prototype.getRelatedNodes = function(graphNodeId, resolvedOnly, callback) {
        return this._ajaxGet({ url: 'graph/' + encodeURIComponent(graphNodeId) + (resolvedOnly ? '/relatedResolvedNodes' : '/relatedNodes') }, callback);
    };

    Ucd.prototype.entityRelationships = function (id, options, callback) {
        return this._relationships("entity", id, options, callback);
    };

    Ucd.prototype.getNodeRelationships = function(graphNodeId, callback) {
        return this._ajaxGet({ url: 'node/' + encodeURIComponent(graphNodeId) + '/relationships'}, callback);
    }

    Ucd.prototype.getNodeProperties = function(graphNodeId, callback) {
        return this._ajaxGet({ url: 'node/' + encodeURIComponent(graphNodeId) + '/properties'}, callback);
    }

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
