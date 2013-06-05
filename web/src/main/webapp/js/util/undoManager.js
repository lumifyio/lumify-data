
define([
], function() {
    'use strict';

    /**
     * Manages undo,redo stack
     *
     * After an action:
     *
     *  undoManager.performedAction( [action], undoFunction, redoFunction);
     */
    function UndoManager() {
        var $this = this;

        this.undos = [];
        this.redos = [];
        
        $(function() {
            $(document).on({
                'keydown': $this._handleKey.bind($this),
                'keyup': $this._handleKey.bind($this)
            });
        });
    }


    UndoManager.prototype.performedAction = function(name, options) {
        if ( name && 
             options && 
             typeof options.undo === 'function' &&
             typeof options.redo === 'function' ) {

            this.undos.push({
                name: name,
                undo: options.undo.bind(options.bind || this),
                redo: options.redo.bind(options.bind || this)
            });
        } else console.log('Invalid performedAction arguments');
    };

    UndoManager.prototype.performUndo = function() {
        _performWithStacks('undo', this.undos, this.redos);
    };

    UndoManager.prototype.performRedo = function() {
        _performWithStacks('redo', this.redos, this.undos);
    };

    UndoManager.prototype._isUndo = function(character, event) {
        return character === 'Z' && event.metaKey && !event.shiftKey;
    };

    UndoManager.prototype._isRedo = function(character, event) {
        return ( 
            // Windows
            (character === 'Y' && event.ctrlKey) || 
            // Mac
            (character === 'Z' && event.metaKey && event.shiftKey)
        );
    };

    UndoManager.prototype._handleKey = function(event) {
        var character = String.fromCharCode(event.which).toUpperCase();

        if ( this._isUndo(character, event) ) {
            this.performUndo();
            event.preventDefault();
        }

        if ( this._isRedo(character, event) ) {
            this.performRedo();
            event.preventDefault();
        }
    };

    function _performWithStacks(name, stack1, stack2) {
        var action, undo;

        if (stack1.length) {
            action = stack1.pop();
            undo = action.undo;

            undo();

            stack2.push({
                action: action.name,
                undo: action.redo,
                redo: action.undo
            });

        } else {
            // TODO: give user feedback (flash the screen) / beep
            console.log('TODO: alert user. Nothing to ' + name);                
        }
    }

    return new UndoManager();
});
