
define([
    'service/vertex',
], function(VertexService) {

    return withVertexCache;

    function withVertexCache() {

        this.cachedVertices = {};
        this.workspaceVertices = {};
        if (!this.vertexService) {
            this.vertexService = new VertexService();
        }

        this.resemblesVertices = function(val) {
            return val && val.length && val[0].id && val[0].properties;
        };

        this.verticesInWorkspace = function() {
            return _.values(_.pick(this.cachedVertices, _.keys(this.workspaceVertices)));
        };

        this.copy = function(obj) {
            return JSON.parse(JSON.stringify(obj));
        };

        this.workspaceOnlyVertexCopy = function(vertex) {
            return {
                id: vertex.id,
                workspace: this.copy(vertex.workspace || this.workspaceVertices[vertex.id] || {})
            };
        };

        this.workspaceOnlyVertex = function(id) {
            return {
                id: id,
                workspace: this.workspaceVertices[id] || {}
            };
        };

        this.inWorkspace = function(vertex) {
            return !!this.workspaceVertices[_.isString(vertex) ? vertex : vertex.id];
        };

        this.vertex = function(id) {
            return this.cachedVertices['' + id];
        };

        this.vertices = function(ids) {
            if (ids.toArray) {
                ids = ids.toArray();
            }

            ids = ids.map(function(id) {
                return id.id ? id.id : '' + id;
            });

            return _.values(_.pick(this.cachedVertices, ids));
        };

        this.refresh = function(vertex) {
            var self = this,
                deferred = null;

            if (_.isString(vertex) || _.isNumber(vertex)) {
                deferred = this.vertexService.getVertexProperties(vertex);
            } else {
                deferred = this.vertexService.getVertexProperties(vertex.id);
            }

            return deferred.then(function(v) {
                return self.vertex((v && v.id) || vertex.id);
            });
        };

        this.updateCacheWithVertex = function(vertex, options) {
            var id = vertex.id;
            var cache = this.cachedVertices[id] || (this.cachedVertices[id] = { id: id });

            if (options && options.deletedProperty && cache.properties) {
                delete cache.properties[options.deletedProperty]
            }

            $.extend(true, cache.properties || (cache.properties = {}), vertex.properties);
            $.extend(true, cache.workspace ||  (cache.workspace = {}),  vertex.workspace || {});

            if (this.workspaceVertices[id]) {
                this.workspaceVertices[id] = cache.workspace;
            }

            if (_.isString(cache.properties.geoLocation)) {
                var m = cache.properties.geoLocation.match(/point\[(.*?),(.*?)\]/);
                if (m) {
                    var latitude = m[1];
                    var longitude = m[2];
                    cache.properties.geoLocation = {
                        latitude: latitude,
                        longitude: longitude,
                        title: cache.properties._geoLocationDescription
                    };
                }
            } 

            cache.concept = this.cachedConcepts.byId[cache.properties._conceptType]
            if (!cache.concept) {
                console.error('Unable to attach concept to vertex', cache);
            }

            if (cache.properties.latitude || cache.properties.longitude) {
                $.extend(cache.properties.geoLocation || (cache.properties.geoLocation = {}), {
                    latitude: cache.properties.latitude,
                    longitude: cache.properties.longitude,
                    title: cache.properties._geoLocationDescription
                });
                delete cache.properties.latitude;
                delete cache.properties.longitude;
                delete cache.properties._geoLocationDescription;
            }

            return cache;
        };

    }
});
