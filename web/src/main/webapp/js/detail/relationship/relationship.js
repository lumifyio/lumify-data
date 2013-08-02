


define([
    'flight/lib/component',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./relationship',
    'tpl!./statements',
    'tpl!./excerpts',
    'underscore'
], function(defineComponent, withTypeContent, withHighlighting, template, statementsTemplate, excerptsTemplate, _) {

    'use strict';

    return defineComponent(Relationship, withTypeContent, withHighlighting);

    function Relationship() {

        this.defaultAttrs({
            moreMentionsSelector: '.mention-request',
            mentionsSelector: '.entity-mentions',
            relationshipsSelector: '.entity-relationships',
            mentionArtifactSelector: '.mention-artifact',
            statementsSelector: '.statements',
            bidirectionalStatementsSelector: 'bi-statements',
            entityToEntityRelationshipSelector: '.entity-to-entity-relationship a.relationship-summary',
        });

        this.after('initialize', function() {

            this.on('click', {
                moreMentionsSelector: this.onRequestMoreMentions,
                mentionArtifactSelector: this.onMentionArtifactSelected,
                entityToEntityRelationshipSelector: this.onEntityToEntityRelationshipClicked
            });

            var data = this.attr.data;
            if(data.relationshipType == 'artifactToEntity') {
                _.defer(this.trigger.bind(this), document, 'searchResultSelected', {
                    type: 'artifact',
                    rowKey: data.source,
                    entityOfInterest: data.target
                });
            } else if(data.relationshipType == 'entityToEntity') {
                this.loadRelationship();
            } else {
                return this.trigger(document, 'error', { error: "Bad relationship type:" + data.relationshipType });
            }
        });


        this.loadRelationship = function() {
            var self = this,
                data = this.attr.data;

            this.handleCancelling(this.ucdService.getEntityToEntityRelationshipDetails(data.source, data.target, function(err, relationshipData) {

                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                relationshipData.highlightButton = self.highlightButton();
                self.$node.html(template(relationshipData));
            }));
        };


        this.onEntityToEntityRelationshipClicked = function(evt) {
            var self = this;
            var $target = $(evt.target).parents('li');
            var statementRowKey = $target.data('statement-row-key');

            if ($target.hasClass('expanded')) {
                $target.removeClass('expanded');
                $target.addClass('collapsed');
                $('.artifact-excerpts', $target).hide();
            } else {
                $target.addClass('expanded');
                $target.removeClass('collapsed');

                this.handleCancelling(this.ucdService.getStatementByRowKey(statementRowKey, function(err, statement) {
                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }

                    var statementMentions = Object.keys(statement)
                        .filter(function(key) {
                            return key.indexOf('urn') === 0;
                        })
                        .map(function(key) {
                            return statement[key];
                        });
                    var html = excerptsTemplate({
                        mentions: statementMentions
                    });
                    $('.artifact-excerpts', $target).html(html);
                    $('.artifact-excerpts', $target).show();
                }));
            }

        };
    }
});











