
define([
    'flight/lib/component',
    '/js/ucd/ucd.js',
    'tpl!search/search',
    'tpl!search/searchResultsSummary',
    'tpl!search/searchResults'
], function(defineComponent, UCD, template, summaryTemplate, resultsTemplate) {
    'use strict';

    return defineComponent(Search);

    function Search() {
        this.defaultAttrs({
            searchFormSelector: '#search-form',
            searchQuerySelector: '#search-form .search-query',
            searchResultsSummarySelector: '#search-results-summary',
            searchSummaryResultItemSelector: '#search-results-summary li li',
            searchResultsSelector: '#search-results'
        });

        this.searchResults = null;

        this.onArtifactSearchResults = function(evt, artifacts) {
            var $searchResultsSummary = this.select('searchResultsSummarySelector');
            $searchResultsSummary.find('.artifacts .documents .count').html(artifacts.document.length);
            $searchResultsSummary.find('.artifacts .images .count').html('0'); // TODO
            $searchResultsSummary.find('.artifacts .videos .count').html('0'); // TODO
        };

        this.onEntitySearchResults = function(evt, entities) {
            var $searchResultsSummary = this.select('searchResultsSummarySelector');
            $searchResultsSummary.find('.entities .people .count').html((entities.person || []).length);
            $searchResultsSummary.find('.entities .locations .count').html((entities.location || []).length);
            $searchResultsSummary.find('.entities .organizations .count').html('0'); // TODO
        };

        this.onFormSearch = function(evt) {
            evt.preventDefault();
            this.searchResults = {};
            var query = this.select('searchQuerySelector').val();
            this.trigger('search', { query: query });
            return false;
        };

        this.doSearch = function(evt, query) {
            var self = this;
            var $searchResultsSummary = this.select('searchResultsSummarySelector');
            var $searchResults = this.select('searchResultsSelector');

            $searchResults.hide();
            $searchResultsSummary.html(summaryTemplate({}));
            new UCD().artifactSearch(query, function(err, artifacts) {
                if(err) {
                    return alert("TODO: show some error dialog? " + err);
                }
                self.searchResults.artifacts = artifacts;
                self.trigger('artifactSearchResults', artifacts);
            });
            new UCD().entitySearch(query, function(err, entities) {
                if(err) {
                    return alert("TODO: show some error dialog? " + err);
                }
                self.searchResults.entities = entities;
                self.trigger('entitySearchResults', entities);
            });
        };

        this.onSummaryResultItemClick = function(evt) {
            var $target = $(evt.target);
            if($target.hasClass('count')) {
                $target = $target.parent('li');
            }

            var itemPath = $target.attr('item-path').split('.');
            var type = itemPath[0];
            var subType = itemPath[1];
            this.trigger('showSearchResults', {
                type: type,
                subType: subType,
                results: this.searchResults[type][subType]
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

            var html = resultsTemplate(data);
            $searchResults.html(html);
            $searchResults.show();
        };

        this.after('initialize', function() {
            this.$node.html(template({}));
            this.on('search', this.doSearch);
            this.on('artifactSearchResults', this.onArtifactSearchResults);
            this.on('entitySearchResults', this.onEntitySearchResults);
            this.on('showSearchResults', this.onShowSearchResults);
            this.on('submit', {
                searchFormSelector: this.onFormSearch
            });
            this.on('click', {
                searchSummaryResultItemSelector: this.onSummaryResultItemClick
            })
        });
    }

});


