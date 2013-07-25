#!/usr/bin/env node

var tablesLabel = "<<< tables >>>";

var yaml = require("js-yaml");
var builder = require("DOMBuilder");

var fs = require("fs");
fs.readFile(__dirname + '/../template.gv', 'utf8', parseTemplate);

function parseTemplate(err, data) {
    if(err) {
      return console.log(err);
    }

    var start = data.indexOf(tablesLabel);
    var end = start + tablesLabel.length;

    var template = data.substring(0, start);
    fs.readdirSync(__dirname + '/../yaml/').forEach(function(filename) {
        if(/\.ya?ml$/.test(filename)) {
            var tableJson = require("../yaml/" + filename);
            template += buildTableHtml(tableJson);
            for(var key in tableJson.connections) {
                template += buildConnectionDot(tableJson.table.label, key, tableJson.connections[key]);
            }
        } else {
            console.log("Skipped processing of:", filename);
        }
    });

    template += data.substring(end);
    fs.writeFile(__dirname + '/../build/erd.gv', template, function(err) {
        if(err) {
           console.log(err);
        } else {
           console.log("The file was saved!");
        }
    });
}

function buildTableHtml(json) {
    var domObj =
        ['TABLE', {'BORDER': '1', 'CELLBORDER': '0', 'CELLSPACING': '0', 'CELLPADDING': '4'},
            ['TR',
                ['TD', {'COLSPAN': '4', 'ALIGN': 'CENTER'},
                    ['FONT', {'POINT-SIZE': '20'}, json.table.label]
                ]
            ]
        ];

    if(typeof json.rowKey != 'undefined' && json.rowKey !== null) {
        domObj = domObj.concat(buildColumnFamilyHtml('rowKey', json.rowKey));
    }

    for(var key in json.columnFamilies) {
        domObj = domObj.concat(buildColumnFamilyHtml(key, json.columnFamilies[key]));
    }

    return '"' + json.table.label + '" [label=<' + builder.build(domObj, 'html').toString() + '>];\n';
}

function buildColumnFamilyHtml(colFamKey, json) {
    var response = [
        ['TR',
            ['TD', {'COLSPAN': '4'},
                ['TABLE', {'HEIGHT': '0', 'BORDER': '0', 'BGCOLOR': '#333333', 'CELLBORDER': '0', 'CELLSPACING': '0', 'CELLPADDING': '0'},
                    ['TR',
                        ['TD', '']
                    ]
                ]
            ]
        ],
        ['TR',
            ['TD', {'COLSPAN': '4', 'COLOR': 'GREY', 'ALIGN': 'LEFT'},
                ['FONT', {'COLOR': '#888888', 'POINT-SIZE': '16'}, colFamKey]
            ]
        ]
    ];

    for(var key in json) {
        var columnData = (json[key] != null) ? json[key].split(' - ') : new Array('N/A');
        response.push(
            ['TR',
                ['TD', {'ALIGN': 'LEFT', 'WIDTH': '100'}, ''],
                ['TD', {'ALIGN': 'LEFT'}, key],
                ['TD', {'ALIGN': 'LEFT'},
                    ['FONT', {'COLOR': '#444444'}, columnData[0].trim()]
                ],
                ['TD', {'ALIGN': 'LEFT'}, (columnData.length > 1 ? columnData[1].trim() : '')]
            ]
        );
    }

    return response;
}

function buildConnectionDot(from, to, relationship) {
    var quantifiers = relationship.split('->');
    return '"' + from + '" -> "' + to + '" [arrowtail=' + getArrowheadName(quantifiers[0].trim()) +
            ', arrowhead=' + getArrowheadName(quantifiers[1].trim()) + ', dir=both]\n';
}

function getArrowheadName(quantifier) {
    switch (quantifier) {
        case '1+':
        case 'many':
            return 'crowtee';
        case '0+':
            return 'crowodot';
        case '01':
            return 'teeodot';
        case '1':
        case 'one':
            return 'teetee';
        default:
            return 'none';
    }
}