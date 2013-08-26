
define([
    'flight/lib/component',
    'tpl!./overlay',
    'sf'
], function(defineComponent, template, sf) {

    var LAST_SAVED_UPDATE_FREQUENCY_SECONDS = 30;

    return defineComponent(WorkspaceOverlay);

    function WorkspaceOverlay() {

        this.defaultAttrs({
            nameSelector: '.name',
            subtitleSelector: '.subtitle'
        });

        this.after('initialize', function() {
            this.$node.html(template({}));

            this.on(document, 'workspaceSwitched', this.onWorkspaceSwitched);
            this.on(document, 'workspaceSaving', this.onWorkspaceSaving);
            this.on(document, 'workspaceSaved', this.onWorkspaceSaved);
        });

        this.setContent = function(title, subtitle) {
            this.select('nameSelector').text(title);
            this.select('subtitleSelector').html(subtitle);
        };

        this.onWorkspaceSwitched = function(event, data) {
            this.setContent(data.workspace.title, 'no changes');
            clearTimeout(this.updateTimer);
        };

        this.onWorkspaceSaving = function(event, data) {
            this.select('subtitleSelector').text('saving...');
            clearTimeout(this.updateTimer);
        };

        this.onWorkspaceSaved = function(event, data) {
            clearTimeout(this.updateTimer);
            this.lastSaved = Date.now();

            if (data.title) {
                this.select('nameSelector').text(data.title);
            }

            var prefix = 'last saved ',
                subtitle = this.select('subtitleSelector').text(prefix + 'moments ago'),
                setTimer = function() {
                    this.updateTimer = setTimeout(function () {
                        var span = new sf.TimeSpan(Date.now() - this.lastSaved),
                            time = '';

                        if (span.seconds < 30 && span.minutes < 1) {
                            time = 'moments';
                        } else if (span.minutes < 2) {
                            time = 'a minute';
                        } else if (span.hours < 1) {
                            time = sf("{0:^m 'minutes'}", span);
                        } else if (span.hours === 1 && span.days < 1) {
                            time = 'an hour';
                        } else if (span.days < 1) {
                            time = sf("{0:^h 'hours'}", span);
                        } else if (span.days === 1 && span.months < 1) {
                            time = 'a day';
                        } else if (span.months < 1) {
                            time = sf("{0:^d 'days'}", span);
                        } else time = 'a long time ago';

                        subtitle.text(prefix + time + ' ago');
                        setTimer();
                    }.bind(this), LAST_SAVED_UPDATE_FREQUENCY_SECONDS * 1000);
                }.bind(this);

            setTimer();
        };
    }
});
