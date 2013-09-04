
define([
    'service/ucd',
    'tpl!./toolbar/fullscreen',
    'tpl!./toolbar/fullscreen-item'
], function(UCD, fullscreenButtonTemplate, fullscreenItemTemplate) {

    var intercomInstance;

    return withTypeContent;

    function withTypeContent() {

        this.ucdService = new UCD();
        this._xhrs = [];

        this.defaultAttrs({
            fullscreenSingleSelector: '.fullscreen-single',
            fullscreenMultiSelector: '.fullscreen-multi',
            fullscreenDropdownButtonSelector: '.fullscreen-multi .dropdown-toggle',
            fullscreenDropdownItemSelector: '.fullscreen-multi .existing a'
        });

        this.after('teardown', function() {
            this.cancel();
            this.$node.empty();
        });

        this.before('initialize', function() {
            this.$node.html('Loading...');
        });

        this.after('initialize', function() {
            if (!window.isFullscreenDetails) {
                this.on('clearAvailableFullscreenDetails', this.onFullscreenClear);
                this.on('fullscreenDetailVerticesAvailable', this.onFullscreenAdd);
                this.on('click', {
                    fullscreenDropdownButtonSelector: this.onFullscreenDropdownClicked,
                    fullscreenDropdownItemSelector: this.onFullscreenWindowClicked
                });

                this.setupTabCommunication();
            }
        });

        this.fullscreenButton = function(vertexIds) {
            return fullscreenButtonTemplate({
                vertexIds: vertexIds
            });
        };

        this.onFullscreenDropdownClicked = function(event) {
            if (intercomInstance) {
                var multi = this.select('fullscreenMultiSelector');
                multi.find('.existing').remove();
                multi.find('.divider').hide();
                intercomInstance.emit('ping');
            }
        };

        this.onFullscreenWindowClicked = function(event) {
            var info = $(event.target).closest('li').data('info');

            if (info) {
                var ids;
                if ($.isArray(this.attr.data)) {
                    ids = _.map(
                            _.reject(this.attr.data, function(v) { 
                                return v._type === 'relationship'; 
                            }), function(v) {
                                return v.id || v.graphVertexId;
                            });
                } else {
                    ids = [this.attr.data.id || this.attr.data.graphVertexId];
                }
                intercomInstance.emit('addVertices', {
                    message: JSON.stringify({
                        targetIdentifier: info.identifier,
                        vertices: ids
                    })
                });
            }
        };

        this.onFullscreenClear = function(event, data) {
            this.select('fullscreenMultiSelector')
                .hide()
                .find('.existing').remove();

            this.select('fullscreenSingleSelector').show();
        };

        this.onFullscreenAdd = function(event, data) {
            $.when.apply(null, this._xhrs)
             .done(function() {
                var multi = this.select('fullscreenMultiSelector');
                multi.show()
                    .find('ul')
                    .append(fullscreenItemTemplate({
                        data: data,
                        text: data.title
                    }));

                multi.find('.divider').show();

                this.select('fullscreenSingleSelector').hide();
             }.bind(this));
        };

        this.setupTabCommunication = function() {
            var self = this;

            require(['intercom'], function(Intercom) {
                if (!intercomInstance) {
                    intercomInstance = Intercom.getInstance();

                    intercomInstance.on('fullscreenDetailsWithVertices', function(data) {
                        var info = JSON.parse(data.message);
                        self.trigger('fullscreenDetailVerticesAvailable', info);
                    });
                }
                self.trigger('clearAvailableFullscreenDetails');
                intercomInstance.emit('ping');
            });
        };

        this.classesForVertex = function(vertex) {
            var cls = [],
                props = vertex.properties || vertex;

            if (props._type == 'artifact') {
                cls.push('artifact');
                cls.push(props._subType);
            } else {
                cls.push('entity');
                cls.push('subType-' + props._subType);
            }
            cls.push('gId-' + (vertex.id || props.graphNodeId));

            return cls.join(' ');
        };

        this.cancel = function() {
            this._xhrs.forEach(function(xhr) {
                if (xhr.state() !== 'complete') {
                    xhr.abort();
                }
            });
            this._xhrs.length = 0;
        };

        // Pass a started XHR request to automatically cancel if detail pane
        // changes
        this.handleCancelling = function(xhr) {
            this._xhrs.push(xhr);
        };
    }
});
