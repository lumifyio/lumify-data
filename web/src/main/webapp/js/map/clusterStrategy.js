
define(['openlayers'], function(OpenLayers) {

    return OpenLayers.Class(OpenLayers.Strategy.Cluster, {
        activate: function() {
            var activated = OpenLayers.Strategy.prototype.activate.call(this);
            if(activated) {
                this.selectedFeatures = {};
                this.layer.events.on({
                    "beforefeaturesadded": this.cacheFeatures,
                    "featuresremoved": this.removeFeatures,
                    "featureselected": this.onFeaturesSelected,
                    "featureunselected": this.onFeaturesUnselected,
                    "moveend": this.cluster,
                    scope: this
                });
            }
            return activated;
        },

        onFeaturesSelected: function(event) {
            var feature = event.feature,
                sf = this.selectedFeatures = {};

            (feature.cluster || [feature]).forEach(function(f) {
                sf[f.id] = true;
            });
        },

        onFeaturesUnselected: function(event) {
            if (!this.selectedFeatures) return;

            var sf = this.selectedFeatures;

            if (event.feature) {
                delete sf[event.feature.id];
                if (event.feature.cluster) {
                    event.feature.cluster.forEach(function(f) {
                        delete sf[f.id];
                    });
                }
            } else {
                this.selectedFeatures = {};
            }
        },

        addToCluster: function(cluster, feature) {
            OpenLayers.Strategy.Cluster.prototype.addToCluster.apply(this, arguments);
            if (this.selectedFeatures[feature.id]) {
                cluster.renderIntent = 'select';
            }
        },

        createCluster: function(feature) {
            var cluster = OpenLayers.Strategy.Cluster.prototype.createCluster.apply(this, arguments);
            if (this.selectedFeatures[feature.id]) {
                cluster.renderIntent = 'select';
            } else cluster.renderIntent = 'default';
            return cluster;
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
