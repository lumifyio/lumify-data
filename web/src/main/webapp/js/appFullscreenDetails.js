

define([
    'flight/lib/component',
    'service/vertex',
    'service/ucd',
    'detail/detail',
    'tpl!appFullscreenDetails',
    'underscore'
], function(defineComponent, VertexService, UCD, Detail, template, _) {

    return defineComponent(FullscreenDetails);


    function FullscreenDetails() {
        this.vertexService = new VertexService();
        this.ucd = new UCD();

        this.defaultAttrs({
            detailSelector: '.detail-pane .content'
        });

        this.after('teardown', function() {
            window.onfocus = window.onblur = null;
        });

        this.after('initialize', function() {
            document.title = this.titleForVertices();
            this._windowHasFocus = true;
            this.vertices = [];

            this.fullscreenIdentifier = Math.floor((1 + Math.random()) * 0xFFFFFF).toString(16).substring(1);

            this.$node.addClass('fullscreen-details');

            window.onfocus = this.onWindowChange.bind(this, true);
            window.onblur = this.onWindowChange.bind(this, false);

            this.vertexService
                .getMultiple(this.attr.graphVertexIds)
                .done(this.handleVerticesLoaded.bind(this));
        });

        this.handleVerticesLoaded = function(vertices) {
            var artifactTitleDeferred = [], previousCount = this.vertices.length;

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
                this.$node.append(template({}));
                Detail.attachTo(this.$node.find('.detail-pane').last()
                                .addClass('type-' + v.properties._type + ' subType-' + v.properties._subType)
                                .find('.content'), {
                    loadGraphVertexData: v.properties,
                    highlightStyle: 0
                });
            }.bind(this));

            location.hash = '#v=' + _.pluck(this.vertices, 'id').sort().join(',');

            this.$node
                .removeClass('split-many split-' + previousCount)
                .addClass(this.vertices.length <= 4 ? 'split-' + this.vertices.length : 'split-many');

            var self = this;
            $.when.apply(null, artifactTitleDeferred)
             .done(function() {

                document.title = self.titleForVertices();

                if (!self._commSetup) {
                    self.setupTabCommunications();
                    self._commSetup = true;
                }
            });
        };

        this.onWindowChange = function(focus, event) {
            this._windowHasFocus = focus;

            if (this._windowHasFocus) {
                clearTimeout(this.timer);
                document.title = this.titleForVertices();
            }
        };

        this.onAddGraphVertices = function(data) {
            var self = this,
                vertices = data.vertices,
                targetIdentifier = data.targetIdentifier;

            if (targetIdentifier !== this.fullscreenIdentifier) {
                return;
            }

            if (!this._windowHasFocus) {
                var i = 0;
                clearTimeout(this.timer);
                self.timer = setTimeout(function f() {
                    document.title = (i++ % 2 === 0) ?  'New object opened' : self.titleForVertices();
                    self.timer = setTimeout(f, 500);
                }, 500);
            }

            var newVertices = _.reject(vertices, function(v) {
                return self.attr.graphVertexIds.indexOf(v) >= 0;
            });
            this.vertexService
                .getMultiple(newVertices)
                .done(this.handleVerticesLoaded.bind(this));
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

