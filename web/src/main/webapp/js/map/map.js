

define([
    'flight/lib/component',
    'tpl!./map',
    'service/ucd'
], function(defineComponent, template, UcdService) {
    'use strict';

    return defineComponent(Map);

    function Map() {
        this.ucdService = new UcdService();

        this.defaultAttrs({
            mapSelector: '#map'
        });

        this.after('initialize', function() {
            var self = this;
            this.$node.html(template({}));

            this.on(document, 'mapShow', this.onMapShow);
            this.on(document, 'mapCenter', this.onMapCenter);
            this.on(document, 'mapEndPan', this.onMapEndPan);
            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on(document, 'graphAddNode', this.onGraphAddNode);
            this.on(document, 'nodeUpdate', this.onNodeUpdate);
        });

        this.onWorkspaceLoaded = function(evt, workspaceData) {
            var self = this;
            workspaceData.data.nodes.forEach(function(node) {
                if(node.location || node.locations) {
                    self.updateOrAddNode(node);
                }
                self.updateNodeLocation(node);
            });
        };

        this.onGraphAddNode = function(evt, graphNodeData) {
            console.log('graphNodeData', graphNodeData);
        };

        this.updateOrAddNode = function(node) {
            var self = this;
            if(!self.map) {
                return;
            }
            if(!node.location && !node.locations) {
                return;
            }

            this.map.markers
                .filter(function(marker) {
                    return marker.getAttribute('rowKey') == node.rowKey;
                })
                .forEach(function(marker) {
                    self.map.removeMarker(marker);
                });

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
                marker.setInfoBubble(node.rowKey);
                marker.click.addHandler(function() {
                    marker.openBubble();
                });
                self.map.addMarker(marker);
            });
        };

        this.onNodeUpdate = function(evt, node) {
            this.updateOrAddNode(node);
        };

        this.updateNodeLocation = function(node) {
            var self = this;
            if(node.type == 'entities') {
                this.ucdService.getEntityById(node.rowKey, function(err, entity) {
                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }
                    console.log('entity', entity);
                });
            } else if(node.type == 'artifacts') {
                this.ucdService.getArtifactById(node.rowKey, function(err, artifact) {
                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }
                    if(artifact && artifact.Dynamic_Metadata && artifact.Dynamic_Metadata.latitude && artifact.Dynamic_Metadata.longitude) {
                        self.trigger(document, 'nodeUpdate', {
                            rowKey: node.rowKey,
                            location: {
                                latitude: artifact.Dynamic_Metadata.latitude,
                                longitude: artifact.Dynamic_Metadata.longitude
                            }
                        });
                    }
                });
            } else {
                console.error("Unknown node type:", node.type);
            }
        };

        this.onMapEndPan = function(evt, mapCenter) {
            var self = this;
            if(self.lastMarker) {
                self.lastMarker.closeBubble();
                self.map.removeMarker(self.lastMarker);
            }
            var pt = new mxn.LatLonPoint(mapCenter.lat, mapCenter.lng);
            console.log('onMapEndPan', pt);
            self.lastMarker = new mxn.Marker(pt);
            self.lastMarker.setInfoBubble("User");
            self.lastMarker.click.addHandler(function() {
                self.lastMarker.openBubble();
            });
            self.map.addMarker(self.lastMarker);
        }

        this.onMapCenter = function(evt, data) {
            console.log(data);
            this.trigger(document, 'modeSelect', { mode: 'map' });
            var latlon = new mxn.LatLonPoint(data.latitude, data.longitude);
            this.map.setCenterAndZoom(latlon, 7);
        };

        this.fixSize = function() {
            if (this.map) {
                var map = this.select('mapSelector');
                this.map.resizeTo(map.width(), map.height());
            }
        };

        this.onMapShow = function(evt, workspaceData) {
            var self = this;
            if (!this.timeout && !this.mapInitialized) {
                this.timeout = setTimeout(this.initializeMap.bind(this, workspaceData), 100);
            } else {
                workspaceData.data.nodes.forEach(function(node) {
                    self.updateOrAddNode(node);
                });
            }
        };

        this.initializeMap = function(workspaceData) {
            this.fixSize();

            var self = this;

            if(self.mapInitialized) {
                return;
            }

            self.map = new mxn.Mapstraction('map', document.mapProvider);

            if(document.mapProvider == 'leaflet') {
                self.map.addTileLayer("/map/{z}/{x}/{y}.png", {
                    name: "Roads"
                });
            }

            var latlon = new mxn.LatLonPoint(38.89,-77.03);
            self.map.setCenterAndZoom(latlon, 7);
            self.map.enableScrollWheelZoom();
            //self.map.addSmallControls();
            self.map.endPan.addHandler(function() {
                var center = self.map.getCenter();
                self.trigger(document, 'mapEndPan', {
                    lat: center.lat,
                    lng: center.lng
                });
            });
            self.mapInitialized = true;
            self.fixSize();

            workspaceData.data.nodes.forEach(function(node) {
                self.updateOrAddNode(node);
            });
        };
    }
});
