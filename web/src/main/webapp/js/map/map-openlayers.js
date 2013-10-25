

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
                map.featuresLayer.removeAllFeatures();
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
                    featuresLayer = map.featuresLayer,
                    selectedIds = _.pluck(vertices, 'id'),
                    toRemove = [];

                featuresLayer.features.forEach(function unselectFeature(feature) {
                    if (feature.cluster) {
                        feature.cluster.forEach(unselectFeature);
                        return;
                    }

                    if (!~selectedIds.indexOf(feature.id)) {
                        if (feature.data.inWorkspace) {
                            feature.style.externalGraphic = feature.style.externalGraphic.replace(/&selected/, '');
                        } else {
                            toRemove.push(feature);
                        }
                    }
                });

                featuresLayer.removeFeatures(toRemove);

                vertices.forEach(function(vertex) {
                    self.findOrCreateMarker(map, vertex);
                });

                featuresLayer.redraw();
            });
        };

        this.featureForId = function(map, id) {
            return _.findWhere(map.featuresLayer.features, { id: id });
        };

        this.findOrCreateMarker = function(map, vertex) {
            var self = this,
                feature = this.featureForId(vertex.id),
                geoLocation = vertex.properties.geoLocation,
                subType = vertex.properties._subType,
                heading = vertex.properties.heading,
                selected = ~appData.selectedVertices.indexOf(vertex.id),
                iconUrl =  '/map/marker/' + subType + '/image?scale=' + (retina.devicePixelRatio > 1 ? '2' : '1');

            if (!geoLocation || !geoLocation.latitude || !geoLocation.longitude) return;

            if (heading) iconUrl += '&heading=' + heading;
            if (selected) iconUrl += '&selected';

            if (!feature) {
                feature = new ol.Feature.Vector(
                    point(geoLocation.latitude, geoLocation.longitude),
                    { vertex: vertex },
                    {
                        graphic: true,
                        externalGraphic: iconUrl,
                        graphicWidth: 22,
                        graphicHeight: 40,
                        graphicXOffset: -11,
                        graphicYOffset: -40,
                        cursor: 'pointer',
                       /*
                        label: vertex.properties.title.length > 15 ? vertex.properties.title.substring(0,15) + '...' : vertex.properties.title,
                        fontColor: '#000',
                        labelOutlineColor: '#fff',
                        labelOutlineWidth: '2px',
                        fontSize: '16px',
                        fontWeight: 'bold',
                        labelYOffset: -10
                        */
                    }
                );
                feature.id = vertex.id;
                map.featuresLayer.addFeatures(feature);
            } else {
                feature.style.externalGraphic = iconUrl;
                // TODO: update position
                // TODO: update heading
            }

            if (!feature.cluster) feature.style.display = '';

            return feature;
        };

        this.updateOrAddVertices = function(vertices, options) {
            var self = this,
                adding = options && options.adding;

            this.mapReady(function(map) {
                vertices.forEach(function(vertex) {
                    var inWorkspace = appData.inWorkspace(vertex),
                        feature = self.featureForId(vertex.id);

                    if (inWorkspace || feature) {
                        var marker = self.findOrCreateMarker(map, vertex);
                        if (marker) {
                            marker.data.inWorkspace = inWorkspace;
                        }
                    }
                });

                map.featuresLayer.redraw();

                if (adding) {
                    map.zoomToExtent(map.featuresLayer.getDataExtent()); 
                }
            });

        };

        this.initializeMap = function() {
            var self = this;

            this.openlayersReady(function(ol) {
                require(['map/clusterStrategy'], function(cluster) {
                    self.createMap(ol, cluster);
                });
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

        this.createMap = function(ol, ClusterStrategy) {
            var self = this,
                map = new ol.Map('map', { 
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

            var cluster = new ClusterStrategy({ 
                    distance: 45,
                    threshold: 2,
                    animationMethod: ol.Easing.Expo.easeOut,
                    animationDuration: 100
                }),
                style = self.featureStyle(),
                selectedStyle = {
                    fillColor:'#0070C3', labelOutlineColor:'#08538B', strokeColor:'#08538B' 
                };

            map.featuresLayer = new ol.Layer.Vector('Markers', {
                strategies: [ cluster ],
                styleMap: new ol.StyleMap({
                    'default': new ol.Style(style.baseStyle, style.baseContext),
                    'select': new ol.Style($.extend({}, style.baseStyle, selectedStyle), style.baseContext)
                })
            });

            // Feature Clustering
            cluster.activate();

            // Feature Selection
            var selectFeature = new ol.Control.SelectFeature(map.featuresLayer);
            map.addControl(selectFeature);
            selectFeature.activate();
            map.featuresLayer.events.on({
                featureselected: function(featureEvents) {
                    var vertices;
                    if (featureEvents.feature.cluster) {
                        vertices = [_.map(featureEvents.feature.cluster, function(feature) {
                            return feature.data.vertex; 
                        })];
                    } else vertices = [featureEvents.feature.data.vertex];
                    self.trigger('verticesSelected', vertices);
                }
            });
            map.events.register("click", map, function(event) {
                self.trigger('verticesSelected', []);
            });

            map.addLayers([base, map.featuresLayer]);

            latLon = latLon.bind(null, map.displayProjection, map.getProjectionObject());
            point = point.bind(null, map.displayProjection, map.getProjectionObject());

            map.setCenter(latLon(START_COORDINATES), 7);

            this.featureForId = this.featureForId.bind(this, map);
            this.mapMarkReady(map);
        };

        this.featureStyle = function() {
            return {
                baseStyle: {
                    pointRadius: "${radius}",
                    label: "${label}",
                    labelOutlineColor: '#AD2E2E',
                    labelOutlineWidth: '2',
                    fontWeight: 'bold',
                    fontSize: '16px',
                    fontColor: '#ffffff',
                    fillColor: "#F13B3C",
                    fillOpacity: 0.8,
                    strokeColor: "#AD2E2E",
                    strokeWidth: 3,
                    cursor: 'pointer'
                },
                baseContext: {
                    context: {
                        label: function(feature) {
                            return feature.attributes.count + '';
                        },
                        radius: function(feature) {
                            var count = Math.min(feature.attributes.count || 0, 10);
                            return count + 10;
                        }
                    }
                }
            };
        };

        function point(sourceProjection, destProjection, x, y) {
            if (arguments.length === 3 && _.isArray(x) && x.length === 2) {
                y = x[1];
                x = x[0];
            }

            return new ol.Geometry.Point(y, x).transform(sourceProjection, destProjection);
        }

        function latLon(sourceProjection, destProjection, lat, lon) {
            if (arguments.length === 3 && _.isArray(lat) && lat.length === 2) {
                lon = lat[1];
                lat = lat[0];
            }

            return new ol.LonLat(lon, lat).transform(sourceProjection, destProjection);
        }
    }

});

