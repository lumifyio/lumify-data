
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

            this.on(document, {
                workspaceSwitched: this.onWorkspaceSwitched,
                workspaceSaving: this.onWorkspaceSaving,
                workspaceSaved: this.onWorkspaceSaved
            });
        });

        this.onWorkspaceSwitched = function(event, data) {
            console.log(data);
            this.select('nameSelector').text(data.workspace.name);
            this.select('subtitleSelector').text('');
            clearTimeout(this.updateTimer);
        };

        this.onWorkspaceSaving = function(event, data) {
            console.log(data);
            clearTimeout(this.updateTimer);
            this.select('subtitleSelector').text('saving...');
        };

        this.onWorkspaceSaved = function(event, data) {
            console.log(data);
            clearTimeout(this.updateTimer);
            this.lastSaved = Date.now();

            var prefix = 'last saved ',
                subtitle = this.select('subtitleSelector').text(prefix + 'moments ago'),
                setTimer = function() {
                    this.updateTimer = setTimeout(function () {
                        subtitle.text(
                            prefix + 
                            sf("{0:^m 'minutes ago'}", 
                                new sf.TimeSpan(Date.now - this.lastSaved)
                            )
                        );
                        setTimer();
                    }, LAST_SAVED_UPDATE_FREQUENCY_SECONDS * 1000);
                }.bind(this);

            setTimer();
        };
    }
});
