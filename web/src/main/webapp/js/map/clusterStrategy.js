
define(['openlayers'], function(OpenLayers) {

    return OpenLayers.Class(OpenLayers.Strategy.Cluster, {
        activate: function() {
            var activated = OpenLayers.Strategy.prototype.activate.call(this);
            if(activated) {
                this.layer.events.on({
                    "beforefeaturesadded": this.cacheFeatures,
                    "featuresremoved": this.removeFeatures,
                    "moveend": this.cluster,
                    scope: this
                });
            }
            return activated;
        },

        cacheFeatures: function(event) {
            var propagate = true;
            if(!this.clustering) {
                this.features = this.features || [];
                var currentIds = [];
                this.features.forEach(function gatherId(feature) {
                    if (feature.cluster) {
                        feature.cluster.forEach(gatherId);
                        return;
                    }
                    currentIds.push(feature.id);
                });
                event.features.forEach(function(feature) {
                    if (! ~currentIds.indexOf(feature.id)) {
                        this.features.push(feature);
                    }
                }.bind(this));
                this.cluster();
                propagate = false;
            }
            return propagate;
        },

        removeFeatures: function(event) {
            if(!this.clustering) {
                var existingIds = _.pluck(event.features, 'id');
                this.features = _.filter(this.features, function(feature) {
                    return ! ~existingIds.indexOf(feature.id);
                });
            }
        }
    });
});
