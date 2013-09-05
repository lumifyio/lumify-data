
define([
    'flight/lib/component',
    'service/ucd',
    'service/ontology',
    'util/previews',
    'util/video/scrubber',
    './filters/filters',
    'tpl!./search',
    'tpl!./searchResultsSummary',
    'tpl!./searchResults',
    'tpl!util/alert',
    'util/jquery.ui.draggable.multiselect',
], function(defineComponent, UCD, OntologyService, previews, VideoScrubber, Filters, template, summaryTemplate, resultsTemplate, alertTemplate) {
    'use strict';

    return defineComponent(Search);

    function Search() {
        this.ucd = new UCD();
        this.ontologyService = new OntologyService();
		this.currentQuery = null;

        this.defaultAttrs({
            searchFormSelector: '.navbar-search',
            searchQuerySelector: '.navbar-search .search-query',
            searchQueryValidationSelector: '.search-query-validation',
            searchResultsSummarySelector: '.search-results-summary',
            searchSummaryResultItemSelector: '.search-results-summary li',
            searchResultsScrollSelector: '.search-results ul.nav',
            searchResultItemLinkSelector: '.search-results li a',
            searchResultsSelector: '.search-results',
            filtersSelector: '.search-filters',
            filtersContentSelector: '.search-filters ul.nav'
        });

        this.searchResults = null;

        this.onArtifactSearchResults = function(evt, artifacts) {
            var $searchResultsSummary = this.select('searchResultsSummarySelector');
            $searchResultsSummary.find('.document .badge').removeClass('loading').text(artifacts.document.length);
            $searchResultsSummary.find('.image .badge').removeClass('loading').text(artifacts.image.length);
            $searchResultsSummary.find('.video .badge').removeClass('loading').text(artifacts.video.length);
        };

        this.onEntitySearchResults = function(evt, entities) {
            var self = this;
            console.log('onEntitySearchResults', entities);
            var $searchResultsSummary = this.select('searchResultsSummarySelector');
            this.ontologyService.concepts(function(err, concepts) {
                concepts.entityConcept.children.forEach(function(concept) {
                    self.onEntitySearchResultsForConcept($searchResultsSummary, concept, entities);
                });
            });
        };

        this.onEntitySearchResultsForConcept = function($searchResultsSummary, concept, entities) {
            var self = this;
            $searchResultsSummary.find('.concept-' + concept.id + ' .badge').removeClass('loading').text((entities[concept.id] || []).length);
            if(concept.children && concept.children.length > 0) {
                concept.children.forEach(function(childConcept) {
                    self.onEntitySearchResultsForConcept($searchResultsSummary, childConcept, entities);
                });
            }
        };

        this.onFormSearch = function(evt) {
            evt.preventDefault();
            var $searchQueryValidation = this.select('searchQueryValidationSelector');
            $searchQueryValidation.html('');

            var query = this.select('searchQuerySelector').val();
            if(!query) {
                this.select('searchResultsSummarySelector').empty();
                return $searchQueryValidation.html(alertTemplate({ error: 'Query cannot be empty' }));
            }
            this.trigger('search', { query: query });
            return false;
        };

        this.getConceptChildrenHtml = function(concept, indent) {
            var self = this;
            var html = "";
            concept.children.forEach(function(concept) {
                html += '<li item-path="entity.' + concept.id + '" class="concept-' + concept.id + '"><a href="#" style="padding-left:' + indent + 'px;">' + concept.displayName + '<span class="badge"></span></a></li>';
                if(concept.children && concept.children.length > 0) {
                    html += self.getConceptChildrenHtml(concept, indent + 15);
                }
            });
            return html;
        };

        this.doSearch = function(evt, query) {
            if (!this.searchResults) {
                this.searchResults = {};
            }
			if (this.select('searchQuerySelector').val() != query.query) {
				this.select('searchQuerySelector').val(query.query);
			}
			
            var self = this;

            this.ontologyService.concepts(function(err, concepts) {
                this.updateConceptSections(concepts);
                this.searchArtifacts(query);
                this.searchEntities(query);
            }.bind(this));
        };

        this.updateConceptSections = function(concepts) {
            var $searchResultsSummary = this.select('searchResultsSummarySelector'),
                $searchResults = this.select('searchResultsSelector'),
                resultsHtml = this.getConceptChildrenHtml(concepts.entityConcept, 15);

            $searchResultsSummary.html(summaryTemplate({ resultsHtml: resultsHtml }));
            $('.badge', $searchResultsSummary).addClass('loading');
        };

        this.searchArtifacts = function(query) {
            var self = this;

            this.ucd.artifactSearch(query, function(err, artifacts) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                self.searchResults.artifact = artifacts;
                self.trigger('artifactSearchResults', artifacts);
            });
        };

        this.searchEntities = function(query) {
            var self = this;

            this.ucd.graphVertexSearch(query || this.select('searchQuerySelector').val(), this.filters, function(err, entities) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                self.searchResults.entity = {};
                entities.vertices.forEach(function(entity) {
                    entity.sign = entity.properties.title;
                    entity.source = entity.properties.source;
                    entity.graphVertexId = entity.id;
                    self.searchResults.entity[entity.properties._subType] = self.searchResults.entity[entity.properties._subType] || [];
                    self.searchResults.entity[entity.properties._subType].push(entity);
                });
                self.trigger('entitySearchResults', self.searchResults.entity);
            });
        };

        this.onSummaryResultItemClick = function(evt) {
            evt.preventDefault();

            var $target = $(evt.target).parents('li');
            if ($target.hasClass('active')) {
                return this.close(evt);
            }

            if (+$target.find('.badge').text() === 0) {
                return this.close(evt);
            }

            this.$node.find('.search-results-summary .active').removeClass('active');
            $target.addClass('active');

            var itemPath = $target.attr('item-path').split('.');
            var _type = itemPath[0];
            var _subType = itemPath[1];
            this.trigger('showSearchResults', {
                _type: _type,
                _subType: _subType
            });
        };

        this.onShowSearchResults = function(evt, data) {
            var self = this,
                $searchResults = this.select('searchResultsSelector');

            data.results = this.searchResults[data._type][data._subType] || [];

            this.select('filtersSelector').hide();

            data.results.forEach(function(result) {
                if(data._type == 'artifact') {
                    result.title = result.subject;
                } else if(data._type == 'entity') {
                    result.title = result.sign;
                } else {
                    result.title = 'Error: unknown type: ' + data._type;
                }

                // Check if this result is in the graph/map
                var classes = ['gId' + encodeURIComponent(result.graphVertexId)];
                var vertexState = _currentVertices[result.graphVertexId];
                if (vertexState) {
                    if ( vertexState.inGraph ) classes.push('graph-displayed');
                    if ( vertexState.inMap ) classes.push('map-displayed');
                }
                if (data._subType === 'video' || data._subType === 'image') {
                    classes.push('has_preview');
                }
                result.className = classes.join(' ');
            });

            this.makeResizable($searchResults);

            // Update content
            $searchResults.find('ul').html(resultsTemplate(data));

            // Allow search results to be draggable, selectable
            this.applyDraggable( $searchResults.find('li a') );
            
            if (data.results.length) {
                $searchResults.show().find('.multi-select').focus();

                this.loadVisibleResultPreviews();
            } else {
                $searchResults.hide();
            }
        };

        this.makeResizable = function(node) {
            var self = this;

            // Add splitbar to search results
            node.resizable({
                handles: 'e',
                minWidth: 200,
                maxWidth: 350, 
                resize: function() {
                    self.trigger(document, 'paneResized');
                }
            });
        };

		this.onKeyUp = function (evt) {
			var query = this.select('searchQuerySelector').val();
			if (query != this.currentQuery) {
				this.trigger("searchQueryChanged", { query: query});
				this.currentQuery = query;
			}
		};

        this.onQueryFocus = function (evt, data) {
            Filters.attachTo(this.select('filtersContentSelector'));

            var filters = this.select('filtersSelector');

            this.makeResizable(filters);
            filters.show();
            this.select('searchResultsSelector').hide();
            this.$node.find('.search-results-summary .active').removeClass('active');
        };
		
		this.onQueryChange = function (evt, data) {
			if (!data.remoteEvent) {
				return;
			}
			
			this.select('searchQuerySelector').val(data.query);
			this.currentQuery = data.query;
		};

        this.onFocusSearchField = function() {
            this.select('searchQuerySelector').focus();
        };

        this.close = function(e) {
            this.select('searchResultsSelector').hide();
            this.$node.find('.search-results-summary .active').removeClass('active');
        };

        var previewTimeout;
        this.onResultsScroll = function(e) {
            clearTimeout(previewTimeout);
            previewTimeout = setTimeout(this.loadVisibleResultPreviews.bind(this), 1000);
        };

        this.loadVisibleResultPreviews = function() {
            var self = this;

            if ( !self.previewQueue ) {
                self.previewQueue = previews.createQueue('searchresults', { maxConcurrent: 1 });
            }

            var ul = self.select('searchResultsScrollSelector'),
                yMin = ul[0].offsetTop,
                yMax = yMin + ul.height(),
                lis = ul.children('li'),
                lisVisible = lis
                    .filter(function(){ 
                        return this.offsetTop >= yMin && this.offsetTop < yMax;
                    });
            
            lisVisible.each(function() {
                var li = $(this),
                    info = li.data('info'),
                    _rowKey = info._rowKey;

                if ((info._subType === 'video' || info._subType === 'image') && !li.data('preview-loaded')) {
                    li.addClass('preview-loading');
                    previews.generatePreview(_rowKey, null, function(poster, frames) {
                        li.removeClass('preview-loading')
                          .data('preview-loaded', true);

                        if(info._subType === 'video') {
                            VideoScrubber.attachTo(li.find('.preview'), {
                                posterFrameUrl: poster,
                                videoPreviewImageUrl: frames
                            });
                        } else if(info._subType === 'image') {
                            li.find('.preview').html("<img src='" + poster + "' />");
                        }
                    });
                }
            });
        };


        this.after('initialize', function() {
            this.searchResults = {};
            this.$node.html(template({}));

            this.select('searchResultsSelector').hide();

            this.on('filterschange', this.onFiltersChange);

            this.on(document,'search', this.doSearch);
            this.on('artifactSearchResults', this.onArtifactSearchResults);
            this.on('entitySearchResults', this.onEntitySearchResults);
            this.on(document,'showSearchResults', this.onShowSearchResults);
			this.on(document,'searchQueryChanged',this.onQueryChange);
            this.on(document, 'focusSearchField', this.onFocusSearchField);
            this.on('submit', {
                searchFormSelector: this.onFormSearch
            });
            this.on('click', {
                searchSummaryResultItemSelector: this.onSummaryResultItemClick
            });
			this.on('keyup', {
				searchQuerySelector: this.onKeyUp
			});

            this.select('searchQuerySelector').on('focus', this.onQueryFocus.bind(this));
            this.select('searchResultsScrollSelector').on('scroll', this.onResultsScroll.bind(this));

            this.on(document, 'verticesAdded', this.onVerticesUpdated);
            this.on(document, 'verticesUpdated', this.onVerticesUpdated);
            this.on(document, 'verticesDeleted', this.onVerticesDeleted);
            this.on(document, 'switchWorkspace', this.onWorkspaceClear);
            this.on(document, 'workspaceDeleted', this.onWorkspaceClear);
            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);

            this.select('searchResultsSelector').droppable({ accept:'.search-results *' });
        });

        this.onWorkspaceLoaded = function(evt, workspace) {
            this.onVerticesUpdated(evt, workspace.data || {});
        };

        this.onFiltersChange = function(evt, data) {
            this.filters = data.filters;
            
            var query = this.select('searchQuerySelector').val() || '*';

            // TODO: star query is broken for entities
            if (this.$node.find('.search-results-summary li').length) {
                this.searchEntities(query);
            } else {
                this.trigger(document, 'search', { query:query });
            }
        };

        // Track changes to vertices so we display the "Displayed in Graph" icon
        // in search results
        var _currentVertices = {};
        this.toggleSearchResultIcon = function(graphVertexId, inGraph, inMap) {
            this.$node
                .find('li.gId' + encodeURIComponent(graphVertexId))
                .toggleClass('graph-displayed', inGraph)
                .toggleClass('map-displayed', inMap);
        };

        // Switching workspaces should clear the icon state and vertices
        this.onWorkspaceClear = function() {
            this.$node.find('li.graph-displayed').removeClass('graph-displayed');
            this.$node.find('li.map-displayed').removeClass('map-displayed');
            _currentVertices = {};
        };

        this.onVerticesUpdated = function(event, data) {
            var self = this;
            (data.vertices || []).forEach(function(vertex) {
                // Only care about vertex search results and location updates
                if ( (vertex._type && vertex._subType) || vertex.location || vertex.locations ) {
                    var inGraph = true;
                    var inMap = !!(vertex.location || (vertex.locations && vertex.locations.length));
                    _currentVertices[vertex.graphVertexId] = { inGraph:inGraph, inMap:inMap };
                    self.toggleSearchResultIcon(vertex.graphVertexId, inGraph, inMap);
                }
            });
        };

        this.onVerticesDeleted = function(event, data) {
            var self = this;
            (data.vertices || []).forEach(function(vertex) {
                delete _currentVertices[vertex.graphVertexId];
                self.toggleSearchResultIcon(vertex.graphVertexId, false, false);
            });
        };


        this.applyDraggable = function(el) {
            var self = this;

            el.draggable({
                helper:'clone',
                appendTo: 'body',
                revert: 'invalid',
                revertDuration: 250,
                scroll: false,
                zIndex: 100,
                distance: 10,
                multi: true,
                otherDraggablesClass: 'search-result-dragging',
                start: function(ev, ui) {
                    $(ui.helper).addClass('search-result-dragging');
                },
                otherDraggables: function(ev, ui){

                    ui.otherDraggables.each(function(){
                        var info = this.data('original').parent().data('info'),
                            offset = this.offset(),
                            dropPosition = { x:offset.left, y:offset.top };

                        self.trigger(document, 'addVertices', {
                            vertices: [{
                                title: info.title,
                                graphVertexId: info.graphVertexId,
                                _rowKey: info._rowKey,
                                _subType: info._subType,
                                _type: info._type,
                                dropPosition: dropPosition
                            }]
                        });
                    });
                },
                selection: function(ev, ui) {
                    var selected = ui.selected,
                        info = selected.map(function() {
                            return $(this).data('info');
                        }).toArray();

                    self.trigger(document, 'searchResultSelected', [info]);
                }
            });
        };
    }

});
