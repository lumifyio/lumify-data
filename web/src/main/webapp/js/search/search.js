
define([
    'flight/lib/component',
    'ucd/ucd',
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
            $searchResultsSummary.find('.organizations .badge').removeClass('loading').text('0'); // TODO
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
                subType: subType,
                results: this.searchResults[type][subType] || []
            });
        };

        this.onShowSearchResults = function(evt, data) {
            console.log("Showing search results: ", data);

            var $searchResults = this.select('searchResultsSelector');


            data.results.forEach(function(result) {
                if(data.type == 'artifacts') {
                    result.title = result.subject;
                } else if(data.type == 'entities') {
                    result.title = result.sign;
                } else {
                    result.title = 'Error: unknown type: ' + data.type;
                }
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

        this.close = function(e) {
            this.select('searchResultsSelector').hide();
            this.$node.find('.search-results-summary .active').removeClass('active');
        };


        this.after('initialize', function() {
            this.$node.html(template({}));

            this.select('searchResultsSelector').hide();

            this.on('search', this.doSearch);
            this.on('artifactSearchResults', this.onArtifactSearchResults);
            this.on('entitySearchResults', this.onEntitySearchResults);
            this.on('showSearchResults', this.onShowSearchResults);
            this.on('submit', {
                searchFormSelector: this.onFormSearch
            });
            this.on('click', {
                searchSummaryResultItemSelector: this.onSummaryResultItemClick,
                closeResultsSelector: this.close
            });
        });


        this.applyDraggable = function(el) {

            var $this = this;
            el.draggable({
                helper:'clone',
                appendTo: 'body',
                revert: 'invalid',
                revertDuration: 250,
                addClass: false,
                scroll: false,
                zIndex: 100,
                multi: true,
                otherDraggables: function(ev, ui){

                    ui.otherDraggables.each(function(){
                        var info = this.data('original').parent().data('info');

                        $this.trigger(this, 'addToGraph', {
                            text: this.text(), 
                            info:info
                        });
                    });
                },
                selection: function(ev, ui) {
                    var selected = ui.selected,
                        info = selected.data('info');

                    $this.trigger(document, 'searchResultSelected', info);
                }
            });
        };
    }

});


