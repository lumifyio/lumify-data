

define([
    'flight/lib/component',
    'flight/lib/registry',
    'tpl!./filters',
    'tpl!./item',
    'service/ontology'
], function(
    defineComponent,
    registry,
    template,
    itemTemplate,
    OntologyService) {
    'use strict';

    var FILTER_SEARCH_DELAY_SECONDS = 0.25;

    return defineComponent(Filters);

    function Filters() {
        this.currentFilters = {};
        this.filterId = 0;

        this.ontologyService = new OntologyService();

        this.defaultAttrs({
            propertySelector: 'select.property-filters'
        });

        this.after('initialize', function() {
            this.notifyOfFilters = _.debounce(this.notifyOfFilters.bind(this), FILTER_SEARCH_DELAY_SECONDS * 1000);

            this.$node.html(template({}));

            this.on('change', { propertySelector: this.onPropertyChanged });
            this.on('propertychange', this.onPropertyFieldItemChanged);

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

        this.onPropertyFieldItemChanged = function(event, data) {
            this.currentFilters[data.id] = data;
            this.notifyOfFilters();
            event.stopPropagation();
        };

        this.onPropertyChanged = function(event, data) {
            var self = this,
                target = $(event.target),
                property = target.find(':selected').data('info'),
                li = target.closest('li'),
                addRemoveOption = target.find('option').eq(0);

            if (!property || !property.dataType) {
                this.teardownField(target.next('.configuration'));
                li.remove();
                return;
            }

            addRemoveOption.text('Remove filter');

            require(['fields/' + property.dataType], function(PropertyFieldItem) {
                var node = target.next('.configuration');

                self.teardownField(node);

                PropertyFieldItem.attachTo(node, {
                    property: property,
                    id: self.filterId++,
                    predicates: true
                });

                li.removeClass('newrow');

                if (self.$node.find('.newrow').length === 0) {
                    li.closest('ul').append(itemTemplate({properties:self.properties}));
                }
            });
        };

        this.teardownField = function(node) {
            var self = this, 
                instanceInfo = registry.findInstanceInfoByNode(node[0]);
            if (instanceInfo && instanceInfo.length) {
                instanceInfo.forEach(function(info) {
                    delete self.currentFilters[info.instance.attr.id];
                    if (!info.instance.isValid || info.instance.isValid()) {
                        self.notifyOfFilters();
                    }
                    info.instance.teardown();
                });
            }

            node.empty();
        };

        this.loadPropertyFilters = function() {
            var self = this;

            this.ontologyService.properties(function(err, properties) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                self.properties = _.filter(properties.list, function(p) { 
                    if (p.title === 'boundingBox') return false; 
                    if (/^_/.test(p.title)) return false;
                    return true; 
                });
                self.$node.find('.nav-header').after(itemTemplate({properties:self.properties}));
            });
        };
    }
});
