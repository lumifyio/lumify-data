
define([
    'flight/lib/component',
    'service/ucd',
    'service/entity',
    'util/previews',
    'util/video/scrubber',
    'tpl!./search',
    'tpl!./searchResultsSummary',
    'tpl!./searchResults',
    'tpl!util/alert',
    'util/jquery.ui.draggable.multiselect',
], function(defineComponent, UCD, EntityService, previews, VideoScrubber, template, summaryTemplate, resultsTemplate, alertTemplate) {
    'use strict';

    return defineComponent(Search);

    function Search() {
        this.ucd = new UCD();
        this.entityService = new EntityService();
		this.currentQuery = null;

        this.defaultAttrs({
            searchFormSelector: '.navbar-search',
            searchQuerySelector: '.navbar-search .search-query',
            searchQueryValidationSelector: '.search-query-validation',
            searchResultsSummarySelector: '.search-results-summary',
            searchSummaryResultItemSelector: '.search-results-summary li',
            searchResultsScrollSelector: '.search-results ul.nav',
            searchResultItemLinkSelector: '.search-results li a',
            searchResultsSelector: '.search-results'
        });

        this.searchResults = null;

        this.onArtifactSearchResults = function(evt, artifacts) {
            var $searchResultsSummary = this.select('searchResultsSummarySelector');
            $searchResultsSummary.find('.document .badge').removeClass('loading').text(artifacts.document.length);
            $searchResultsSummary.find('.image .badge').removeClass('loading').text(artifacts.image.length);
            $searchResultsSummary.find('.video .badge').removeClass('loading').text(artifacts.video.length);
        };

        this.onEntitySearchResults = function(evt, entities) {
            var $searchResultsSummary = this.select('searchResultsSummarySelector');
            $searchResultsSummary.find('.badge').removeClass('loading').text('0');
            $searchResultsSummary.find('.person .badge').removeClass('loading').text((entities.person || []).length);
            $searchResultsSummary.find('.location .badge').removeClass('loading').text((entities.location || []).length);
            $searchResultsSummary.find('.organization .badge').removeClass('loading').text((entities.organization || []).length);
        };

        this.onFormSearch = function(evt) {
            evt.preventDefault();
            this.searchResults = {};
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

        this.doSearch = function(evt, query) {
			//added for sync effect
			if (!this.searchResults) {
				this.searchResults = {};
			}
			
			if (this.select('searchQuerySelector').val() != query.query) {
				this.select('searchQuerySelector').val(query.query);
			}
			
            var self = this;
            var $searchResultsSummary = this.select('searchResultsSummarySelector');
            var $searchResults = this.select('searchResultsSelector');

            $searchResults.hide();
            this.entityService.concepts(function(err, concepts) {
                $searchResultsSummary.html(summaryTemplate({concepts:concepts}));
                $('.badge', $searchResultsSummary).addClass('loading');
                this.ucd.artifactSearch(query, function(err, artifacts) {
                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }
                    self.searchResults.artifact = artifacts;
                    self.trigger('artifactSearchResults', artifacts);
                });
                this.ucd.entitySearch(query, function(err, entities) {
                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }
                    self.searchResults.entity = entities;
                    self.trigger('entitySearchResults', entities);
                });
            }.bind(this));
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
            var type = itemPath[0];
            var subType = itemPath[1];
            this.trigger('showSearchResults', {
                type: type,
                subType: subType
            });
        };

        this.onShowSearchResults = function(evt, data) {
            console.log("Showing search results: ", data);

            var $searchResults = this.select('searchResultsSelector');
            data.results = this.searchResults[data.type][data.subType] || [];

            data.results.forEach(function(result) {
                if(data.type == 'artifact') {
                    result.title = result.subject;
                } else if(data.type == 'entity') {
                    result.title = result.sign;
                } else {
                    result.title = 'Error: unknown type: ' + data.type;
                }

                // Check if this result is in the graph/map
                var classes = [encodeURIComponent(result.rowKey)];
                var nodeState = _currentNodes[result.rowKey];
                if (nodeState) {
                    if ( nodeState.inGraph ) classes.push('graph-displayed');
                    if ( nodeState.inMap ) classes.push('map-displayed');
                }
                if (data.subType === 'video' || data.subType === 'image') {
                    classes.push('has_preview');
                }
                result.className = classes.join(' ');
            });

            // Add splitbar to search results
            $searchResults.resizable({
                handles: 'e',
                minWidth: 50
            });
            
            // Update content
            $searchResults.find('ul').html(resultsTemplate(data));

            // Allow search results to be draggable, selectable
            this.applyDraggable( $searchResults.find('li a') );
            
            if (data.results.length) {
                $searchResults.show();

                this.loadVisibleResultPreviews();
            } else {
                $searchResults.hide();
            }
        };

		this.onKeyUp = function (evt) {
			var query = this.select('searchQuerySelector').val();
			if (query != this.currentQuery) {
				this.trigger("searchQueryChanged", { query: query});
				this.currentQuery = query;
			}
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
                    rowKey = info.rowKey;

                if ((info.subType === 'video' || info.subType === 'image') && !li.data('preview-loaded')) {
                    li.addClass('preview-loading');
                    previews.generatePreview(rowKey, null, function(poster, frames) {
                        li.removeClass('preview-loading')
                          .data('preview-loaded', true);

                        if(info.subType === 'video') {
                            VideoScrubber.attachTo(li.find('.preview'), {
                                posterFrameUrl: poster,
                                videoPreviewImageUrl: frames
                            });
                        } else if(info.subType === 'image') {
                            li.find('.preview').html("<img src='" + poster + "' />");
                        }
                    });
                }
            });
        };


        this.after('initialize', function() {
            this.$node.html(template({}));

            this.select('searchResultsSelector').hide();

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

            this.select('searchResultsScrollSelector').on('scroll', this.onResultsScroll.bind(this));

            this.on(document, 'nodesAdded', this.onNodesUpdated);
            this.on(document, 'nodesUpdated', this.onNodesUpdated);
            this.on(document, 'nodesDeleted', this.onNodesDeleted);
            this.on(document, 'switchWorkspace', this.onSwitchWorkspace);
            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
        });

        this.onWorkspaceLoaded = function(evt, workspace) {
            this.onNodesUpdated(evt, workspace.data || {});
        };

        // Track changes to nodes so we display the "Displayed in Graph" icon
        // in search results
        var _currentNodes = {};
        this.toggleSearchResultIcon = function(rowKey, inGraph, inMap) {
            this.$node
                .find('li.' + encodeURIComponent(rowKey).replace(/(['"%])/g,"\\$1"))
                .toggleClass('graph-displayed', inGraph)
                .toggleClass('map-displayed', inMap);
        };

        // Switching workspaces should clear the icon state and nodes
        this.onSwitchWorkspace = function() {
            this.$node.find('li.graph-displayed').removeClass('graph-displayed');
            this.$node.find('li.map-displayed').removeClass('map-displayed');
            _currentNodes = {};
        };

        this.onNodesUpdated = function(event, data) {
            var self = this;
            (data.nodes || []).forEach(function(node) {

                // Only care about node search results and location updates
                if ( (node.type && node.subType) || node.location || node.locations ) {
                    var inGraph = true;
                    var inMap = !!(node.location || (node.locations && node.locations.length));
                    _currentNodes[node.rowKey] = { inGraph:inGraph, inMap:inMap };
                    self.toggleSearchResultIcon(node.rowKey, inGraph, inMap);
                }
            });
        };

        this.onNodesDeleted = function(event, data) {
            var self = this;
            (data.nodes || []).forEach(function(node) {
                delete _currentNodes[node.rowKey];
                self.toggleSearchResultIcon(node.rowKey, false, false);
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

                        self.trigger(document, 'addNodes', {
                            nodes: [{
                                title: info.title,
                                rowKey: info.rowKey.replace(/\\[x](1f)/ig, '\u001f'),
                                subType: info.subType,
                                type: info.type,
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
