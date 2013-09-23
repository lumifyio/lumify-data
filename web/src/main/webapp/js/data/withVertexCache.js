
define([
    'service/ucd'
], function(Ucd) {

    return withVertexCache;

    function withVertexCache() {

        this.cachedVertices = {};
        this.workspaceVertices = {};
        if (!this.ucdService) {
            this.ucdService = new Ucd();
        }


        this.resemblesVertices = function(val) {
            return val && val.length && val[0].id && val[0].properties;
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

            console.log('getting', ids);
            return _.values(_.pick(this.cachedVertices, ids));
        };

        this.refresh = function(vertex) {
            var self = this,
                deferred = null;

            if (_.isString(vertex)) {
                deferred = this.ucdService.getVertexProperties(vertex);
            } else if (vertex.properties._type === 'artifact' && vertex.properties._rowKey) {
                deferred = $.when(
                    this.ucdService.getArtifactById(vertex.properties._rowKey),
                    this.ucdService.getVertexProperties(vertex.id)
                );
            } else {
                deferred = this.ucdService.getVertexProperties(vertex.id);
            }

            return deferred.then(function() {
                return self.vertex(vertex.id);
            });
        };

        this.updateCacheWithArtifact = function(artifact, subType) {

            // Determine differences between artifact search and artifact get requests
            var id = artifact.graphVertexId || artifact.Generic_Metadata['atc:graph_vertex_id'],
                rowKey = artifact._rowKey || artifact.key.value,
                content = artifact.Content;

            // Fix characters
            if (rowKey) {
                artifact._rowKey = encodeURIComponent((rowKey || '').replace(/\\[x](1f)/ig, '\u001f'));
            }

            if (content) {
                // Format Html
                artifact.contentHtml = (content.highlighted_text || content.doc_extracted_text || '').replace(/[\n]+/g, "<br><br>\n");

                // Video transcripts
                if(content.video_transcript) {
                    artifact.videoTranscript = JSON.parse(artifact.Content.video_transcript);
                    artifact.videoDuration = artifact.Content['atc:video_duration'];
                }
            }

            if (artifact['atc:Artifact_Detected_Objects']) {
                // TODO: reference cached vertices
                artifact.detectedObjects = artifact['atc:Artifact_Detected_Objects'].detectedObjects.sort(function(a, b){
                    var aX = a.info.coords.x1, bX = b.info.coords.x1;
                    return aX - bX;
                });
            } else artifact.detectedObjects = [];

            var cache = this.updateCacheWithVertex({
                id: id,
                properties: {
                    _rowKey: rowKey,
                    _type: 'artifact',
                    _subType: subType || artifact.type,
                    source: artifact.source
                }
            });

            // Properties from artifacts that don't override vertex
            if (!cache.properties.title) {
                cache.properties.title = artifact.subject || artifact.Generic_Metadata.subject || 'No Title Available';
            }
            if (!cache.properties.geoLocation) {
                if (artifact.Dynamic_Metadata && 
                    artifact.Dynamic_Metadata['atc:geoLocationTitle'] &&
                    artifact.Dynamic_Metadata.latitude &&
                    artifact.Dynamic_Metadata.longitude) {

                    cache.properties.geoLocation = {
                        latitude: artifact.Dynamic_Metadata.latitude,
                        longitude: artifact.Dynamic_Metadata.longitude,
                        title: artifact.Dynamic_Metadata['atc:geoLocationTitle']
                    };
                }
            }

            $.extend(true, cache.artifact || (cache.artifact = {}), artifact);
            return cache;
        };

        this.updateCacheWithVertex = function(vertex, options) {
            var id = vertex.id,
                cache = this.cachedVertices[id] || (this.cachedVertices[id] = { id:id });

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
