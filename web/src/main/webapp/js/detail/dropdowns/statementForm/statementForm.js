
define([
    'flight/lib/component',
    '../withDropdown',
    'tpl!./statementForm',
    'service/statement',
    'underscore'
], function(defineComponent, withDropdown, statementFormTemplate, StatementService, _) {
    'use strict';

    return defineComponent(StatementForm, withDropdown);


    function StatementForm() {
        this.statementService = new StatementService();

        this.defaultAttrs({
            formSelector: '.form',
            sourceTermSelector: '.src-term',
            destTermSelector: '.dest-term',
            termLabelsSelector: '.src-term span, .dest-term span',
            createStatementButtonSelector: '.create-statement',
            statementLabelSelector: '.statement-label',
            invertAnchorSelector: 'a.invert'
        });

        this.after('initialize', function() {
            this.$node.html(statementFormTemplate({
                source: this.attr.sourceTerm.text(),
                dest: this.attr.destTerm.text()
            }));


            this.applyTermClasses(this.attr.sourceTerm, this.select('sourceTermSelector'));
            this.applyTermClasses(this.attr.destTerm, this.select('destTermSelector'));

            this.attr.sourceTerm.addClass('focused');
            this.attr.destTerm.addClass('focused');
            
            this.select('createStatementButtonSelector').attr('disabled', true);
            this.setupLabelTypeAhead();

            this.on('click', {
                createStatementButtonSelector: this.onCreateStatement,
                invertAnchorSelector: this.onInvert
            });
            this.on('opened', this.onOpened);
        });

        this.after('teardown', function() {
            this.attr.sourceTerm.removeClass('focused');
            this.attr.destTerm.removeClass('focused');
        });


        this.applyTermClasses = function(el, applyToElement) {
            var classes = el.attr('class').split(/\s+/),
                ignored = [/^ui-*/, /^term$/, /^entity$/];

            classes.forEach(function(cls) {
                var ignore = _.any(ignored, function(regex) { return regex.test(cls); });
                if ( !ignore ) {
                    applyToElement.addClass(cls);
                }
            });
        };

        this.onInputChange = function(e) {
            this.select('createStatementButtonSelector')
                .attr('disabled', $.trim($(e.target).val()).length === 0);
        };

        this.onOpened = function() {
            this.select('statementLabelSelector')
                .on('change keyup', this.onInputChange.bind(this))
                .focus();
        };

        this.onInvert = function(e) {
            e.preventDefault();
            this.select('formSelector').toggleClass('invert');
        };


        this.onCreateStatement = function(event) {
            var self = this,
                parameters = {
                    sourceGraphNodeId: this.attr.sourceTerm.data('info').graphNodeId,
                    destGraphNodeId: this.attr.destTerm.data('info').graphNodeId,
                    predicateLabel: this.select('statementLabelSelector').val()
                };

            if (this.select('formSelector').hasClass('invert')) {
                var swap = parameters.sourceGraphNodeId;
                parameters.sourceGraphNodeId = parameters.destGraphNodeId;
                parameters.destGraphNodeId = swap;
            }

            this.statementService.createStatement(parameters, function(err, data) {
                if (err) {
                    self.trigger(document, 'error', err);
                } else {
                    _.defer(self.teardown.bind(self));
                    self.trigger(document, 'refreshRelationships');
                }
            });
        };

        this.setupLabelTypeAhead = function() {
            var self = this;

            self.select('statementLabelSelector').typeahead({
                source: function(query, callback) {
                    self.statementService.predicates(function(err, predicates) {
                        if(err) {
                            console.error('Error', err);
                            callback([]);
                            return self.trigger(document, 'error', { message: err.toString() });
                        }

                        callback(predicates.map(function(p) {
                            return p.labelUi; 
                        }));
                    });
                    return;
                }
            });
        };
    }

});
