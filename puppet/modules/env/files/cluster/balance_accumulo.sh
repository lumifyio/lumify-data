#!/bin/bash

function show_help {
    echo "Usage: balance_accumulo.sh [OPTION]"
    echo ""
    echo "Options:"
    echo "  -c, --count   Number of groups you would like"
    exit 1
}

GROUP_COUNT=2

while getopts "h?c:" opt; do
    case "$opt" in
    h|\?)
        show_help
        exit 0
        ;;
    c)
        GROUP_COUNT=$OPTARG
        ;;
    *)
        echo "Invalid option $opt"
        ;;
    esac
done

GROUP_SIZE=$(expr $((0x10000)) / $GROUP_COUNT)

SPLIT_FILE="/tmp/$(basename $0).$$.splits.tmp"
echo -n "" > $SPLIT_FILE

for (( i = 1; i < $GROUP_COUNT; i++ ))
do
    SPLIT=$(expr $i \* $GROUP_SIZE)
    SPLIT_HEX=$(printf 'urn\x1Fsha256\x1F%04x' $SPLIT)
    echo $SPLIT_HEX >> $SPLIT_FILE
done

SCRIPT_FILE="/tmp/$(basename $0).$$.tmp"
echo -n "" > $SCRIPT_FILE

echo "addsplits -t Artifact --splits-file $SPLIT_FILE" >> $SCRIPT_FILE
echo "addsplits -t atc_TermMention --splits-file $SPLIT_FILE" >> $SCRIPT_FILE
echo "addsplits -t atc_VideoFrame --splits-file $SPLIT_FILE" >> $SCRIPT_FILE

LAST_SPLIT_HEX=0
for (( i = 1; i <= $GROUP_COUNT; i++ ))
do
    if [[ $i == $GROUP_COUNT ]]
    then
        SPLIT_HEX=$(printf 'urn\\x1Fsha256\\x1Fffff')
    else
        SPLIT=$(expr $i \* $GROUP_SIZE)
        SPLIT_HEX=$(printf 'urn\\x1Fsha256\\x1F%04x' $SPLIT)
    fi
    echo "merge -t Artifact --begin-row $LAST_SPLIT_HEX --end-row $SPLIT_HEX" >> $SCRIPT_FILE
    echo "merge -t atc_TermMention --begin-row $LAST_SPLIT_HEX --end-row $SPLIT_HEX" >> $SCRIPT_FILE
    echo "merge -t atc_VideoFrame --begin-row $LAST_SPLIT_HEX --end-row $SPLIT_HEX" >> $SCRIPT_FILE
    LAST_SPLIT_HEX=$SPLIT_HEX
done

echo "Run the following script in your accumulo shell: $SCRIPT_FILE"
