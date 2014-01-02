#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

OUT_DIR=${DIR}/build

mkdir -p ${OUT_DIR}
node ${DIR}/node_modules/flight-doc --ignore-event=error ${DIR}/../web/src/main/webapp/js > ${OUT_DIR}/flight-doc.gv
dot ${OUT_DIR}/flight-doc.gv | neato -n -Tsvg -o${OUT_DIR}/flight-doc.svg
SVG_TEXT=`cat ${OUT_DIR}/flight-doc.svg`

cat > ${OUT_DIR}/flight-doc.html <<EOF
<html>
<head>
    <title>Flight Docs</title>
    <script type="text/javascript" src="../jquery.js"></script>
    <style>
      .over text {
        font-weight: bold;
      }

      .over path {
        stroke-width: 4px;
      }
    </style>
</head>
<body>
<svg>
    ${SVG_TEXT}
</svg>
<script>
    \$(function() {
        \$('.edge').hover(
            function() {
                var clazz = \$(this).attr('class');
                \$(this).attr('class', clazz + ' over');
            },
            function() {
                var clazz = \$(this).attr('class');
                \$(this).attr('class', clazz.replace(/ over/g, ''));
            });
    })
</script>
</body>
</html>
EOF
