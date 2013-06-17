

define([
    'flight/lib/component',
    'tpl!./map',
], function(defineComponent, template) {
    'use strict';

    return defineComponent(Map);

    function Map() {

        this.defaultAttrs({
            mapSelector: '#map'
        });

        this.after('initialize', function() {
            var self = this;
            this.$node.html(template({}));

            this.on(document, 'mapHide', this.onMapHide);
            this.on(document, 'mapShow', this.onMapShow);
            this.on(document, 'mapCenter', this.onMapCenter);

            this.on(document, 'mapEndPan', function(evt, mapCenter) {
                if(self.lastMarker) {
                    self.lastMarker.closeBubble();
                    self.map.removeMarker(self.lastMarker);
                }
                var pt = new mxn.LatLonPoint(mapCenter.lat, mapCenter.lng);
                self.lastMarker = new mxn.Marker(pt);
                self.lastMarker.setInfoBubble("User");
                self.lastMarker.click.addHandler(function() {
                    self.lastMarker.openBubble();
                });
                self.map.addMarker(self.lastMarker);
            });
        });

        this.onMapCenter = function(evt, data) {
            console.log(data);
            this.trigger(document, 'modeSelect', { mode: 'map' });
            var latlon = new mxn.LatLonPoint(data.latitude, data.longitude);
            this.map.setCenterAndZoom(latlon, 7);
        };

        this.onMapHide = function() {
            this.$node.hide();
        };

        this.onMapShow = function() {
            this.$node.show();
            this.initializeMap();
        };

        this.initializeMap = function() {
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
            self.map.addSmallControls();
            self.map.endPan.addHandler(function() {
                var center = self.map.getCenter();
                self.trigger(document, 'mapEndPan', {
                    lat: center.lat,
                    lng: center.lng
                });
            });
            self.mapInitialized = true;
        };
    }
});
