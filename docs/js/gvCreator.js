#!/usr/bin/env node

var tablesLabel = "<<< tables >>>";

var yaml = require("js-yaml");

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

    var template = data.substring(0, start) + buildTableDot(artifact) + data.substring(end);
    fs.writeFile(__dirname + '/../build/erd.gv', template, function(err) {
        if(err) {
           console.log(err);
        } else {
           console.log("The file was saved!");
        }
    });
}

function buildTableDot(json) {
    var result = '"' + json.table.label + '" [label="' + json.table.label + ' | ';

    result += '{ rowKey | {';
    var firstKey = true;
    json.rowKey.forEach(function(keyPart) {
        if(!firstKey) {
            result += ' | ';
        } else {
            firstKey = false;
        }

        result += keyPart;
    });
    result += '} }';

    json.columnFamilies.forEach(function(colFam) {
        result += ' | ' + buildColumnFamilyDot(colFam);
    });

    result += '"];\n';
    return result;
}

function buildColumnFamilyDot(columnFamilyJson) {
    var colFamKey = "";
    for(var key in columnFamilyJson) {
        colFamKey = key;
    }

    var result = '{ ' + colFamKey + ' | { ';

    var internalJson = columnFamilyJson[colFamKey];
    var firstColumn = true;
    for(var key in internalJson) {
        if(!firstColumn) {
            result += ' | ';
        } else {
            firstColumn = false;
        }

        var columnData = internalJson[key].split(' - ');
        result += key + ' (' + columnData[0].trim() + ') : ' + columnData[1].trim();
    }

    result += '} } ';
    return result;
}