#!/usr/bin/env node

var tablesLabel = "<<< tables >>>";

var yaml = require("js-yaml");
var builder = require("DOMBuilder");

// Loads and converts to json
var artifact = require("../yaml/artifact.yaml");

var fs = require("fs");
fs.readFile(__dirname + '/../template.gv', 'utf8', parseTemplate);

function parseTemplate(err, data) {
    if(err) {
      return console.log(err);
    }

    var start = data.indexOf(tablesLabel);
    var end = start + tablesLabel.length;

    var template = data.substring(0, start) + buildTableHtml(artifact) + data.substring(end);
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
        ['TABLE', {'BORDER': '0', 'CELLBORDER': '0', 'CELLSPACING': '0', 'CELLPADDING': '4'},
            ['TR',
                ['TD', {'COLSPAN': '4', 'ALIGN': 'CENTER'},
                    ['FONT', {'POINT-SIZE': '20'}, json.table.label]
                ]
            ]
        ];

    domObj = domObj.concat(buildColumnFamilyHtml('rowKey', json.rowKey));

    for(var key in json.columnFamilies) {
        domObj = domObj.concat(buildColumnFamilyHtml(key, json.columnFamilies[key]));
    }

    return '"' + json.table.label + '" [label=<' + builder.build(domObj, 'html').toString() + '>];\n';
}

function buildColumnFamilyHtml(colFamKey, json) {
    var response = [
        ['TR', {'CELLPADDING': '0'},
            ['TD', {'HEIGHT': '0', 'BGCOLOR': 'BLACK', 'COLSPAN': '4'}]
        ],
        ['TR',
            ['TD', {'COLSPAN': '4', 'COLOR': 'GREY', 'ALIGN': 'LEFT'},
                ['FONT', {'COLOR': '#888888', 'POINT-SIZE': '16'}, colFamKey]
            ]
        ]
    ];

    for(var key in json) {
        var columnData = json[key].split(' - ');
        response.push(
            ['TR',
                ['TD', {'ALIGN': 'LEFT', 'WIDTH': '100'}, ''],
                ['TD', {'ALIGN': 'LEFT'}, key],
                ['TD', {'ALIGN': 'LEFT'},
                    ['FONT', {'COLOR': '#444444'}, columnData[0].trim()]
                ],
                ['TD', {'ALIGN': 'LEFT'}, columnData[1].trim()]
            ]
        );
    }

    return response;
}