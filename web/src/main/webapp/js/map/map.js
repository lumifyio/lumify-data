

define([
    'flight/lib/component',
    'tpl!./map',
    'tpl!./instructions/regionCenter',
    'tpl!./instructions/regionRadius',
    'tpl!./instructions/regionLoading',
    'service/ucd',
    'util/retina',
    'util/withContextMenu'
], function(defineComponent, template, centerTemplate, radiusTemplate, loadingTemplate, UcdService, retina, withContextMenu) {
    'use strict';

    var MODE_NORMAL = 0,
        MODE_REGION_SELECTION_MODE_POINT = 1,
        MODE_REGION_SELECTION_MODE_RADIUS = 2,
        MODE_REGION_SELECTION_MODE_LOADING = 3;

    return defineComponent(MapView, withContextMenu);

    function MapView() {
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
            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on(document, 'verticesAdded', this.onVerticesAdded);
            this.on(document, 'verticesUpdated', this.onVerticesUpdated);
            this.on(document, 'verticesDeleted', this.onVerticesDeleted);
            this.on(document, 'syncEnded', this.onSyncEnded);
            this.on(document, 'existingVerticesAdded', this.onExistingVerticesAdded);
            this.on(document, 'socketMessage', this.onSocketMessage);
        });

        this.onSocketMessage = function (evt, message) {
            var self = this;
            switch (message.type) {
                case 'propertiesChange':
                    for(var i=0; i<message.data.properties.length; i++) {
                        var propertyChangeData = message.data.properties[i];
                        self.onPropertyChange(propertyChangeData);
                    }
                    break;
            }
        };

        this.onPropertyChange = function(propertyChangeData) {
            var self = this;

            if(propertyChangeData.propertyName == 'geoLocation') {
                var m = propertyChangeData.value.match(/point\[(.*?),(.*?)\]/);
                if(!m) {
                    return;
                }
                var latitude = m[1];
                var longitude = m[2];

                this.map(function(map) {
                    var markers = map.markers
                        .filter(function(marker) {
                            return marker.getAttribute('graphVertexId') == propertyChangeData.graphVertexId;
                        });
                    markers.forEach(function(marker) {
                        var pt = new mxn.LatLonPoint(latitude, longitude);
                        if (map.api == 'googlev3') {
                            var p = pt.toProprietary('googlev3');
                            marker.proprietary_marker.setPosition(p);
                        }
                    });
                });
            }

            if(propertyChangeData.propertyName == 'heading') {
                this.map(function(map) {
                    var markers = map.markers
                        .filter(function(marker) {
                            return marker.getAttribute('graphVertexId') == propertyChangeData.graphVertexId;
                        });
                    markers.forEach(function(marker) {
                        self.updateMarkerIcon(marker, propertyChangeData.value);
                    });
                });
            }
        };

        this.map = function(callback) {
            if ( this.mapLoaded ) {
                callback.call(this, this._map);
            } else {
                callbackQueue.push( callback );
            }
        };

        this.locationToPixels = function(map, point) {
            var width = this.$node.width(),
                height = this.$node.height(),
                bounds = map.getBounds(),
                swLat = bounds.sw.latConv(),
                swLon = bounds.sw.lonConv(),
                neLat = bounds.ne.latConv(),
                neLon = bounds.ne.lonConv();

            var crossesMeridian = bounds.sw.lon > bounds.ne.lon;

            return {

                // If the bounds spans the 180° meridian fix the span
                left: Math.round(
                    width * (
                        crossesMeridian ?

                        ((180 - bounds.sw.lon) + (point.lon + 180)) /
                        ((180 - bounds.sw.lon) + (bounds.ne.lon + 180)) :

                        (point.lon - bounds.sw.lon) / (bounds.ne.lon - bounds.sw.lon)
                    )
                ),

                // Not an exact latitude -> y calculation for mercador but close enough
                top: Math.round(
                    height * (
                        1 - (point.lat - bounds.sw.lat) / (bounds.ne.lat - bounds.sw.lat)
                    )
                )
            };
        };

        this.registerForContextMenuEvent = function() {

            this.map(function(map) {
                var self = this;
                if (map.api == 'googlev3') {
                    google.maps.event.addListener(map.getMap(), 'rightclick', function(e) {
                        self.toggleMenu({
                            positionInVertex:e.pixel
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

        this.onExistingVerticesAdded = function(evt, data) {
            var self = this;
            if (this.$node.closest('.visible').length === 0) return;

            var dragging = $('.ui-draggable-dragging:not(.clone-vertex)'),
                position = dragging.position(),
                mapOffset = this.$node.offset(),
                offset = dragging.offset(),
                graphOffset = this.$node.offset();

            if (dragging.length != 1) return;

            this.map(function(map) {

                var points = [];
                map.markers.forEach(function(marker) {
                    window.marker = marker;
                    data.vertices.forEach(function(vertex) {
                        if (marker.getAttribute('graphVertexId') === vertex.graphVertexId) {
                            points.push(marker.location);
                        }
                    });
                });

                if (points.length === 0) {
                    self.invalidMap ();
                    return;
                }

                var cloned = dragging.clone()
                                 .css({width:'auto'})
                                 .addClass('clone-vertex')
                                 .insertAfter(dragging);

                if (!map.getBounds().contains(points[0]) || map.getZoom() < 3) {
                    var zoom = map.getZoom();
                    map.centerAndZoomOnPoints(points);
                    var afterZoom = map.getZoom();
                    map.setZoom(Math.max(3, afterZoom > zoom ? zoom : afterZoom));
                }

                _.delay(function() {
                    var p = this.locationToPixels(map, points[0]);

                    // Adjust rendered position to page coordinate system
                    p.left += mapOffset.left;
                    p.top += mapOffset.top;

                    // Move draggable coordinates from top/left to center
                    offset.left += cloned.outerWidth(true) / 2;
                    offset.top += cloned.outerHeight(true) / 2;

                    cloned
                        .animate({
                            left: (position.left + (p.left-offset.left))  + 'px',
                            top: (position.top +  (p.top-offset.top)) + 'px'
                        }, {
                            complete: function() {
                                cloned.addClass('shrink');
                                _.delay(function() {
                                    cloned.remove();
                                }, 1000);
                            }
                        });
                }.bind(this), 100);
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

                if (workspaceData.data === undefined || workspaceData.data.vertices === undefined) {
                    return;
                }
                workspaceData.data.vertices.forEach(function(vertex) {
                    if(vertex.location || vertex.locations) {
                        self.updateOrAddVertex(vertex);
                    }
                });
            });
        };

        this.onVerticesAdded = function(evt, data) {
            var self = this;
            data.vertices.forEach(function(vertex) {
                self.updateOrAddVertex(vertex);
                self.updateVertexLocation(vertex);
            });
        };

        this.updateOrAddVertex = function(vertex) {
            var self = this;
            if(!vertex.location && !vertex.locations) {
                return;
            }

            this.map(function(map) {
                this.deleteVertex(vertex);

                var locations = $.isArray(vertex.locations) ? vertex.locations : [ vertex.location ];

                locations.forEach(function(location) {
                    var pt = new mxn.LatLonPoint(location.latitude, location.longitude);
                    var marker = new mxn.Marker(pt);
                    marker.setAttribute('graphVertexId', vertex.graphVertexId);
                    marker.setAttribute('subType', vertex._subType);

                    self.updateMarkerIcon(marker, vertex.heading);

                    marker.click.addHandler(function(eventType, marker) {
                        self.trigger('verticesSelected', [ vertex ]);
                    });
                    map.addMarker(marker);
                });

                if (locations.length) {
                    self.fit(map);
                }
            });
        };

        this.updateMarkerIcon = function(marker, heading) {
            var subType = marker.getAttribute('subType');
            var additionalParameters = '';
            if(heading) {
                additionalParameters += '&heading=' + heading;
            }
            var iconUrl = '/map/marker/' + subType + '/image';
            if (retina.devicePixelRatio > 1) {
                marker.setIcon(iconUrl + '?scale=2' + additionalParameters, [44, 80], [22, 80]);
            } else {
                marker.setIcon(iconUrl + '?scale=1' + additionalParameters, [22, 40], [11, 20]);
            }
        };

        this.onVerticesUpdated = function(evt, data) {
            var self = this;
            data.vertices.forEach(function(vertex) {
                self.updateOrAddVertex(vertex);
            });
        };

        this.onVerticesDeleted = function(evt, data) {
            var self = this;
            data.vertices.forEach(function(vertex) {
                self.deleteVertex(vertex);
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


        this.deleteVertex = function(vertex) {
            var self = this;

            this.map(function(map) {
                map.markers
                    .filter(function(marker) {
                        return marker.getAttribute('graphVertexId') == vertex.graphVertexId;
                    })
                    .forEach(function(marker) {
                        map.removeMarker(marker);
                    });
            });
        };

        this.updateVertexLocation = function(vertex) {
            var self = this;
            if(vertex._type == 'artifact') {
                this.ucdService.getArtifactById(vertex._rowKey, function(err, artifact) {
                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }

                    if(artifact && artifact.Dynamic_Metadata && artifact.Dynamic_Metadata.latitude && artifact.Dynamic_Metadata.longitude) {

                        vertex.location = {
                            latitude: artifact.Dynamic_Metadata.latitude,
                            longitude: artifact.Dynamic_Metadata.longitude
                        };

                        var verticesUpdateData = {
                            vertices: [vertex]
                        };
                        self.trigger(document, 'updateVertices', verticesUpdateData);
                    } else {
                        self.invalidMap();
                    }
                });
            } else {
                this.ucdService.getVertexProperties(vertex.graphVertexId, function(err, entity) {
                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }
                    var locations = [];

                    if(entity.properties.geoLocation) {
                        var m = entity.properties.geoLocation.match(/point\[(.*?),(.*)\]/);
                        if(m){
                            var latitude = m[1];
                            var longitude = m[2];
                            locations.push({
                                latitude: latitude,
                                longitude: longitude
                            });
                        }
                    }

                    vertex.locations = locations;
                    if (locations.length === 0) {
                        self.invalidMap();
                    }

                    var verticesUpdateData = {
                        vertices: [vertex]
                    };
                    self.trigger(document, 'updateVertices', verticesUpdateData);
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

        this.onMapCenter = function(evt, data) {
            this.map(function(map) {
                var latlon = new mxn.LatLonPoint(data.latitude, data.longitude);
                map.setCenterAndZoom(latlon, 7);
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

            if (this.mapLoaded) {
                setTimeout(function() {
                    console.log('fixing');
                    this.fixSize();
                }.bind(this), 250);
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
                    self.trigger('verticesSelected', []);
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
                                self.trigger(document, 'addVertices', data);
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
