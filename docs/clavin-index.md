The following instructions can be used to build the Lucene index of geonames data that CLAVIN uses.


## Build

    git clone https://github.com/altamiracorp/CLAVIN -b stable/1.1.x`
    cd CLAVIN

    curl -O http://download.geonames.org/export/dump/allCountries.zip
    unzip allCountries.zip

    mvn compile
    MAVEN_OPTS="-Xmx2048M" mvn exec:java -Dexec.mainClass="com.bericotech.clavin.WorkflowDemo"

If you encounter the following error:

>    ... InvocationTargetException: Java heap space ...

Then try:

    MAVEN_OPTS=-Xmx3G


## Install Locally

    mkdir -p /opt/lumify/clavin-index
    mv CLAVIN/IndexDirectory/* /opt/lumify/clavin-index


## Publish

    cd CLAVIN/IndexDirectory
    tar czf ../clavin-index-$(date +'%Y-%m-%d').tgz *

Copy the `.tgz` file to `http://bits.lumify.io/data` and update [../puppet/hiera/common.yaml](../puppet/hiera/common.yaml)
