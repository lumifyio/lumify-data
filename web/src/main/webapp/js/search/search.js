
define([
    'flight/lib/component',
    'service/ucd',
    'tpl!./search',
    'tpl!./searchResultsSummary',
    'tpl!./searchResults',
    'tpl!util/alert',
    'util/jquery.ui.draggable.multiselect',
], function(defineComponent, UCD, template, summaryTemplate, resultsTemplate, alertTemplate) {
    'use strict';

    return defineComponent(Search);

    function Search() {
        this.ucd = new UCD();
		this.currentQuery = null;

        this.defaultAttrs({
            searchFormSelector: '.navbar-search',
            searchQuerySelector: '.navbar-search .search-query',
            searchQueryValidationSelector: '.search-query-validation',
            searchResultsSummarySelector: '.search-results-summary',
            searchSummaryResultItemSelector: '.search-results-summary li',
            searchResultItemLinkSelector: '.search-results li a',
            searchResultsSelector: '.search-results',
            closeResultsSelector: '.search-results .close'
        });

        this.searchResults = null;

        this.onArtifactSearchResults = function(evt, artifacts) {
            var $searchResultsSummary = this.select('searchResultsSummarySelector');
            $searchResultsSummary.find('.documents .badge').removeClass('loading').text(artifacts.document.length);
            $searchResultsSummary.find('.images .badge').removeClass('loading').text('0'); // TODO
            $searchResultsSummary.find('.videos .badge').removeClass('loading').text('0'); // TODO
        };

        this.onEntitySearchResults = function(evt, entities) {
            var $searchResultsSummary = this.select('searchResultsSummarySelector');
            $searchResultsSummary.find('.people .badge').removeClass('loading').text((entities.person || []).length);
            $searchResultsSummary.find('.locations .badge').removeClass('loading').text((entities.location || []).length);
            $searchResultsSummary.find('.organizations .badge').removeClass('loading').text((entities.organization || []).length);
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
            $searchResultsSummary.html(summaryTemplate({}));
            $('.badge', $searchResultsSummary).addClass('loading');
            this.ucd.artifactSearch(query, function(err, artifacts) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                self.searchResults.artifacts = artifacts;
                self.trigger('artifactSearchResults', artifacts);
            });
            this.ucd.entitySearch(query, function(err, entities) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                self.searchResults.entities = entities;
                self.trigger('entitySearchResults', entities);
            });
        };

        this.onSummaryResultItemClick = function(evt) {
            var $target = $(evt.target).parents('li');

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
                if(data.type == 'artifacts') {
                    result.title = result.subject;
                } else if(data.type == 'entities') {
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

        this.close = function(e) {
            this.select('searchResultsSelector').hide();
            this.$node.find('.search-results-summary .active').removeClass('active');
        };


        this.after('initialize', function() {
            this.$node.html(template({}));

            this.select('searchResultsSelector').hide();

            this.on(document,'search', this.doSearch);
            this.on('artifactSearchResults', this.onArtifactSearchResults);
            this.on('entitySearchResults', this.onEntitySearchResults);
            this.on(document,'showSearchResults', this.onShowSearchResults);
			this.on(document,'searchQueryChanged',this.onQueryChange);
            this.on('submit', {
                searchFormSelector: this.onFormSearch
            });
            this.on('click', {
                searchSummaryResultItemSelector: this.onSummaryResultItemClick,
                closeResultsSelector: this.close
            });
			this.on('keyup', {
				searchQuerySelector: this.onKeyUp
			});

            this.on(document, 'nodesAdd', this.onNodesUpdate);
            this.on(document, 'nodesUpdate', this.onNodesUpdate);
            this.on(document, 'nodesDelete', this.onNodesDelete);
            this.on(document, 'switchWorkspace', this.onSwitchWorkspace);
        });


        // Track changes to nodes so we display the "Displayed in Graph" icon
        // in search results
        var _currentNodes = {};
        this.toggleSearchResultIcon = function(rowKey, inGraph, inMap) {
            this.$node
                .find('li.' + encodeURIComponent(rowKey).replace(/%/g,"\\%"))
                .toggleClass('graph-displayed', inGraph)
                .toggleClass('map-displayed', inMap);
        };

        // Switching workspaces should clear the icon state and nodes
        this.onSwitchWorkspace = function() {
            this.$node.find('li.graph-displayed').removeClass('graph-displayed');
            this.$node.find('li.map-displayed').removeClass('map-displayed');
            _currentNodes = {};
        };

        this.onNodesUpdate = function(event, data) {
            var self = this;
            (data.nodes || []).forEach(function(node) {

                // Only care about node search results and location updates
                if ( (node.type && node.subType) || node.location || node.locations ) {
                    var inGraph = true;
                    var inMap = !!(node.location || node.locations);
                    _currentNodes[node.rowKey] = { inGraph:inGraph, inMap:inMap };
                    self.toggleSearchResultIcon(node.rowKey, inGraph, inMap);
                }
            });
        };

        this.onNodesDelete = function(event, data) {
            var self = this;
            (data.nodes || []).forEach(function(node) {
                delete _currentNodes[node.rowKey];
                self.toggleSearchResultIcon(node.rowKey, false, false);
            });
        };


        this.applyDraggable = function(el) {

            var $this = this;
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
                        var info = this.data('original').parent().data('info');
                        $this.trigger(this, 'addToGraph', {
                            text: info.title,
                            info:info
                        });
                    });
                },
                selection: function(ev, ui) {
                    var selected = ui.selected,
                        info = selected.map(function() {
                            return $(this).data('info');
                        }).toArray();

                    $this.trigger(document, 'searchResultSelected', [info]);
                }
            });
        };
    }

});
