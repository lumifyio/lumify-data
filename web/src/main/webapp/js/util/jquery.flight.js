define(['flight/lib/registry'],function(registry) {

    $.fn.lookupComponent = function(instanceConstructor) {
        return _lookupComponent(this[0], instanceConstructor);
    };

    $.fn.teardownComponent = function(instanceConstructor) {
        var instance = _lookupComponent(this[0], instanceConstructor);
        if (instance) {
            instance.teardown();
        }
    };

    function _lookupComponent (elem, instanceConstructor) {
        var results = registry.findInstanceInfoByNode(elem);
        for (i = 0; i < results.length; ++i) {
            if (results[i].instance.constructor === instanceConstructor) {
                return results[i].instance;
            }
        }
        return false;
    };

});