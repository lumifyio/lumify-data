

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
            mapSelector: '#map',
            contextMenuSelector: '.contextmenu',
            contextMenuVertexSelector: '.contextmenuvertex'
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
            if (this.mapIsReady()) {
                this.mapReady(function(map) {
                    _.defer(function() {
                        map.updateSize();
                    });
                });
            } else this.initializeMap();
        };

        this.onMapCenter = function(evt, data) {
            this.mapReady(function(map) {
                map.setCenter(latLon(data.latitude, data.longitude), 7);
                map.featuresLayer.redraw();
            });
        };

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
            this.mapReady(function(map) {
                var featuresLayer = map.featuresLayer,
                    toRemove = [],
                    ids = _.pluck(data.vertices, 'id');

                featuresLayer.features.forEach(function removeIfDeleted(feature) {
                    if (~ids.indexOf(feature.id)) {
                        toRemove.push(feature);
                    } else if (feature.cluster) {
                        feature.cluster.forEach(removeIfDeleted);
                    }
                });

                featuresLayer.removeFeatures(toRemove);
                this.clusterStrategy.cluster();
            });
        };

        this.onVerticesSelected = function(evt, data) {
            var self = this,
                vertices = _.isArray(data) ? data : data ? [data] : [];
            
            this.mapReady(function(map) {
                var self = this,
                    featuresLayer = map.featuresLayer,
                    selectedIds = _.pluck(vertices, 'id'),
                    toRemove = [];

                // Remove features not selected and not in workspace
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

                if (toRemove.length) {
                    featuresLayer.removeFeatures(toRemove);
                }

                // Create new features for new selections
                vertices.forEach(function(vertex) {
                    self.findOrCreateMarker(map, vertex);
                });

                // Select clustered items that contain selected features
                featuresLayer.features.forEach(function(feature) {
                    if (feature.cluster) {
                        var someSelected = _.some(feature.cluster, function(f) {
                            return ~selectedIds.indexOf(f.id);
                        });
                        if (someSelected) {
                            feature.renderIntent = 'select';
                        } else {
                            feature.renderIntent = 'default';
                        }
                    }
                });

                var sf = this.clusterStrategy.selectedFeatures = {};
                selectedIds.forEach(function(sId) { sf[sId] = true; });

                featuresLayer.redraw();
            });
        };

        this.findOrCreateMarker = function(map, vertex) {
            var self = this,
                feature = map.featuresLayer.getFeatureById(vertex.id),
                geoLocation = vertex.properties.geoLocation,
                subType = vertex.properties._subType,
                heading = vertex.properties.heading,
                selected = ~appData.selectedVertices.indexOf(vertex.id),
                iconUrl =  '/map/marker/' + subType + '/image?scale=' + (retina.devicePixelRatio > 1 ? '2' : '1');

            if (!geoLocation || !geoLocation.latitude || !geoLocation.longitude) return;

            if (heading) iconUrl += '&heading=' + heading;
            if (selected) iconUrl += '&selected';

            if (!feature) {
                map.featuresLayer.features.forEach(function(f) {
                    if (f.cluster) {
                        feature = _.findWhere(f.cluster, { id:vertex.id });
                    }
                });
            }

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
                        cursor: 'pointer'
                    }
                );
                feature.id = vertex.id;
                map.featuresLayer.addFeatures(feature);
            } else {
                feature.style.externalGraphic = iconUrl;
                feature.move(latLon(geoLocation.latitude, geoLocation.longitude));
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
                        feature = map.featuresLayer.getFeatureById(vertex.id);

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

        this.invalidMap = function() {
            var map = this.select('mapSelector'),
                cls = 'invalid',
                animate = function() {
                    map.removeClass(cls);
                    _.defer(function() {
                        map.on('animationend MSAnimationEnd webkitAnimationEnd oAnimationEnd oanimationend', function() {
                            map.removeClass(cls);
                        });
                        map.addClass(cls);
                    });
                };

            if (this.$node.closest('.visible').length === 0) {
                return;
            } else {
                animate();
            }
        };

        this.handleContextMenu = function(event) {
            event.originalEvent = event.originalEvent || event;
            

            this.mapReady(function(map) {
                var feature = map.featuresLayer.getFeatureFromEvent(event);
                if (feature) {
                    var menu = this.select('contextMenuVertexSelector');
                    menu.data('feature', feature);
                    this.toggleMenu({ positionUsingEvent:event }, menu);
                } else this.toggleMenu({ positionUsingEvent:event }, this.select('contextMenuSelector'));
            });
        };

        this.onContextMenuRemoveItem = function() {
            var menu = this.select('contextMenuVertexSelector'),
                feature = menu.data('feature'),
                vertices = (feature.cluster || [feature]).map(function(f) {
                    return f.data.vertex;
                });

            this.trigger('deleteVertices', { vertices:vertices });
        };

        this.onContextMenuLoadResultsWithinRadius = function() {
            var self = this;

            this.mode = MODE_REGION_SELECTION_MODE_POINT;
            this.$node.find('.instructions').remove();
            this.$node.append(centerTemplate({}));
            $(document).on('keydown.regionselection', function(e) {
                if (e.which === $.ui.keyCode.ESCAPE) {
                    self.endRegionSelection();
                }
            });
        };

        this.endRegionSelection = function() {
            this.mode = MODE_NORMAL;

            this.off('mousemove');
            $('#map_mouse_position_hack').remove();
            this.$node.find('.instructions').remove();

            if (this.regionLayer) {
                this.mapReady(function(map) {
                    map.removeLayer(this.regionLayer);
                });
            }

            $(document).off('keydown.regionselection');
        };

        this.onMapClicked = function(evt, map) {
            var self = this;
            this.$node.find('.instructions').remove();

            switch (self.mode) {
                case MODE_NORMAL:
                    self.trigger('verticesSelected', []);
                    map.featuresLayer.events.triggerEvent('featureunselected');
                    break;

                case MODE_REGION_SELECTION_MODE_POINT:

                    self.mode = MODE_REGION_SELECTION_MODE_RADIUS;

                    this.$node.append(radiusTemplate({}));

                    var offset = self.$node.offset();
                    self.regionCenterPoint = map.getLonLatFromViewPortPx({x:evt.pageX-offset.left,y:evt.pageY-offset.top});
                    var centerPoint = new ol.Geometry.Point(self.regionCenterPoint.lon, self.regionCenterPoint.lat);

                    var circleFeature = new ol.Feature.Vector(
                            OpenLayers.Geometry.Polygon.createRegularPolygon(
                                centerPoint,
                                // Default diameter is 10% of viewport
                                map.getExtent().getWidth() * 0.1 / 2,
                                30,
                                0
                            ),
                            {},
                            { fillOpacity: 0.8, fillColor:'#0070C3', strokeColor:'#08538B' }
                        ),
                        layer = new ol.Layer.Vector('SelectionLayer', {
                            /*
                             * TODO: change resize handle colors
                            styleMap: new ol.StyleMap({
                                'default': new ol.Style({ fillColor: '#ff0000'}),
                                'select': new ol.Style({ fillColor: '#ff0000'})
                            })
                            */
                        });

                    self.regionLayer = layer;
                    self.regionFeature = circleFeature;

                    layer.addFeatures(circleFeature);
                    map.addLayer(layer);

                    var modify = new ol.Control.ModifyFeature(layer);
                    modify.mode = ol.Control.ModifyFeature.RESIZE | 
                                  ol.Control.ModifyFeature.DRAG;
                    map.addControl(modify);
                    modify.activate();
                    modify.selectFeature(circleFeature);

                    break;

                case MODE_REGION_SELECTION_MODE_RADIUS:

                    self.mode = MODE_REGION_SELECTION_MODE_LOADING;

                    var area = self.regionFeature.geometry.getArea();
                    var radius = 0.565352 * Math.sqrt(area);
                    var lonlat = self.regionCenterPoint.transform(map.getProjectionObject(), new ol.Projection("EPSG:4326"));

                    self.$node.find('.instructions').remove();
                    self.$node.append(loadingTemplate({}));

                    self.ucdService.locationSearch(
                        lonlat.lat,
                        lonlat.lon,
                        radius).done(
                        function(data) {
                            self.endRegionSelection();
                            self.trigger(document, 'addVertices', data);
                        }
                    );

                    break;

            }
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
                controls = new ol.Control.Navigation({
                    handleRightClicks: true,
                    dragPanOptions: {
                        enableKinetic: true
                    }
                }),
                map = new ol.Map('map', { 
                    zoomDuration: 0,
                    numZoomLevels: 18,
                    displayProjection: new ol.Projection("EPSG:4326"),
                    controls: [ controls ]
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
            this.clusterStrategy = cluster;

            // Feature Selection
            var selectFeature = this.featuresLayerSelection = new ol.Control.SelectFeature(map.featuresLayer, {
                clickout: true
            });
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
            map.events.on({
                mouseup: function(event) {
                    if (event.button === 2 || event.ctrlKey) {
                        self.handleContextMenu(event);
                    }
                },
                click: function(event) {
                    self.closeMenu();
                    self.onMapClicked(event, map);
                }
            });

            map.addLayers([base, map.featuresLayer]);

            latLon = latLon.bind(null, map.displayProjection, map.getProjectionObject());
            point = point.bind(null, map.displayProjection, map.getProjectionObject());

            map.setCenter(latLon(START_COORDINATES), 7);

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

