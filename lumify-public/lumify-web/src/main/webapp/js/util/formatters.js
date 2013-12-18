
define(['sf'], function() {
    'use strict';

    var isMac = checkIfMac(),
        keyboardMappings = {
            metaIcons: {
                shift: isMac ? '⇧' : 'Shift',
                meta: isMac ? '⌘' : 'Ctrl',
                ctrl: isMac ? '⌃' : 'Ctrl',
                alt: isMac ? '⌥' : 'Alt'
            },
            charIcons: {
                esc: isMac ? '⎋' : null,
                'escape': isMac ? '⎋' : 'esc',
                'delete': isMac ? '⌫' : null,
                backspace: isMac ? '⌦' : null,
                up: '↑', 
                down: '↓',
                left: '←', 
                right: '→',
                drag: isMac ? '' : null
            }
        };

    function checkIfMac() {
        return ~navigator.userAgent.indexOf('Mac OS X');
    }

    function codeForCharacter(character) {
        var special = {
          backspace: 8, tab: 9, clear: 12,
          enter: 13, 'return': 13,
          esc: 27, 'escape': 27, space: 32,
          left: 37, up: 38,
          right: 39, down: 40,
          del: 46, 'delete': 46,
          home: 36, end: 35,
          pageup: 33, pagedown: 34,
          ',': 188, '.': 190, '/': 191,
          '`': 192, '-': 189, '=': 187,
          ';': 186, '\'': 222,
          '[': 219, ']': 221, '\\': 220
        };

        // Set delete to equal backspace on mac
        if (isMac) {
            special.del = special['delete'] = special.backspace;
        }

        return special[character.toLowerCase()] || character.toUpperCase().charCodeAt(0);
    }



    var FORMATTERS = {
        number: {
            pretty: function(number) {
                if (number >= 10000) {
                    return (number / 10000).toFixed(1).replace(/.0$/,'') + 'K';
                } else return sf('{0:#,###}', number);
            }
        },
        object: {
            shortcut: function(key) {
                var normalized = key.replace(/\+/g, '-').toUpperCase(),
                    forLookup = normalized,
                    parts = normalized !== '-' ? normalized.split('-') : ['-'],
                    shortcut = {normalized:normalized, forEventLookup:normalized};

                if (parts.length === 1) {
                    shortcut.keyCode = codeForCharacter(parts[0]);
                    shortcut.character = parts[0];
                    shortcut.forEventLookup = shortcut.keyCode;
                } else if (parts.length === 2) {
                    shortcut.keyCode = codeForCharacter(parts[1]);
                    shortcut.character = parts[1];
                    shortcut[parts[0].toLowerCase() + 'Key'] = true;
                    shortcut.forEventLookup = parts[0] + '-' + shortcut.keyCode;
                } else return console.warn('Unable to add shortcut ', key);

                return shortcut;
            }
        },
        string: {
            shortcut: function(character, metaKeys) {
                if (!metaKeys) {
                    metaKeys = FORMATTERS.object.shortcut(character);
                    character = metaKeys.character;
                }

                var metaIcon;

                character = keyboardMappings.charIcons[character.toLowerCase()] || (character.length > 1 ? character.toLowerCase() : character);

                if (metaKeys.metaKey)
                    metaIcon = keyboardMappings.metaIcons.meta;
                else if (metaKeys.ctrlKey)
                    metaIcon = keyboardMappings.metaIcons.ctrl;
                else if (metaKeys.altKey)
                    metaIcon = keyboardMappings.metaIcons.alt;
                else if (metaKeys.shiftKey)
                    metaIcon = keyboardMappings.metaIcons.shift;

                if (metaIcon) {
                    return metaIcon + (isMac ? '' : '+') + character;
                }

                return character;
            },
            plural: function(count, singular, plural) {
                plural = plural || (singular + 's');

                switch(count) {
                    case 0: return 'No ' + plural;
                    case 1: return '1 ' + singular;
                    default: return count + ' ' + plural;
                }
            }
        },
        date: {
            relativeToNow: function(date) {
                date = _.isDate(date) ? date : new Date(date);

                var span = new sf.TimeSpan(Date.now() - date),
                    time = '';

                if (span.years > 1) {
                    time = sf("{0:^y 'years'}", span);
                } else if (span.years === 1) {
                    time = 'a year';
                } else if (span.months > 1) {
                    time = sf("{0:^M 'months'}", span);
                } else if (span.months === 1) {
                    time = 'a month';
                } else if (span.days > 1) {
                    time = sf("{0:^d 'days'}", span);
                } else if (span.days === 1) {
                    time = 'a day';
                } else if (span.hours > 1) {
                    time = sf("{0:^h 'hours'}", span);
                } else if (span.hours === 1) {
                    time = 'an hour';
                } else if (span.minutes > 1) {
                    time = sf("{0:^m 'minutes'}", span);
                } else if (span.minutes === 1) {
                    time = 'a minute';
                } else {
                    time = 'moments';
                }

                return time + ' ago';
            }
        }
    }


    return FORMATTERS;
});
