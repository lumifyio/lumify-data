

define([
    './dropdowns/termForm/termForm',
    './dropdowns/statementForm/statementForm',
    'tpl!detail/toolbar/highlight',
    'util/css-stylesheet',
    'colorjs',
    'service/entity',
    'service/ontology'
], function(TermForm, StatementForm, highlightButtonTemplate, stylesheet, colorjs, EntityService, OntologyService) {

    var HIGHLIGHT_STYLES = [
            { name: 'None' },
            { name: 'Icons', selector:'icons' },
            { name: 'Underline', selector:'underline' },
            { name: 'Colors', selector:'colors' }
        ],
        DEFAULT = 2,
        useDefaultStyle = true;

    return withHighlighting;

    function withHighlighting() {
        this.entityService = new EntityService();
        this.ontologyService = new OntologyService();

        this.highlightButton = function() {
            return highlightButtonTemplate({
                styles: HIGHLIGHT_STYLES,
                activeStyle: this.getActiveStyle()
            });
        };

        this.defaultAttrs({
            resolvableSelector: '.text .entity',
            highlightTypeSelector: '.highlight-options a',
            highlightedWordsSelector: '.entity, .term, .artifact',
            draggablesSelector: '.resolved, .artifact, .generic-draggable'
        });

        // Automatically refresh draggables when request completes
        this.before('handleCancelling', function(xhr) {
            var self = this;
            xhr.success(function() {
                self.updateEntityAndArtifactDraggables();
            });
        });

        this.after('teardown', function() {
            $(document).off('selectionchange.detail');
            $(document).off('ignoreSelectionChanges.detail');
            $(document).off('resumeSelectionChanges.detail');
            $(document).off('termCreated');
            this.highlightNode().off('scrollstop');
        });

        this.after('initialize', function() {
            var self = this;

            // Allow components to disable selection listening
            $(document).on('ignoreSelectionChanges.detail', function() {
                $(document).off('selectionchange.detail');
            });
            $(document).on('resumeSelectionChanges.detail', function() {
                $(document).off('selectionchange.detail').on('selectionchange.detail', self.onSelectionChange.bind(self));
            });
            $(document).trigger('resumeSelectionChanges');

            this.highlightNode().on('scrollstop', this.updateEntityAndArtifactDraggables.bind(this));
            this.on('click', {
                resolvableSelector: this.onResolvableClicked,
                highlightTypeSelector: this.onHighlightTypeClicked
            });
            this.on(document, 'termCreated', this.updateEntityAndArtifactDraggables);

            this.applyHighlightStyle();
        });

        this.onHighlightTypeClicked = function(evt) {
            var target = $(evt.target),
                li = target.parents('li'),
                ul = li.parent('ul'),
                content = this.highlightNode();

            ul.find('.checked').not(li).removeClass('checked');
            li.addClass('checked');

            var newClass = li.data('selector');
            if (newClass) {
                content.addClass('highlight-' + newClass);
            } else this.removeHighlightClasses();
            useDefaultStyle = false;

            this.applyHighlightStyle();
        };

        this.highlightNode = function() {
            return this.$node.closest('.content');
        };

        this.getActiveStyle = function() {
            if (useDefaultStyle) {
                return DEFAULT;
            }

            var content = this.highlightNode(),
                index = 0;
            $.each( content.attr('class').split(/\s+/), function(_, item) {
                var match = item.match(/^highlight-(.+)$/);
                if (match) {
                    return HIGHLIGHT_STYLES.forEach(function(style, i) {
                        if (style.selector === match[1]) {
                            index = i;
                            return false;
                        }
                    });
                }
            });

            return index;
        };

        this.removeHighlightClasses = function() {
            var content = this.highlightNode();
            $.each( content.attr('class').split(/\s+/), function(index, item) {
                if (item.match(/^highlight-(.+)$/)) {
                    content.removeClass(item);
                }
            });
        };

        this.applyHighlightStyle = function() {
            var style = HIGHLIGHT_STYLES[this.getActiveStyle()];
            this.removeHighlightClasses();
            this.highlightNode().addClass('highlight-' + style.selector);

            if (!style.styleApplied) {

                this.ontologyService.concepts(function(err, concepts) {
                    var styleFile = 'tpl!detail/highlight-styles/' + style.selector + '.css';
                    require([styleFile], function(tpl) {
                        function apply(concept) {
                            if (concept.color) {
                                var STATES = {
                                        NORMAL: 0,
                                        HOVER: 1,
                                        DIM: 2
                                    },
                                    className = concept.className || 'entity.subType-' + concept.id,
                                    definition = function(state) {
                                        return tpl({ STATES:STATES, state:state, concept:concept, colorjs:colorjs });
                                    };

                                // Dim 
                                // (when dropdown is opened and it wasn't this entity)
                                stylesheet.addRule(
                                    '.highlight-' + style.selector + ' .dropdown .' + className,
                                    definition(STATES.DIM)
                                );

                                // Default style (or focused)
                                stylesheet.addRule(
                                    '.highlight-' + style.selector + ' .' + className +',' +
                                    '.highlight-' + style.selector + ' .dropdown .focused.' + className,
                                    definition(STATES.NORMAL)
                                );

                                // Drag-drop hover
                                stylesheet.addRule(
                                    '.highlight-' + style.selector + ' .drop-hover.' + className,
                                    definition(STATES.HOVER)
                                );

                                stylesheet.addRule('.concepticon-' + concept.id, 'background-image: url(' + concept.glyphIconHref + ')');
                            }
                            if (concept.children) {
                                concept.children.forEach(apply);
                            }
                        }
                        apply(concepts.tree);

                        // Artifacts
                        apply({
                            id: 'artifact',
                            className: 'artifact',
                            color: 'rgb(255,0,0)',
                            glyphIconHref: '/img/glyphicons/glyphicons_036_file@2x.png'
                        });

                        style.styleApplied = true;
                    });
                });
            }
        };


        this.onSelectionChange = function(e) {
            var selection = window.getSelection(),
                text = selection.type === 'Range' ? $.trim(selection.toString()) : '';

            // Ignore selection events within the dropdown
            if ( selection.type == 'None' ||
                 $(selection.anchorNode).is('.underneath') ||
                 $(selection.anchorNode).parents('.underneath').length ||
                 $(selection.focusNode).is('.underneath') ||
                 $(selection.focusNode).parents('.underneath').length) {
                return;
            }

            // Ignore if selection hasn't change
            if (text.length && text === this.previousSelection) {
                return;
            } else this.previousSelection = text;

            // Remove all dropdowns if empty selection
            if (selection.isCollapsed || text.length === 0) {
                this.tearDownDropdowns();
            }

            this.handleSelectionChange();
        };

        this.handleSelectionChange = _.debounce(function() {
            this.tearDownDropdowns();

            var sel = window.getSelection(),
                text = sel && sel.type === 'Range' ? $.trim(sel.toString()) : '';

            if (text && text.length) {
                var anchor = $(sel.anchorNode),
                    focus = $(sel.focusNode),
                    is = '.detail-pane .text';
                
                // Ignore outside content text
                if (anchor.parents(is).length === 0 || focus.parents(is).length === 0) {
                    return;
                }

                // Ignore if too long of selection
                var wordLength = text.split(/\s+/).length;
                if (wordLength > 10) {
                    return;
                }

                if (sel.rangeCount === 0) return;

                var range = sel.getRangeAt(0);

                // Avoid adding dropdown inside of entity
                var endContainer = range.endContainer;
                while (/entity/.test(endContainer.parentNode.className)) {
                    endContainer = endContainer.parentNode;
                }

                var isEndTextNode = endContainer.nodeType === 1;
                if (isEndTextNode) {
                    this.dropdownEntity(true, endContainer, sel, text);
                } else {

                    // Move to first space in end so as to not break up word when splitting
                    var i = Math.max(range.endOffset - 1, 0), character = '', whitespaceCheck = /^[^\s]$/;
                    do {
                        character = endContainer.textContent.substring(++i, i+1);
                    } while (whitespaceCheck.test(character));

                    endContainer.splitText(i);
                    this.dropdownEntity(true, endContainer, sel, text);
                }

            }
        }, 750);

        this.dropdownEntity = function(creating, insertAfterNode, sel, text) {
            this.tearDownDropdowns();

            var form = $('<div class="underneath"/>');
            $(insertAfterNode).after(form);
            TermForm.attachTo(form, {
                sign: text,
                selection: sel && { anchor:sel.anchorNode, focus:sel.focusNode, anchorOffset: sel.anchorOffset, focusOffset: sel.focusOffset, range:sel.rangeCount && sel.getRangeAt(0).cloneRange() },
                mentionNode: insertAfterNode,
                existing: !creating,
                artifactKey: this.attr.data._rowKey,
                artifactId: this.attr.data.graphVertexId
            });
        };

        this.onResolvableClicked = function(event) {
            var $target = $(event.target);
            if ($target.is('.underneath') || $target.parents('.underneath').length) {
                return;
            }
            _.defer(this.dropdownEntity.bind(this), false, $target);
        };

        this.updateEntityAndArtifactDraggables = function() {
            var self = this,
                words = this.select('draggablesSelector');

            // Filter list to those in visible scroll area
            words
                .withinScrollable(this.$node.closest('.content'))
                .draggable({
                    helper:'clone',
                    revert: 'invalid',
                    revertDuration: 250,
                    // scroll:true (default) requests position:relative on
                    // detail-pane .content, but that breaks dragging from
                    // detail-pane to graph.
                    scroll: false,
                    zIndex: 100,
                    distance: 10,
                    cursorAt: { left: -10, top: -10 },
                    start: function() {
                        $(this)
                            .parents('.text').addClass('drag-focus');
                    },
                    stop: function() {
                        $(this)
                            .parents('.text').removeClass('drag-focus');
                    }
                })
                .droppable({
                    activeClass: 'drop-target',
                    hoverClass: 'drop-hover',
                    tolerance: 'pointer',
                    accept: function(el) {
                        var item = $(el),
                            isEntity = item.is('.entity');

                        return isEntity;
                    },
                    drop: function(event, ui) {
                        var destTerm = $(this),
                            form = $('<div class="underneath"/>').insertAfter(destTerm);

                        self.tearDownDropdowns();

                        StatementForm.attachTo(form, {
                            sourceTerm: ui.draggable,
                            destTerm: destTerm
                        });
                    }
                });
        };

        this.tearDownDropdowns = function() {
            TermForm.teardownAll();
            StatementForm.teardownAll();
        };

    }
});
