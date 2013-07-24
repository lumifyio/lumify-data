
define([
    'flight/lib/component',
    '../withDropdown',
    'tpl!./statementForm',
    'service/ucd',
    'service/entity',
    'underscore'
], function(defineComponent, withDropdown, statementFormTemplate, Ucd, EntityService, _) {
    'use strict';

    return defineComponent(StatementForm, withDropdown);


    function StatementForm() {
        this.entityService = new EntityService();

        this.defaultAttrs({
            sourceTermSelector: '.src-term',
            destTermSelector: '.dest-term',
            createStatementButtonSelector: '.create-statement'
        });

        this.after('initialize', function() {
            this.$node.html(statementFormTemplate({
                source: this.attr.sourceTerm.text(),
                dest: this.attr.destTerm.text()
            }));

            this.on('click', {
                createStatementButtonSelector: this.onCreateStatement
            });
        });


        this.onCreateStatement = function(event) {

            // TODO: call service and close
            alert('Not implemented');
            _.defer(this.teardown.bind(this));

            /*
            this.entityService.createTerm(parameters, function(err, data) {
                if (err) {
                    self.trigger(document, 'error', err);
                } else {
                    _.defer(self.teardown.bind(self));
                }
            });
            */

        };
    }

});
