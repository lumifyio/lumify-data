

define([
    'flight/lib/component',
    'tpl!./map',
    'service/ucd'
], function(defineComponent, template, UcdService) {
    'use strict';

    return defineComponent(Map);

    function Map() {
        var callbackQueue = [];

        this.ucdService = new UcdService();

        this.defaultAttrs({
            mapSelector: '#map'
        });

        this.after('initialize', function() {
            this.$node.html(template({}));

            this.on(document, 'mapShow', this.onMapShow);
            this.on(document, 'mapCenter', this.onMapCenter);
            this.on(document, 'mapEndPan', this.onMapEndPan);
            this.on(document, 'mapEndZoom', this.onMapEndPan);
            this.on(document, 'mapUpdateBoundingBox', this.onMapUpdateBoundingBox);
            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on(document, 'nodesAdd', this.onNodesAdd);
            this.on(document, 'nodesUpdate', this.onNodesUpdate);
            this.on(document, 'nodesDelete', this.onNodesDelete);
            this.on(document, 'windowResize', this.onMapEndPan);
        });

        this.map = function(callback) {
            if ( this.mapLoaded ) {
                callback.call(this, this._map);
                //this.drainCallbackQueue();
            } else {
                callbackQueue.push( callback );
            }
        };


        this.onWorkspaceLoaded = function(evt, workspaceData) {
            var self = this;
            if (workspaceData.data === undefined || workspaceData.data.nodes === undefined) {
                return;
            }
            workspaceData.data.nodes.forEach(function(node) {
                if(node.location || node.locations) {
                    self.updateOrAddNode(node);
                }
                self.updateNodeLocation(node);
            });
        };

        this.onNodesAdd = function(evt, data) {
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

                var locations;
                if(node.locations) {
                    locations = node.locations;
                } else {
                    locations = [ node.location ];
                }

                locations.forEach(function(location) {
                    var pt = new mxn.LatLonPoint(location.latitude, location.longitude);
                    var marker = new mxn.Marker(pt);
                    marker.setAttribute('rowKey', node.rowKey);
                    // TODO: fix weird shadow and can't close
                    //marker.setInfoBubble(node.rowKey);
                    //marker.click.addHandler(function() {
                        // marker.openBubble();
                    //});
                    map.addMarker(marker);
                });
            });
        };

        this.onNodesUpdate = function(evt, data) {
            var self = this;
            data.nodes.forEach(function(node) {
                self.updateOrAddNode(node);
            });
        };

        this.onNodesDelete = function(evt, data) {
            var self = this;
            data.nodes.forEach(function(node) {
                self.deleteNode(node);
            });
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
            if(node.type == 'entities') {
                this.ucdService.getEntityById(node.rowKey, function(err, entity) {
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

                    var nodesUpdateData = {
                        nodes: [{
                            rowKey: node.rowKey,
                            locations: locations
                        }]
                    };
                    self.trigger(document, 'nodesUpdate', nodesUpdateData);
                });
            } else if(node.type == 'artifacts') {
                this.ucdService.getArtifactById(node.rowKey, function(err, artifact) {
                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }

                    if(artifact && artifact.Dynamic_Metadata && artifact.Dynamic_Metadata.latitude && artifact.Dynamic_Metadata.longitude) {
                        var nodesUpdateData = {
                            nodes: [{
                                rowKey: node.rowKey,
                                location: {
                                    latitude: artifact.Dynamic_Metadata.latitude,
                                    longitude: artifact.Dynamic_Metadata.longitude
                                }
                            }]
                        };
                        self.trigger(document, 'nodesUpdate', nodesUpdateData);
                    }
                });
            } else {
                console.error("Unknown node type:", node.type);
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
                this.trigger(document, 'modeSelect', { mode: 'map' });
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

        this.onMapShow = function(evt, workspaceData) {
            var self = this;
            if (!this.timeout && !this.mapLoaded) {
                this.timeout = setTimeout(this.initializeMap.bind(this, workspaceData), 100);
            } else {
                workspaceData.data.nodes.forEach(function(node) {
                    self.updateOrAddNode(node);
                });
            }
        };

        this.initializeMap = function(workspaceData) {
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


            if(document.mapProvider == 'leaflet') {
                map.addTileLayer("/map/{z}/{x}/{y}.png", {
                    name: "Roads"
                });
            }

            var latlon = new mxn.LatLonPoint(38.89,-77.03);
            map.setCenterAndZoom(latlon, 7);
            map.enableScrollWheelZoom();

            this.fixSize();
            // TODO: Is this necessary?
            //var self = this;
            //workspaceData.data.nodes.forEach(function(node) {
                //self.updateOrAddNode(node);
            //});
        };
    }
});
