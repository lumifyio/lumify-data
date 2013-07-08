define([
    'flight/lib/component',
    'tpl!./syncCursor'
], function(defineComponent, template) {
    'use strict';

    var CURSOR_RATE_LIMIT_PER_SECOND = 0.03;
    var SECOND = 1000;

    return defineComponent(SyncCursor);

	function SyncCursor() {

        this.defaultAttrs({
            bodySelector: 'body'
        });

        this.after('initialize', function() {
            this.on('mousemove', this.onMouseMove);
            this.on('focus', this.onFocus);
            this.on('blur', this.onBlur);

            this.on(document, 'syncCursorMove', this.onRemoteCursorMove);
            this.on(document, 'syncCursorFocus', this.onRemoteCursorFocus);
            this.on(document, 'syncCursorBlur', this.onRemoteCursorBlur);
            this.on(document, 'windowResize', this.updateWindowSize);

            this.cursorEl = $(template({name:this.attr.chatUser})).appendTo(document.body);

            this.updateWindowSize();
        });

        this.after('teardown', function() {
            this.cursorEl.remove();
            this.cursorEl = null;
        });

        this.updateWindowSize = function() {
            var w = $(window);
            this.windowWidth = w.width();
            this.windowHeight = w.height();
        };


        // Remote bound events trigger these

        this.onRemoteCursorMove = remoteHandler(function(e, data) {
            var buffer = 10,
                w = this.windowWidth,
                h = this.windowHeight;

            this.cursorEl.css({
                    display: 'block',
                    left: Math.min(w, data.x),
                    top: Math.min(h, data.y)
                })
                .toggleClass( 'offscreen-x', data.x > (w - buffer) )
                .toggleClass( 'offscreen-y', data.y > (h - buffer) );
        });
        this.onRemoteCursorFocus = remoteHandler(function(e) {
            this.cursorEl.addClass('focus');
        });
        this.onRemoteCursorBlur = remoteHandler(function(e) {
            this.cursorEl.removeClass('focus');
        });


        // Local handlers

        var timeout;
        this.update = function(e) {
            this.trigger(document, 'syncCursorMove', { 
                x: e.pageX, 
                y: e.pageY,
                w: this.windowWidth,
                h: this.windowHeight,
                name: this.attr.me || 'Unknown'
            });
            this.lastSend = Date.now();
        };

        this.onFocus = function() {
            this.trigger(document, 'syncCursorFocus', {});
        };

        this.onBlur = function() {
            this.trigger(document, 'syncCursorBlur', {});
        };

        this.onMouseMove = function(e) {
            var now = Date.now(),
                nextSendDate = this.lastSend ? this.lastSend + (SECOND * CURSOR_RATE_LIMIT_PER_SECOND) : now;

            clearTimeout(timeout);

            if ( now < nextSendDate ) {
                timeout = setTimeout(this.update.bind(this, e), nextSendDate - now);
                return;
            }

            this.update(e);
        };


        function remoteHandler(func) {
            return function(e, data) {
                if (data.remoteEvent) {
                    func.apply(this, arguments);
                }
            };
        }
    }
});

