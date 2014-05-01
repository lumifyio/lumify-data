
define([
    'service/vertex',
    'util/vertex/formatters'
], function(VertexService, F) {

    var PROPERTIES_TO_INSPECT_FOR_CHANGES = [
        'http://lumify.io#visibility',
        'http://lumify.io#visibilityJson',
        'detectedObjects',
        'properties',
        'sandboxStatus'
    ];

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

            if (_.isArray(vertex)) {
                var vertices = [], toRequest = [];
                vertex.forEach(function(v) {
                    var cachedVertex = self.vertex(v);
                    if (cachedVertex) {
                        vertices.push(cachedVertex);
                    } else {
                        toRequest.push(v);
                    }
                })
                deferred = $.Deferred();

                if (toRequest.length) {
                    this.vertexService.getMultiple(toRequest)
                        .done(function(data) {
                            deferred.resolve(vertices.concat(data.vertices));
                        })
                } else {
                    deferred.resolve(vertices);
                }

                return deferred;
            } else if (_.isString(vertex) || _.isNumber(vertex)) {
                deferred = this.vertexService.getVertexProperties(vertex);
            } else {
                deferred = this.vertexService.getVertexProperties(vertex.id);
            }

            return deferred;
        };

        this.getVertexTitle = function(vertexId) {
            var deferredTitle = $.Deferred(),
                v,
                vertexTitle;

            v = this.vertex(vertexId);
            if (v) {
                vertexTitle = F.vertex.prop(v, 'title');
                return deferredTitle.resolve(vertexTitle);
            }

            this.refresh(vertexId).done(function(vertex) {
                vertexTitle = F.vertex.prop(vertex, 'title');
                deferredTitle.resolve(vertexTitle);
            });

            return deferredTitle;
        };

        this.updateCacheWithVertex = function(vertex, options) {
            var id = vertex.id,
                cache = this.cachedVertices[id] || (this.cachedVertices[id] = { id: id }),
                hasChanged = !_.isEqual(
                    _.pick.apply(_, [cache].concat(PROPERTIES_TO_INSPECT_FOR_CHANGES)),
                    _.pick.apply(_, [vertex].concat(PROPERTIES_TO_INSPECT_FOR_CHANGES))
                );

            if (!cache.properties) cache.properties = [];
            if (!cache.workspace) cache.workspace = {};

            verifyVisibility(vertex);

            cache.properties = _.isUndefined(vertex.properties) ? cache.properties : vertex.properties;
            cache.workspace = $.extend(true, {}, cache.workspace, vertex.workspace || {});

            $.extend(cache, _.pick(vertex, [
                'http://lumify.io#visibility',
                'http://lumify.io#visibilityJson',
                'sandboxStatus']));

            cache.detectedObjects = vertex.detectedObjects;

            if (this.workspaceVertices[id]) {
                this.workspaceVertices[id] = cache.workspace;
            }

            var conceptType = F.vertex.prop(cache, 'conceptType', 'http://www.w3.org/2002/07/owl#Thing');
            cache.concept = this.cachedConcepts.byId[conceptType];
            if (cache.concept) {
                setPreviewsForVertex(cache, this.workspaceId);
            } else {
                console.error('Unable to attach concept to vertex', conceptType);
            }

            cache.detectedObjects = cache.detectedObjects || [];

            $.extend(true, vertex, cache);

            return (options && options.returnNullIfNotChanged === true && !hasChanged) ? null : cache;
        };

        function verifyVisibility(vertex) {
            if (!vertex) return;

            var key = 'http://lumify.io#visibilityJson',
                defaultJson = {
                    source: '',
                    _addedToPreventErrors: ''
                };

            if (!(key in vertex)) {
                vertex[key] = defaultJson;
            }

            if (vertex.properties) {
                _.each(vertex.properties, function(property) {
                    if (!(key in property)) {
                        property[key] = defaultJson;
                    }
                });
            }
        }

        function setPreviewsForVertex(vertex, currentWorkspace) {
            var params = {
                    workspaceId: currentWorkspace,
                    graphVertexId: vertex.id
                },
                artifactUrl = _.template('artifact/{ type }?' + $.param(params)),
                glyphIconHref = F.vertex.prop(vertex, 'glyphIcon');

            vertex.imageSrcIsFromConcept = false;

            if (glyphIconHref) {
                var sep = glyphIconHref.indexOf('?') > 0 ? '&' : '?';
                vertex.imageSrc = glyphIconHref + sep + $.param({workspaceId: currentWorkspace});
            } else {
                switch (vertex.concept.displayType) {

                    case 'image':
                        vertex.imageSrc = artifactUrl({ type: 'thumbnail' });
                        vertex.imageRawSrc = artifactUrl({ type: 'raw' });
                        break;

                    case 'video':
                        vertex.imageSrc = artifactUrl({ type: 'poster-frame' });
                        vertex.imageRawSrc = artifactUrl({ type: 'raw' });
                        vertex.imageFramesSrc = artifactUrl({ type: 'video-preview' });
                        break;

                    default:
                        vertex.imageSrc = vertex.concept.glyphIconHref;
                        vertex.imageRawSrc = artifactUrl({ type: 'raw' });
                        vertex.imageSrcIsFromConcept = true;
                }
            }
        }
    }
});
