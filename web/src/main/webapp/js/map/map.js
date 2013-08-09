

define([
    'flight/lib/component',
    'tpl!./map',
    'tpl!./instructions/regionCenter',
    'tpl!./instructions/regionRadius',
    'tpl!./instructions/regionLoading',
    'service/ucd',
    'util/retina',
    'util/withContextMenu',
    'underscore'
], function(defineComponent, template, centerTemplate, radiusTemplate, loadingTemplate, UcdService, retina, withContextMenu, _) {
    'use strict';
            
    var MODE_NORMAL = 0,
        MODE_REGION_SELECTION_MODE_POINT = 1,
        MODE_REGION_SELECTION_MODE_RADIUS = 2,
        MODE_REGION_SELECTION_MODE_LOADING = 3;

    return defineComponent(Map, withContextMenu);

    function Map() {
        var callbackQueue = [];

        this.ucdService = new UcdService();
        this.mode = MODE_NORMAL;

        this.defaultAttrs({
            mapSelector: '#map'
        });

        this.after('initialize', function() {
            this.$node.html(template({}));


            this.registerForContextMenuEvent();

            this.on(document, 'mapShow', this.onMapShow);
            this.on(document, 'mapCenter', this.onMapCenter);
            this.on(document, 'mapEndPan', this.onMapEndPan);
            this.on(document, 'mapEndZoom', this.onMapEndPan);
            this.on(document, 'mapUpdateBoundingBox', this.onMapUpdateBoundingBox);
            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on(document, 'nodesAdded', this.onNodesAdded);
            this.on(document, 'nodesUpdated', this.onNodesUpdated);
            this.on(document, 'nodesDeleted', this.onNodesDeleted);
            this.on(document, 'windowResize', this.onMapEndPan);
            this.on(document, 'syncEnded', this.onSyncEnded);
        });

        this.map = function(callback) {
            if ( this.mapLoaded ) {
                callback.call(this, this._map);
            } else {
                callbackQueue.push( callback );
            }
        };

        this.registerForContextMenuEvent = function() {

            this.map(function(map) {
                var self = this;
                if (map.api == 'googlev3') {
                    google.maps.event.addListener(map.getMap(), 'rightclick', function(e) {
                        self.toggleMenu({
                            positionInNode:e.pixel
                        });
                    });
                } else {
                    this.on('contextmenu', function(event) {
                        self.toggleMenu({ positionUsingEvent:event });
                    });
                }
            });
        };

        this.onContextMenuLoadResultsWithinRadius = function() {
            var self = this;

            this.mode = MODE_REGION_SELECTION_MODE_POINT;
            this.$node.append(centerTemplate({}));
            $(document).on('keydown.regionselection', function(e) {
                if (e.which === $.ui.keyCode.ESCAPE) {
                    self.endRegionSelection();
                }
            });
        };

        this.onSyncEnded = function() {
            this.map(function(map) {
                if (this.syncPolyline) {
                    map.removePolyline(this.syncPolyline);
                    this.syncPolyline = null;
                }
                if (this.syncNameMarker) {
                    map.removeMarker(this.syncNameMarker);
                    this.syncNameMarker = null;
                }
            });
        };


        this.onWorkspaceLoaded = function(evt, workspaceData) {
            var self = this;

            this.map(function(map) {
                map.removeAllMarkers();

                if (workspaceData.data === undefined || workspaceData.data.nodes === undefined) {
                    return;
                }
                workspaceData.data.nodes.forEach(function(node) {
                    if(node.location || node.locations) {
                        self.updateOrAddNode(node);
                    }
                });
            });
        };

        this.onNodesAdded = function(evt, data) {
            var self = this;
            data.nodes.forEach(function(node) {
                self.updateOrAddNode(node);
                self.updateNodeLocation(node);
            });
        };

        this.updateOrAddNode = function(node) {
            var self = this;
            if(!node.location && !node.locations) {
                return;
            }

            this.map(function(map) {
                this.deleteNode(node);

                var locations = $.isArray(node.locations) ? node.locations : [ node.location ];

                locations.forEach(function(location) {
                    var pt = new mxn.LatLonPoint(location.latitude, location.longitude);
                    var marker = new mxn.Marker(pt);
                    marker.setAttribute('rowKey', node.rowKey);
                    if (retina.devicePixelRatio > 1) {
                        marker.setIcon('/img/small_pin@2x.png', [26, 52], [13, 52]);
                    } else {
                        marker.setIcon('/img/small_pin.png', [13, 26], [6,26]);
                    }
                    marker.click.addHandler(function(eventType, marker) {
                        self.trigger(document, 'searchResultSelected', [ node ]);
                    });
                    map.addMarker(marker);
                });

                if (locations.length) {
                    self.fit(map);
                }
            });
        };

        this.onNodesUpdated = function(evt, data) {
            var self = this;
            data.nodes.forEach(function(node) {
                self.updateOrAddNode(node);
            });
        };

        this.onNodesDeleted = function(evt, data) {
            var self = this;
            data.nodes.forEach(function(node) {
                self.deleteNode(node);
            });

            this.fit();
        };

        this.fit = function(map) {
            if (this.$node.is(':visible')) {

                var _fit = function(map) {
                    map.autoCenterAndZoom();
                    if ( map.getZoom() > 10 ) {
                        map.setZoom(10);
                    }
                }

                if (map) _fit(map);
                else this.map(_fit);
            }
        };


        this.deleteNode = function(node) {
            var self = this;

            this.map(function(map) {
                map.markers
                    .filter(function(marker) {
                        return marker.getAttribute('rowKey') == node.rowKey;
                    })
                    .forEach(function(marker) {
                        map.removeMarker(marker);
                    });
            });
        };

        this.updateNodeLocation = function(node) {
            var self = this;
            if(node.type == 'artifact') {
                this.ucdService.getArtifactById(node.rowKey || node['_rowKey'], function(err, artifact) {
                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }

                    if(artifact && artifact.Dynamic_Metadata && artifact.Dynamic_Metadata.latitude && artifact.Dynamic_Metadata.longitude) {

                        node.location = {
                            latitude: artifact.Dynamic_Metadata.latitude,
                            longitude: artifact.Dynamic_Metadata.longitude
                        };

                        var nodesUpdateData = {
                            nodes: [node]
                        };
                        self.trigger(document, 'updateNodes', nodesUpdateData);
                    } else {
                        self.invalidMap();
                    }
                });
            } else {
                this.ucdService.getGraphNodeById(node.graphNodeId, function(err, entity) {
                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }
                    var locations = [];

                    Object.keys(entity).forEach(function(entityKey) {
                        var mention = entity[entityKey];
                        if(mention.latitude && mention.latitude) {
                            if(locations.filter(function(l) { return (mention.latitude == l.latitude) && (mention.longitude == l.longitude); }).length == 0) {
                                locations.push({
                                    latitude: mention.latitude,
                                    longitude: mention.longitude
                                });
                            }
                        }
                    });

                    node.locations = locations;
                    if (locations.length === 0) {
                        self.invalidMap();
                    }

                    var nodesUpdateData = {
                        nodes: [node]
                    };
                    self.trigger(document, 'updateNodes', nodesUpdateData);
                });
            }
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

        this.onMapEndPan = function(evt, mapCenter) {
            this.map(function(map) {
                var boundingBox = map.getBounds();
                var boundingBoxData = {
                    swlat: boundingBox.getSouthWest().lat,
                    swlon: boundingBox.getSouthWest().lon,
                    nelat: boundingBox.getNorthEast().lat,
                    nelon: boundingBox.getNorthEast().lon
                };
                this.trigger(document, 'mapUpdateBoundingBox', boundingBoxData);
            });
        };

        this.onMapCenter = function(evt, data) {
            this.map(function(map) {
                var latlon = new mxn.LatLonPoint(data.latitude, data.longitude);
                map.setCenterAndZoom(latlon, 7);
            });
        };

        this.onMapUpdateBoundingBox = function(evt, data) {
            if (!data.remoteEvent) {
                return;
            }

            this.map(function(map) {
                var points = [];
                points.push(new mxn.LatLonPoint(data.swlat, data.swlon));
                points.push(new mxn.LatLonPoint(data.nelat, data.swlon));
                points.push(new mxn.LatLonPoint(data.nelat, data.nelon));
                points.push(new mxn.LatLonPoint(data.swlat, data.nelon));
                points.push(new mxn.LatLonPoint(data.swlat, data.swlon));

                var polyline = new mxn.Polyline(points);
                polyline.setColor('#909090');

                if (this.syncPolyline) {
                    map.removePolyline(this.syncPolyline);
                }
                if (this.syncNameMarker) {
                    map.removeMarker(this.syncNameMarker);
                }
                
                var imageSize = [0,0];
                var imageUrl = this.cachedRenderName(data.remoteInitiator, imageSize, 
                    polyline.color, '#fff', 'rgba(0,0,0,0.5)');
                if ( imageUrl ) {
                    // Place in bottom left corner and move to make even with
                    // border
                    this.syncNameMarker = new mxn.Marker(points[0]);
                    this.syncNameMarker.setIcon(imageUrl, imageSize, [2,0]);
                    map.addMarker(this.syncNameMarker);
                } else console.warn('Unable to create name marker');

                map.addPolyline(polyline);
                this.syncPolyline = polyline;
            });
        };

        this.fixSize = function() {
            this.map(function(map) {
                var mapEl = this.select('mapSelector');
                map.resizeTo(mapEl.width(), mapEl.height());
            });
        };

        this.onMapShow = function(evt) {
            var self = this;
            if (!this.timeout && !this.mapLoaded) {
                this.timeout = setTimeout(this.initializeMap.bind(this), 100);
            }
        };

        this.endRegionSelection = function() {
            this.mode = MODE_NORMAL;

            this.off('mousemove');
            $('#map_mouse_position_hack').remove();
            this.$node.find('.instructions').remove();

            if (this.regionPolyline) {
                this.map(function(map) {
                    map.removePolyline(this.regionPolyline);
                });
            }

            $(document).off('keydown.regionselection');
        };

        this.onMapClicked = function(evt, map, data) {
            var self = this;
            this.$node.find('.instructions').remove();

            switch (self.mode) {
                case MODE_NORMAL: 
                    self.trigger(document, 'searchResultSelected', []);
                    break;

                case MODE_REGION_SELECTION_MODE_POINT:

                    self.mode = MODE_REGION_SELECTION_MODE_RADIUS;

                    this.$node.append(radiusTemplate({}));

                    self.regionCenterPoint = data.location;

                    var span = $('<span id="map_mouse_position_hack"></span>').hide().appendTo(document.body),
                        radius = new mxn.Radius(self.regionCenterPoint, 10);

                    // Register for mouse events using the hack. 
                    // mapstraction doesn't have a mousemove handler!?! so we
                    // just grab the text of the span content: "lat / lon"
                    map.mousePosition(span.attr('id'));

                    self.on('mousemove', function() {
                       var parts = span.text().split(/\s*\/\s*/);
                       if (parts.length === 2) {
                           var point = new mxn.LatLonPoint(parts[0], parts[1]);

                           if (self.regionPolyline) {
                               map.removePolyline(self.regionPolyline);
                           }

                           self.regionRadiusDistance = Math.max(1, self.regionCenterPoint.distance(point) * 0.95);
                           self.regionPolyline = radius.getPolyline(self.regionRadiusDistance, '#0000aa');
                           self.regionPolyline.addData({
                               fillColor: '#000088',
                               opacity: 0.2,
                               closed: true
                           });

                           map.addPolyline(self.regionPolyline);

                           // Google maps fix to disable events on polyline
                           if (map.api === 'googlev3' && self.regionPolyline.proprietary_polyline) {
                               self.regionPolyline.proprietary_polyline.setOptions( { clickable: false } );
                           }
                       }
                    });

                    break;

                case MODE_REGION_SELECTION_MODE_RADIUS:

                    self.mode = MODE_REGION_SELECTION_MODE_LOADING;
                    self.off('mousemove');
                    self.$node.find('.instructions').remove();
                    self.$node.append(loadingTemplate({}));

                    self.ucdService.locationSearch(
                        self.regionCenterPoint.lat,
                        self.regionCenterPoint.lon, 
                        self.regionRadiusDistance,
                        function(err, data) {
                            self.endRegionSelection();
                            if (!err) {
                                self.trigger(document, 'addNodes', data);
                            }
                        }
                    );

                    break;

            }
        };

        this.initializeMap = function() {
            delete this.initializeMap;

            var self = this;
            var map = new mxn.Mapstraction('map', document.mapProvider);

            this.drainCallbackQueue = function() {
                var self = this;
                callbackQueue.forEach(function( callback ) {
                    callback.call(self, map); 
                });
                callbackQueue.length = 0;
            };

            // Map Handlers
            map.load.addHandler(function() {
                self._map = map;
                self.mapLoaded = true;
                self.drainCallbackQueue();
            });
            map.endPan.addHandler(function() {
                var center = map.getCenter();
                self.trigger(document, 'mapEndPan', {
                    lat: center.lat,
                    lng: center.lng
                });
            });
            map.changeZoom.addHandler(function() {
                self.trigger(document, 'mapEndZoom');
            });
            map.click.addHandler(function(event, mapmxn, data) {
                self.onMapClicked(event, map, data);
            });


            if(document.mapProvider == 'leaflet') {
                map.addTileLayer("/map/{z}/{x}/{y}.png", {
                    name: "Roads"
                });
            }

            // TODO: persist the map location in workspace
            var latlon = new mxn.LatLonPoint(38.89,-77.03);
            map.setCenterAndZoom(latlon, 7);
            map.enableScrollWheelZoom();

            this.fixSize();
        };


        this.cachedRenderName = function(name, imageSize) {
            if ( ! this.cachedNames ) {
                this.cachedNames = [];
                this.cachedSizes = [];
            }

            var url = this.cachedNames[ name ];
            if ( ! url ) {
                url = this.renderNameToDataUrl.apply(this, arguments);
                this.cachedNames[ name ] = url;
                this.cachedSizes[ name ] = imageSize;
            }

            imageSize[0] = this.cachedSizes[ name ][0];
            imageSize[1] = this.cachedSizes[ name ][1];

            return url;
        };

        // Map only allows adding an image overlay, so create one
        // using canvas and dataURL's
        this.renderNameToDataUrl = function(name, imageSize, backgroundColor, fontColor, shadowColor) {

            var FONT_SIZE = 14,
                PADDING = 10,
                HEIGHT = FONT_SIZE * 1.5,
                TEXT_BASELINE = HEIGHT * 0.7,
                canvas = document.createElement('canvas');

            canvas.width = 200;
            canvas.height = HEIGHT;
            if ( ! canvas ) return console.warn('Unable to create canvas object for name label');
            if ( ! canvas.toDataURL ) return console.warn('Unable to retrieve canvas contents as data url');

            var ctx = canvas.getContext('2d');
            if ( ! ctx ) return console.warn('Unable to get canvas context for name label');

            function render() {
                function setup() {
                    ctx.clearRect(0,0,canvas.width,canvas.height);
                    ctx.font = FONT_SIZE + 'px HelveticaNeue, Arial';
                }
                setup();
                var size = ctx.measureText(name);
                canvas.width = size.width + PADDING * 2;
                imageSize[0] = canvas.width;
                imageSize[1] = canvas.height;
                setup();

                // Background
                ctx.fillStyle = backgroundColor || '#000';
                ctx.fillRect(0,0,canvas.width, canvas.height);

                // Text Shadow
                if (shadowColor) {
                    ctx.fillStyle = shadowColor;
                    ctx.fillText(name, PADDING, TEXT_BASELINE - 1);
                }

                // Text
                ctx.fillStyle = fontColor || '#fff';
                ctx.fillText(name, PADDING, TEXT_BASELINE);

                return canvas.toDataURL();
            }

            return render();
        };
    }
});
