

define([], function() {

    return withProperties;

    function withProperties() {

        this.filterPropertiesForDisplay = function(properties, ontologyProperties) {
            var displayProperties = [];

            Object.keys(properties).forEach(function(name) {
                var displayName, value,
                    ontologyProperty = ontologyProperties.byTitle[name];

                if (ontologyProperty) {
                    displayName = ontologyProperty.displayName;
                    if(ontologyProperty.dataType == 'date') {
                        value = sf("{0:yyyy/MM/dd}", new Date(properties[name]));
                    } else {
                        value = properties[name];
                    }
                } else {
                    displayName = name;
                    value = properties[name];
                }

                var data = {
                    key: name,
                    value: value,
                    displayName: displayName
                };

                if(/^[^_]/.test(name)) {
                    displayProperties.push(data);
                }
            });
            return displayProperties;
        };
    }
});
