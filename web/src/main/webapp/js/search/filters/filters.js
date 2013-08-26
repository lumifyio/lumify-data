

define([
    'flight/lib/component',
    'flight/lib/registry',
    'tpl!./filters',
    'tpl!./item',
    'service/ontology',
    'underscore'
], function(defineComponent, registry, template, itemTemplate, OntologyService, _) {

    return defineComponent(Filters);

    function Filters() {
        this.currentFilters = {};
        this.filterId = 0;

        this.ontologyService = new OntologyService();

        this.defaultAttrs({
            propertySelector: 'select.property-filters'
        });

        this.after('initialize', function() {
            this.$node.html(template({}));

            this.on('change', { propertySelector: this.onPropertyFilterChanged });
            this.on('filterchange', this.onFilterItemChanged);

            this.loadPropertyFilters();
        });

        this.notifyOfFilters = function() {
            this.trigger('filterschange', {
                filters: _.map(this.currentFilters, function(filter) {
                    return {
                        propertyId: filter.propertyId,
                        predicate: filter.predicate,
                        values: filter.values
                    };
                })
            });
        };

        this.onFilterItemChanged = function(event, data) {
            this.currentFilters[data.id] = data;
            this.notifyOfFilters();
            event.stopPropagation();
        };

        this.onPropertyFilterChanged = function(event, data) {
            var self = this,
                target = $(event.target),
                property = target.find(':selected').data('info'),
                li = target.closest('li');

            if (!property || !property.dataType) {
                li.addClass('newrow');
                li.next('.newrow').remove();
                return this.teardownFilter(target.next('.configuration'));
            }

            require(['search/filters/types/' + property.dataType], function(FilterItem) {
                var node = target.next('.configuration');

                self.teardownFilter(node);

                FilterItem.attachTo(node, {
                    property: property,
                    id: self.filterId++
                });

                li.removeClass('newrow');

                if (self.$node.find('.newrow').length === 0) {
                    li.closest('ul').append(itemTemplate({properties:self.properties}));
                }
            });
        };

        this.teardownFilter = function(node) {
            var self = this,
                instanceInfo = registry.findInstanceInfoByNode(node[0]);

            if (instanceInfo && instanceInfo.length) {
                instanceInfo.forEach(function(info) {
                    delete self.currentFilters[info.instance.attr.id];
                    self.notifyOfFilters();
                    info.instance.teardown();
                });
            }

            node.empty();
        };

        this.loadPropertyFilters = function() {
            var self = this;

            this.ontologyService.properties(function(err, properties) {
                if (err) return;

                self.properties = properties.list;
                self.$node.find('.nav-header').after(itemTemplate({properties:properties.list}));
            });
        };
    }
});
