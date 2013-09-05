
define( [], function() {

    $.ui.plugin.add( "draggable", "multi", {


        create: function(e, ui) {
            var inst = this.data("ui-draggable");
            this.attr("tabindex", "0");
            this.addClass("multi-select");

            if ( inst.options.revert === 'invalid' ) {

                inst.options.revert = function(dropped) {
                    var reverted = inst.reverted = !dropped;

                    if (reverted && inst.alsoDragging) {
                        inst.alsoDragging.each(function() {
                            this.animate(this.data('original').offset(),
                                parseInt(inst.options.revertDuration, 10),
                                function() {
                                    this.remove();
                                }
                            );
                        });
                    }

                    return reverted;
                };
            }

            this.on('click', function(evt) {
                evt.preventDefault();
                this.focus();

                var $target = $(evt.target).parents('li'),
                    list = $target.parent(),
                    lastSelection = list.data('lastSelection'),
                    selected = list.find('.active');

                if (evt.shiftKey && lastSelection) {

                    // Handle contiguous selection
                    var targetIndex = $target.index(),
                        previousIndex = lastSelection.index();

                    if (targetIndex !== previousIndex) {
                        $target[targetIndex < previousIndex ? 'nextUntil' : 'prevUntil'](lastSelection)
                        .andSelf()
                        .addClass('active');
                    }

                } else if (evt.metaKey) {

                    // Just add this
                    $target.addClass('active');

                } else {

                    selected.not($target).removeClass('active');
                    $target.addClass('active');
                }

                // Keep track of last selected for shift-selection later
                list.data('lastSelection', $target);

                onSelection(evt, list.find('.active'));
            });

            this.on('keyup', function(evt) {
                if ((evt.metaKey || event.ctrlKey) && evt.which === 65) {
                    evt.preventDefault();
                    var allItems = $(evt.target).parents('li').parent().children('li');
                    allItems.addClass('active');
                    onSelection(evt, allItems);
                }
            });

            function onSelection(evt, elements) {
                if ( inst.options.selection ) {
                    inst.options.selection.call(
                        inst.element,
                        evt,
                        $.extend(inst._uiHash(), {
                            selected: elements
                        }));
                }
            }
        },

        start: function(e, ui) {
            if (arguments.length > 2) {
                console.warn("Switch plugin to use [instance] argument instead of data");
            }

            var instance = this.data("ui-draggable"),
                helper = ui.helper,
                anchor = $(this),
                item = anchor.parent('li'),
                list = item.parent(),
                width = anchor.width();

            instance.reverted = false;

            // Make clone the same width
            helper.width(width);

            // Hovers while moving makes it slower as the browser
            // displays the url, so just make the link invalid
            helper.removeAttr('href');

            // If dragging a selected node, bring along other selected
            // items
            if ( item.hasClass('active') ) {
                instance.alsoDragging = list.find('.active a')
                    .not(anchor)
                    .map(function() {
                        var $this = $(this),
                            style = $this.offset(),
                            cloned = $this.clone().removeAttr('id href').data('original', $this);

                        cloned.addClass('ui-draggable-dragging');
                        if (instance.options.otherDraggablesClass) {
                            cloned.addClass(instance.options.otherDraggablesClass);
                        }
                        style.width = width;
                        style.position = 'absolute';
                        style.zIndex = 100;

                        return cloned.css(style);
                    })
                    .appendTo('body');

            } else {
                instance.alsoDragging = false;
            }
        },

        drag: function(ev, ui) {
            var instance = this.data("ui-draggable");
            if ( !instance.alsoDragging || !instance.alsoDragging.length ) {
                return;
            }

            var helper = $(ui.helper),
                currentLoc = helper.offset(),
                prevLoc = helper.data('prevLoc') || ui.originalPosition,
                offsetTop = currentLoc.top-prevLoc.top;

            instance.reverted = false;
            instance.alsoDragging.each(function(){
                var p = this.offset();

                this.css({
                    left: currentLoc.left,
                    top: p.top + offsetTop
                });
            });
            helper.data('prevLoc', currentLoc);
        },

        stop: function(ev, ui){
            var inst = this.data("ui-draggable");
            if ( !inst.alsoDragging || !inst.alsoDragging.length ) {
                return;
            }

            if ( !inst.reverted && inst.options.otherDraggables ) {
                inst.options.otherDraggables.call(
                    inst.element, 
                    ev, 
                    $.extend(inst._uiHash(), { 
                        otherDraggables: inst.alsoDragging 
                    }));

            }

            if ( ! inst.reverted ) {
                inst.alsoDragging.each(function() {
                    this.remove();
                });
            }

            inst.alsoDragging = false;
        }
    });

});
