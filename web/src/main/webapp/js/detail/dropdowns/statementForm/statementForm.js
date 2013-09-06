define([
    'flight/lib/component',
    '../withDropdown',
    'tpl!./statementForm',
    'tpl!./relationship-options',
    'service/statement',
    'service/ontology',
    'underscore'
], function (defineComponent, withDropdown, statementFormTemplate, relationshipTypeTemplate, StatementService, OntologyService, _) {
    'use strict';

    return defineComponent(StatementForm, withDropdown);

    function StatementForm() {
        this.statementService = new StatementService();
        this.ontologyService = new OntologyService();

        this.defaultAttrs({
            formSelector: '.form',
            sourceTermSelector: '.src-term',
            destTermSelector: '.dest-term',
            termLabelsSelector: '.src-term span, .dest-term span',
            createStatementButtonSelector: '.create-statement',
            statementLabelSelector: '.statement-label',
            invertAnchorSelector: 'a.invert',
            relationshipSelector: 'select'
        });

        this.after('initialize', function () {
            this.$node.html(statementFormTemplate({
                source: this.attr.sourceTerm.text(),
                dest: this.attr.destTerm.text()
            }));

            this.applyTermClasses(this.attr.sourceTerm, this.select('sourceTermSelector'));
            this.applyTermClasses(this.attr.destTerm, this.select('destTermSelector'));

            this.attr.sourceTerm.addClass('focused');
            this.attr.destTerm.addClass('focused');

            this.select('createStatementButtonSelector').attr('disabled', true);
            this.getRelationshipLabels (this.attr.sourceTerm, this.attr.destTerm);

            this.on('click', {
                createStatementButtonSelector: this.onCreateStatement,
                invertAnchorSelector: this.onInvert
            });
            this.on('opened', this.onOpened);
        });

        this.after('teardown', function () {
            this.attr.sourceTerm.removeClass('focused');
            this.attr.destTerm.removeClass('focused');
        });


        this.applyTermClasses = function (el, applyToElement) {
            var classes = el.attr('class').split(/\s+/),
                ignored = [/^ui-*/, /^term$/, /^entity$/];

            classes.forEach(function (cls) {
                var ignore = _.any(ignored, function (regex) {
                    return regex.test(cls);
                });
                if (!ignore) {
                    applyToElement.addClass(cls);
                }
            });

            applyToElement.addClass('concepticon-' + el.data('info')._subType);
        };

        this.onSelection = function (e) {
            if (this.select('relationshipSelector').val() == ''){
                this.select('createStatementButtonSelector')
                                .attr('disabled', true);
                return;
            }
            this.select('createStatementButtonSelector')
                .attr('disabled', false);
        };

        this.onOpened = function () {
            this.select('relationshipSelector')
                .on('change', this.onSelection.bind(this))
                .focus();
        };

        this.onInvert = function (e) {
            e.preventDefault();
            this.select('formSelector').toggleClass('invert');
            this.getRelationshipLabels (this.attr.destTerm, this.attr.sourceTerm);
        };


        this.onCreateStatement = function (event) {
            var self = this,
                parameters = {
                    sourceGraphVertexId: this.attr.sourceTerm.data('info').graphVertexId,
                    destGraphVertexId: this.attr.destTerm.data('info').graphVertexId,
                    predicateLabel: this.select('relationshipSelector').val()
                };

            if (this.select('formSelector').hasClass('invert')) {
                var swap = parameters.sourceGraphVertexId;
                parameters.sourceGraphVertexId = parameters.destGraphVertexId;
                parameters.destGraphVertexId = swap;
            }

            this.statementService.createStatement(parameters, function (err, data) {
                if (err) {
                    self.trigger(document, 'error', err);
                } else {
                    _.defer(self.teardown.bind(self));
                    self.trigger(document, 'refreshRelationships');
                }
            });
        };

        this.getRelationshipLabels = function (sourceTerm, destTerm) {
            var self = this;
            var sourceConceptTypeId = sourceTerm.data('info')._subType;
            var destConceptTypeId = destTerm.data('info')._subType;
            self.statementService.relationships (sourceConceptTypeId, destConceptTypeId, function (err, results){
                if (err) {
                    console.error ('Error', err);
                    return self.trigger (document, 'error', { message: err.toString () });
                }

                self.displayRelationships (results.relationships);
            });
        };

        this.displayRelationships = function (relationships) {
            var self = this;
            self.ontologyService.relationships(function(err, ontologyRelationships) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                var relationshipsTpl = [];

                relationships.forEach(function(relationship) {
                    var ontologyRelationship = ontologyRelationships.byTitle[relationship.title];
                    var displayName;
                    if(ontologyRelationship) {
                        displayName = ontologyRelationship.displayName;
                    } else {
                        displayName = relationship.title;
                    }

                    var data = {
                        title: relationship.title,
                        displayName: displayName
                    };

                    relationshipsTpl.push(data);
                });

                self.select('relationshipSelector').html(relationshipTypeTemplate({ relationships: relationshipsTpl }));
            });
        };
    }

});
