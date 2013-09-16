
define([
    'flight/lib/component',
    'flight/lib/registry',
    'service/ucd',
    'service/ontology',
    'util/vertexList/list',
    './filters/filters',
    'tpl!./search',
    'tpl!./conceptItem',
    'tpl!./conceptSections',
    'tpl!util/alert',
    'util/jquery.ui.draggable.multiselect',
], function(
    defineComponent,
    registry,
    UCD,
    OntologyService,
    VertexList,
    Filters,
    template,
    conceptItemTemplate,
    conceptSectionsTemplate,
    alertTemplate) {
    'use strict';

    return defineComponent(Search);

    function Search() {
        this.ucd = new UCD();
        this.ontologyService = new OntologyService();
		this.currentQuery = null;

        this.defaultAttrs({
            formSelector: '.navbar-search',
            querySelector: '.navbar-search .search-query',
            queryValidationSelector: '.search-query-validation',
            resultsSummarySelector: '.search-results-summary',
            summaryResultItemSelector: '.search-results-summary li',
            resultsSelector: '.search-results',
            filtersSelector: '.search-filters'
        });

        this.searchResults = null;

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
            var $searchQueryValidation = this.select('queryValidationSelector');
            $searchQueryValidation.html('');

            var query = this.select('querySelector').val();
            if(!query) {
                this.select('resultsSummarySelector').empty();
                return $searchQueryValidation.html(alertTemplate({ error: 'Query cannot be empty' }));
            }
            this.trigger('search', { query: query });
            return false;
        };

        this.getConceptChildrenHtml = function(concept, indent) {
            var self = this,
                html = "";
            concept.children.forEach(function(concept) {
                html += conceptItemTemplate({
                    concept: concept,
                    indent: indent
                });
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
			if (this.select('querySelector').val() != query.query) {
				this.select('querySelector').val(query.query);
			}
			
            var self = this;

            this.ontologyService.concepts(function(err, concepts) {
                this.updateConceptSections(concepts);

                $.when(
                    this.ucd.artifactSearch(query),
                    this.ucd.graphVertexSearch(query || this.select('querySelector').val(), this.filters)
                ).done(function(artifactSearch, vertexSearch) {
                    var results = { artifact:artifactSearch[0], entity:{} };

                    // Organize vertexSearch Items
                    vertexSearch[0].vertices.forEach(function(v) {
                        var props = v.properties,
                            type = props._type,
                            subType = props._subType;

                        if (type === 'artifact') return;

                        if (!results[type]) results[type] = {};
                        if (!results[type][subType]) results[type][subType] = [];

                        results[type][subType].push(v);
                    });
                    self.searchResults = results;

                    Object.keys(results).forEach(function(type) {
                        if (type === 'artifact') {
                            Object.keys(results[type]).forEach(function(subType) {
                                self.$node.find('.' + subType + ' .badge').removeClass('loading').text(results[type][subType].length);
                            });
                        }
                    });
                    
                    concepts.byTitle.forEach(function(concept) {
                        self.onEntitySearchResultsForConcept(self.select('resultsSummarySelector'), concept, results.entity);
                    });

                }).fail(function() {
                    self.$node.find('.loading').removeClass('loading').text('!');
                });
            }.bind(this));
        };

        this.updateConceptSections = function(concepts) {
            var $searchResultsSummary = this.select('resultsSummarySelector'),
                resultsHtml = this.getConceptChildrenHtml(concepts.entityConcept, 15);

            $searchResultsSummary.html(conceptSectionsTemplate({ resultsHtml: resultsHtml }));
            $('.badge', $searchResultsSummary).addClass('loading');
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
                $searchResults = this.select('resultsSelector'),
                vertices = (this.searchResults[data._type][data._subType] || []).map(function(v) {
                    return $.extend({ }, data, v);
                });

            this.hideSearchResults();
            this.select('filtersSelector').hide();

            if (vertices.length) {
                VertexList.attachTo($searchResults.find('.content'), {
                    vertices: vertices
                });
                this.makeResizable($searchResults);
                $searchResults.show();
                $searchResults.find('.multi-select').focus();
            }
            this.trigger(document, 'paneResized');
        };

        this.makeResizable = function(node) {
            var self = this;

            // Add splitbar to search results
            return node.resizable({
                handles: 'e',
                minWidth: 200,
                maxWidth: 350, 
                resize: function() {
                    self.trigger(document, 'paneResized');
                }
            });
        };

		this.onKeyUp = function (evt) {
			var query = this.select('querySelector').val();
			if (query != this.currentQuery) {
				this.trigger("searchQueryChanged", { query: query});
				this.currentQuery = query;
			}
		};

        this.onQueryFocus = function (evt, data) {
            var filters = this.select('filtersSelector');
            Filters.attachTo(filters.find('.content'));

            this.makeResizable(filters);
            filters.show();
            this.hideSearchResults();
            this.$node.find('.search-results-summary .active').removeClass('active');
        };

        this.hideSearchResults = function() {
            registry.findInstanceInfoByNode(this.select('resultsSelector').hide().find('.content')[0]).forEach(function(info) {
                info.instance.teardown();
            });
            this.trigger(document, 'paneResized');
        };
		
		this.onQueryChange = function (evt, data) {
			if (!data.remoteEvent) {
				return;
			}
			
			this.select('querySelector').val(data.query);
			this.currentQuery = data.query;
		};

        this.close = function(e) {
            this.hideSearchResults();
            this.$node.find('.search-results-summary .active').removeClass('active');
        };

        this.after('initialize', function() {
            this.searchResults = {};
            this.$node.html(template({}));

            this.select('filtersSelector').hide();
            this.hideSearchResults();

            this.on('filterschange', this.onFiltersChange);

            this.on(document,'search', this.doSearch);
            this.on(document,'showSearchResults', this.onShowSearchResults);
			this.on(document,'searchQueryChanged',this.onQueryChange);
            this.on(document, 'menubarToggleDisplay', this.onMenubarToggle);
            this.on('submit', {
                formSelector: this.onFormSearch
            });
            this.on('click', {
                summaryResultItemSelector: this.onSummaryResultItemClick
            });
			this.on('keyup', {
				querySelector: this.onKeyUp
			});

            this.select('querySelector').on('focus', this.onQueryFocus.bind(this));
        });


        this.onMenubarToggle = function(evt, data) {
            var pane = this.$node.closest(':data(menubarName)');
            if (data.name === pane.data('menubarName')) {
                if (!pane.hasClass('visible')) {
                    this.$node.find('.search-results-summary .active').removeClass('active');
                    this.select('filtersSelector').hide();
                    this.hideSearchResults();
                }
            }
        };

        this.onFiltersChange = function(evt, data) {
            this.filters = data.filters;
            
            var query = this.select('querySelector').val() || '*';

            this.trigger(document, 'search', { query:query });
        };
    }
});
