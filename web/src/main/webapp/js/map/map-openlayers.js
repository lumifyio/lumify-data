

define([
    'flight/lib/component',
    'data',
    'tpl!./template',
    'tpl!./instructions/regionCenter',
    'tpl!./instructions/regionRadius',
    'tpl!./instructions/regionLoading',
    'service/ucd',
    'service/vertex',
    'util/retina',
    'util/withAsyncQueue',
    'util/withContextMenu'
], function(defineComponent,
    appData,
    template,
    centerTemplate,
    radiusTemplate,
    loadingTemplate,
    UcdService,
    VertexService,
    retina,
    withAsyncQueue,
    withContextMenu) {
    'use strict';

    // TODO: persist location and zoom in workspace
    var START_COORDINATES = [42.472, -71.147],
        MODE_NORMAL = 0,
        MODE_REGION_SELECTION_MODE_POINT = 1,
        MODE_REGION_SELECTION_MODE_RADIUS = 2,
        MODE_REGION_SELECTION_MODE_LOADING = 3;

    return defineComponent(MapViewOpenLayers, withContextMenu, withAsyncQueue);

    function MapViewOpenLayers() {

        var ol;

        this.ucdService = new UcdService();
        this.vertexService = new VertexService();
        this.mode = MODE_NORMAL;
        this.markers = {};

        this.defaultAttrs({
            mapSelector: '#map'
        });

        this.after('initialize', function() {
            this.initialized = false;
            this.setupAsyncQueue('openlayers');
            this.setupAsyncQueue('map');
            this.$node.html(template({}));

            this.on(document, 'mapShow', this.onMapShow);
            this.on(document, 'mapCenter', this.onMapCenter);
            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on(document, 'verticesAdded', this.onVerticesAdded);
            this.on(document, 'verticesUpdated', this.onVerticesUpdated);
            this.on(document, 'verticesDeleted', this.onVerticesDeleted);
            this.on(document, 'verticesDropped', this.onVerticesDropped);
            this.on(document, 'verticesSelected', this.onVerticesSelected);

            this.updateOrAddVertices(appData.verticesInWorkspace(), { adding:true });
        });

        this.onMapShow = function() {
            if (!this.mapIsReady()) this.initializeMap();
        };

        this.onMapCenter = function() { };

        this.onWorkspaceLoaded = function(evt, workspaceData) {
            var self = this;
            this.isWorkspaceEditable = workspaceData.isEditable;
            this.mapReady(function(map) {
                map.removeLayer(map.markersLayer);
                map.addLayer(map.markersLayer = new ol.Layer.Markers('Markers'));
                this.updateOrAddVertices(workspaceData.data.vertices, { adding:true });
            });
        };

        this.onVerticesAdded = this.onVerticesDropped = function(evt, data) {
            this.updateOrAddVertices(data.vertices, { adding:true });
        };

        this.onVerticesUpdated = function(evt, data) {
            this.updateOrAddVertices(data.vertices);
        };

        this.onVerticesDeleted = function(evt, data) { 
            // TODO: implement
        };

        this.onVerticesSelected = function(evt, data) {
            var self = this,
                vertices = _.isArray(data) ? data : data ? [data] : [];
            
            this.mapReady(function(map) {
                var self = this,
                    selectedIds = _.pluck(vertices, 'id');

                vertices.forEach(function(vertex) {
                    self.findOrCreateMarker(map, vertex);
                });

                map.markersLayer.markers.forEach(function(marker) {
                    if (! ~selectedIds.indexOf(marker.id)) {
                        if (marker.inWorkspace) {
                            marker.setUrl(marker.icon.url.replace(/&selected/, ''));
                        } else {
                            marker.display(false);
                        }
                    }
                });
            });
        };

        this.findOrCreateMarker = function(map, vertex) {
            var self = this,
                marker = this.markers[vertex.id],
                geoLocation = vertex.properties.geoLocation,
                subType = vertex.properties._subType,
                heading = vertex.properties.heading,
                selected = ~appData.selectedVertices.indexOf(vertex.id),
                iconUrl =  '/map/marker/' + subType + '/image?scale=' + (retina.devicePixelRatio > 1 ? '2' : '1');

            if (!geoLocation || !geoLocation.latitude || !geoLocation.longitude) return;

            if (heading) iconUrl += '&heading=' + heading;
            if (selected) iconUrl += '&selected';

            if (!marker) {
                var size = new ol.Size(22,40),
                    offset = new ol.Pixel(-(size.w/2), -size.h),
                           icon = new ol.Icon(iconUrl, size, offset),
                           pt = latLon(geoLocation.latitude, geoLocation.longitude);

                marker = this.markers[vertex.id] = new ol.Marker(pt, icon);
                marker.id = vertex.id;
                marker.vertex = vertex;
                marker.events.register("click", marker, function(event) {
                    self.trigger('verticesSelected', [this.vertex]);
                });
                map.markersLayer.addMarker(marker);
            } else {
                marker.setUrl(iconUrl);
                // TODO: update position
                // TODO: update heading
            }

            marker.display(true);

            return marker;
        };

        this.updateOrAddVertices = function(vertices, options) {
            var self = this,
                adding = options && options.adding;

            this.mapReady(function(map) {
                vertices.forEach(function(vertex) {
                    var inWorkspace = appData.inWorkspace(vertex);

                    if (inWorkspace || self.markers[vertex.id]) {
                        var marker = self.findOrCreateMarker(map, vertex);
                        if (marker) {
                            marker.inWorkspace = inWorkspace;
                        }
                    }
                });

                // TODO: implement fit
            });

        };

        this.initializeMap = function() {
            var self = this;

            this.openlayersReady(function(ol) {
                var map = new ol.Map('map', { 
                        zoomDuration: 0,
                        numZoomLevels: 18,
                        displayProjection: new ol.Projection("EPSG:4326"),
                        controls: [
                            new ol.Control.Navigation({
                                dragPanOptions: {
                                    enableKinetic: true
                                }
                            })
                        ]
                    }),
                    base = new ol.Layer.Google("Google Streets", {
                        numZoomLevels: 20
                    });

                map.events.register("click", map, function(event) {
                    self.trigger('verticesSelected', []);
                });

                map.markersLayer = new ol.Layer.Markers('Markers');
                map.addLayers([base, map.markersLayer]);

                latLon = latLon.bind(null, map.displayProjection, map.getProjectionObject());

                map.setCenter(latLon(START_COORDINATES), 7);
                this.mapMarkReady(map);
            });

            window.googleV3Initialized = function() {
                google.maps.visualRefresh = true;
                if (ol) {
                    self.openlayersMarkReady(ol);
                }
                delete window.googleV3Initialized;
            };

            require(['openlayers'], function(OpenLayers) {
                ol = OpenLayers;
                if (google.maps.version) {
                    self.openlayersMarkReady(ol);
                }
            });
        };


        function latLon(sourceProjection, destProjection, lat, lon) {
            if (arguments.length === 3 && _.isArray(lat) && lat.length === 2) {
                lon = lat[1];
                lat = lat[0];
            }

            return new ol.LonLat(lon, lat).transform(sourceProjection, destProjection);
        }
    }

});

