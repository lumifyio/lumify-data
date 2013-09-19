

define([
    'flight/lib/component',
    'flight/lib/registry',
    'tpl!./appFullscreenDetails',
    'service/vertex',
    'service/ucd',
    'detail/detail'
], function(defineComponent, registry, template, VertexService, UCD, Detail) {
    'use strict';

    return defineComponent(FullscreenDetails);

    function filterEntity(v) {
        return v.properties._type === 'entity';
    }
    function filterArtifacts(v) {
        return v.properties._type === 'artifact';
    }

    function FullscreenDetails() {
        this.vertexService = new VertexService();
        this.ucd = new UCD();

        this.defaultAttrs({
            detailSelector: '.detail-pane .content',
            closeSelector: '.close-detail-pane'
        });

        this.after('initialize', function() {
            this.$node.html(template({}));
            this.updateTitle();

            this._windowIsHidden = false;
            this.on(document, 'window-visibility-change', this.onVisibilityChange);
            this.on('click', { closeSelector: this.onClose });
            this.vertices = [];

            this.fullscreenIdentifier = Math.floor((1 + Math.random()) * 0xFFFFFF).toString(16).substring(1);

            this.$node.addClass('fullscreen-details');

            this.vertexService
                .getMultiple(this.attr.graphVertexIds)
                .done(this.handleVerticesLoaded.bind(this));
        });

        this.onClose = function(event) {
            event.preventDefault();

            var self = this,
                pane = $(event.target).closest('.detail-pane'),
                node = pane.find('.content');
                instanceInfos = registry.findInstanceInfoByNode(node[0]);

            if (instanceInfos.length) {
                instanceInfos.forEach(function(info) {
                    self.vertices = _.reject(self.vertices, function(v) {
                        return v.id === info.instance.attr.loadGraphVertexData.graphVertexId;
                    });
                    info.instance.teardown();
                });
            }
            pane.remove();

            this.updateLocationHash();
            this.updateLayout();
            this.updateTitle();
        };

        this.updateLocationHash = function() {
            location.hash = '#v=' + _.pluck(this.vertices, 'id').sort().join(',');
        };

        this.updateLayout = function() {

            this.$node.toggleClass('onlyone', this.vertices.length === 1);

            var verts = this.vertices.length,
                entities = _.filter(this.vertices, filterEntity).length,
                artifacts = _.filter(this.vertices, filterArtifacts).length;

            this.$node
                .removePrefixedClasses('vertices- entities- artifacts- has- entity-cols-')
                .addClass([
                    this.vertices.length <= 4 ? 'vertices-' + this.vertices.length : 'vertices-many',
                    'entities-' + entities,
                    'entity-cols-' + _.find([4,3,2,1], function(i) { return entities % i === 0; }),
                    entities ? 'has-entities' : '',
                    'artifacts-' + artifacts,
                    artifacts ? 'has-artifacts' : ''
                ].join(' '));
        };

        this.updateTitle = function() {
            document.title = this.titleForVertices();
        };

        this.handleVerticesLoaded = function(vertices) {
            var artifactTitleDeferred = [];

            Detail.teardownAll();
            this.$node.find('.detail-pane').remove();

            vertices.forEach(function(v) {
                v.properties.graphVertexId = v.id;

                if (v.properties._type === 'artifact' && v.properties._subType === 'document') {
                    artifactTitleDeferred.push(
                        this.ucd.getArtifactById(v.properties._rowKey)
                        .done(function(a) {
                            v.properties.title = a.Generic_Metadata.subject || 'No title available';
                        })
                    );
                }

                this.vertices.push(v);
            }.bind(this));

            this.vertices = _.sortBy(this.vertices, function(v) {
                var descriptors = [];

                // Entities first
                descriptors.push(v.properties._type === 'entity' ? 0 : 1);

                // Image/Video before documents
                descriptors.push(/^(image|video)$/i.test(v.properties._subType) ? 0 : 1);

                // Sort by title
                descriptors.push(v.properties.title);
                return descriptors.join(''); 
            });
            
            this.vertices.forEach(function(v) {
                var node = v.properties._type === 'entity' ? 
                    this.$node.find('.entities-container') : this.$node.find('.artifacts-container');

                node.append('<div class="detail-pane visible highlight-none"><div class="content"/></div>');
                Detail.attachTo(this.$node.find('.detail-pane').last()
                                .addClass('type-' + v.properties._type + ' subType-' + v.properties._subType)
                                .find('.content'), {
                    loadGraphVertexData: v.properties,
                    highlightStyle: 2
                });
            }.bind(this));

            this.updateLocationHash();
            this.updateLayout();

            var self = this;
            $.when.apply(null, artifactTitleDeferred)
             .done(function() {

                self.updateTitle();


                if (!self._commSetup) {
                    self.setupTabCommunications();
                    self._commSetup = true;
                }
            });
        };

        this.onVisibilityChange = function(event, data) {
            this._windowIsHidden = data.hidden;
            if (data.visible) {
                clearTimeout(this.timer);
                this.updateTitle();
            }
        };

        this.onAddGraphVertices = function(data) {
            var self = this,
                vertices = data.vertices,
                targetIdentifier = data.targetIdentifier;

            if (targetIdentifier !== this.fullscreenIdentifier) {
                return;
            }

            var existingVertexIds = _.pluck(this.vertices, 'id');
            var newVertices = _.reject(vertices, function(v) {
                return existingVertexIds.indexOf(v) >= 0;
            });

            if (newVertices.length === 0) {
                return;
            }

            if (this._windowIsHidden) {
                this.flashTitle(vertices);
            }

            this.vertexService
                .getMultiple(newVertices)
                .done(this.handleVerticesLoaded.bind(this))
                .done(this.flashTitle.bind(this, newVertices));
        };

        this.flashTitle = function(newVertexIds, newVertices) {
            var self = this,
                i = 0;

            if (!newVertices || newVertices.length === 0) return;

            clearTimeout(this.timer);

            if (this._windowIsHidden) {
                this.timer = setTimeout(function f() {
                    if (self._windowIsHidden && i++ % 2 === 0) {
                        document.title = newVertices.length === 1 ? 
                            ('"' + newVertices[0].properties.title + '" added') :
                            newVertices.length + ' items added';
                    } else {
                        self.updateTitle();
                    }

                    if (self._windowIsHidden) {
                        self.timer = setTimeout(f, 500);
                    }
                }, 500);
            }
        };

        this.titleForVertices = function() {
            if (!this.vertices || this.vertices.length === 0) {
                return 'Loading...';
            } else if (this.vertices.length === 1) {
                return this.vertices[0].properties.title;
            } else {
                var first = '"' + this.vertices[0].properties.title + '"',
                    l = this.vertices.length - 1;

                return first + ' and ' + l + ' other' + (l > 1 ? 's' : '');
            }
        };

        this.setupTabCommunications = function() {
            var self = this;

            require(['intercom'], function(Intercom) {
                var intercom = Intercom.getInstance(),
                    broadcast = function() {
                        intercom.emit('fullscreenDetailsWithVertices', {
                            message: JSON.stringify({
                                vertices: self.vertices,
                                title: self.titleForVertices(),
                                identifier: self.fullscreenIdentifier
                            })
                        }); 
                    };

                intercom.on('addVertices', function(data) {
                    self.onAddGraphVertices(JSON.parse(data.message));
                });
                intercom.on('ping', broadcast);
                broadcast();
            });
        };
    }
});

