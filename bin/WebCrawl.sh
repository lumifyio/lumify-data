#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  CURRENTDIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$CURRENTDIR/$SOURCE"
done
CURRENTDIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${CURRENTDIR}/classpath.sh crawler)
if [ $? -ne 0 ]; then
    echo "${classpath}"
    exit
fi

if [ $# -eq 0 ] || [ "$1" == "--help" ]
then
    java \
    -Dfile.encoding=UTF-8 \
    -classpath ${classpath} \
    com.altamiracorp.reddawn.crawler.WebCrawl
    exit
fi

while [ $# -ne 0 ]
do
    case "$1" in
        --directory=* ) DIR=`cut -d "=" -f 2 <<< "$1"`
            ;;
        --provider=* ) PROVIDER=`cut -d "=" -f 2 <<< "$1"`
            ;;
        --result-count=* ) COUNT=`cut -d "=" -f 2 <<< "$1"`
            ;;
        --query=* ) QUERY=`cut -d "=" -f 2 <<< "$1"`
            ;;
        --subreddit=* ) SUBREDDIT=`cut -d "=" -f 2 <<< "$1"`
            ;;
        --rss=* ) RSS=`cut -d "=" -f 2 <<< "$1"`
            ;;
    esac
    shift
done

if [ -z "$DIR" ]
then
    DIR="${CURRENTDIR}/../data/searcher/"
    if [ ! -e $DIR ]
    then 
	    mkdir -p $DIR
    fi
fi


if [ -z "$PROVIDER" ]
then
    echo "You must specify a provider"
    exit
fi

PROVIDERS=',' read -a array <<< "$PROVIDER"
for ENTRY in "${PROVIDERS[@]}"
do
    case "$ENTRY" in
        *reddit* )
            if [ -z "$COUNT" ]
            then
                echo "You must specify a result count for the reddit search engine"
                exit
            fi
            ;;
        *rss* )
            if [ -z "$RSS" ]
            then
                echo "You must specify a URL link if using the rss provider"
                exit
            fi
            ;;
        * )
            if [ -z "$QUERY" ] || [ -z "$COUNT" ]
            then
                echo "You must specify both a query and a result count for the providers you listed"
                exit
            fi
            ;;
    esac
done

JAVAPARAMS=("--provider=$PROVIDER" "--directory=$DIR");
if [ -n "$QUERY" ]
then
    JAVAPARAMS=("${JAVAPARAMS[@]}" "--query=$QUERY")
fi
if [ -n "$RSS" ]
then
    JAVAPARAMS=("${JAVAPARAMS[@]}" "--rss=$RSS")
fi
if [ -n "$COUNT" ]
then
    JAVAPARAMS=("${JAVAPARAMS[@]}" "--result-count=$COUNT")
fi
if [ -n "$SUBREDDIT" ]
then
    JAVAPARAMS=("${JAVAPARAMS[@]}" "--subreddit=$SUBREDDIT")
fi

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
com.altamiracorp.reddawn.crawler.WebCrawl \
"${JAVAPARAMS[@]}"
