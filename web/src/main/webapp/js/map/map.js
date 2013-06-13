

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
            var $this = this;
            this.$node.html(template({}));

            this.on(document, 'mapHide', this.onMapHide);
            this.on(document, 'mapShow', this.onMapShow);
        });

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
            self.mapInitialized = true;
        };
    }
});
