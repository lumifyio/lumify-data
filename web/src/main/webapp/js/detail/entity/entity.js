
define([
    'flight/lib/component',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./entity',
    'tpl!./mentions',
    'tpl!./relationships'
], function(defineComponent, withTypeContent, withHighlighting, template, mentionsTemplate, relationshipsTemplate) {

    'use strict';

    return defineComponent(Entity, withTypeContent, withHighlighting);

    function Entity() {

        this.defaultAttrs({
            moreMentionsSelector: '.mention-request',
            mentionsSelector: '.entity-mentions',
            relationshipsSelector: '.entity-relationships',
            mentionArtifactSelector: '.mention-artifact'
        });

        this.after('initialize', function() {

            this.on('click', {
                moreMentionsSelector: this.onRequestMoreMentions,
                mentionArtifactSelector: this.onMentionArtifactSelected
            });

            this.$node.html(template({
                sign: this.attr.data.originalTitle || this.attr.data.title || 'No Title',
                highlightButton: this.highlightButton()
            }));

            this.loadEntity();
        });


        this.loadEntity = function() {
            var self = this;

            console.log('loadEntity', this.attr.data);

            var offset = 0,
                limit = 2, // TODO: sane value here?
                url = 'entity/' + encodeURIComponent(this.attr.data.rowKey).replace(/\./g, '$2E$') + '/mentions?offset=' + offset + '&limit=' + limit,
                dataInfo = JSON.stringify({
                    'rowKey': this.attr.data.rowKey,
                    'type': 'entity',
                    'subType': this.attr.data.subType
                });

            console.log('loadEntity dataInfo', dataInfo);

            self.getMoreMentions(url, this.attr.data.rowKey, dataInfo, function(mentionsHtml) {
                console.log('updating mentions');
                self.select('mentionsSelector').html(mentionsHtml);
                self.updateEntityAndArtifactDraggables();
            });

            self.getRelationships(self.attr.data.rowKey, function(relationships) {
                console.log('updating relationsships');
                self.select('relationshipsSelector').html(relationshipsTemplate({
                    relationships: relationships.statements
                }));
                self.updateEntityAndArtifactDraggables();
            });

        };

        this.onMentionArtifactSelected = function(evt, data) {
            var $target = $(evt.target).parents('a');

            this.trigger(document, 'searchResultSelected', {
                type: 'artifact',
                rowKey: $target.data("row-key")
            });
            evt.preventDefault();
        };


        this.onRequestMoreMentions = function(evt, data) {
            var self = this;
            var $target = $(evt.target);
            data = {
                key: $target.data('key'),
                url: $target.attr("href")
            };

            var dataInfo = JSON.stringify({
                'rowKey': data.key.value,
                'type': 'entity',
                'subType': data.key.conceptLabel
            });

            this.getMoreMentions(data.url, data.key, dataInfo, function(mentionsHtml){
                $('.entity-mentions', self.$node).html(mentionsHtml);
            });
            evt.preventDefault();
        };

        this.getMoreMentions = function(url, key, dataInfo, callback) {
            this.handleCancelling(this.ucdService.getEntityMentionsByRange(url, function(err, mentions){
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                mentions.mentions.forEach(function(termMention) {
                    var originalSentenceText = termMention['atc:sentenceText'];
                    var originalSentenceTextParts = {};

                    var artifactTermMention = JSON.parse(termMention.mention);

                    var termMentionStart = artifactTermMention.start - parseInt(termMention['atc:sentenceOffset'], 10);
                    var termMentionEnd = artifactTermMention.end - parseInt(termMention['atc:sentenceOffset'], 10);

                    termMention.sentenceTextParts = {
                        before: originalSentenceText.substring(0, termMentionStart),
                        term: originalSentenceText.substring(termMentionStart, termMentionEnd),
                        after: originalSentenceText.substring(termMentionEnd)
                    };
                });
                var html = mentionsTemplate({
                    mentions: mentions.mentions,
                    limit: mentions.limit,
                    offset: mentions.offset,
                    key: key,
                    dataInfo: dataInfo
                });
                callback(html);
            }));
        };

        this.getRelationships = function(rowKey, callback) {
            var self = this;

            this.handleCancelling(this.ucdService.getEntityRelationshipsBySubject(encodeURIComponent(rowKey).replace(/\./g, '$2E$'), function(err, relationships) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                console.log("Relationships: ", relationships);
                callback(relationships);
            }));
        };
    }
});
