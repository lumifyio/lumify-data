define([
    'service/serviceBase'
], function(ServiceBase) {
    'use strict';

    // Override in configuration.properties with `web.ui` prefix
    var DEFAULTS = {
        'vertex.loadRelatedMaxBeforePrompt': 50,
        'vertex.loadRelatedMaxForceSearch': 250,
        'ontology.iri.artifactHasEntity': 'http://lumify.io/dev#rawHasEntity',
        'properties.multivalue.defaultVisibleCount': 2,
        'map.provider': 'google',
        'map.provider.osm.url': 'https://a.tile.openstreetmap.org/${z}/${x}/${y}.png,' +
                                'https://b.tile.openstreetmap.org/${z}/${x}/${y}.png,' +
                                'https://c.tile.openstreetmap.org/${z}/${x}/${y}.png'
    };

    // Coerce all values to strings since that's what they will be from
    // server
    _.keys(DEFAULTS).forEach(function(key) {
        DEFAULTS[key] = '' + DEFAULTS[key];
    });

    function ConfigService() {
        ServiceBase.call(this);
        return this;
    }

    ConfigService.prototype = Object.create(ServiceBase.prototype);

    ConfigService.prototype.getProperties = function(refresh) {
        if (!refresh && this.cachedProperties) return this.cachedProperties;

        return (this.cachedProperties = $.get('configuration').then(this.applyDefaults));
    };

    ConfigService.prototype.applyDefaults = function(properties) {
        return _.extend({}, DEFAULTS, properties);
    };

    return ConfigService;
});
