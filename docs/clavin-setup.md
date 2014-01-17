## CLAVIN Integration

When integrating CLAVIN you must edit the configuration.properties to properly include either the disabled flag or the index directory.

### Building the CLAVIN index
1. Clone the source code:

    `$ git clone https://github.com/altamiracorp/CLAVIN -b stable/1/1/x`

2. Move into the CLAVIN directory:

    `$ cd CLAVIN`

3. Download allCountries.zip:

    `$ curl -O http://download.geonames.org/export/dump/allCountries.zip`

4. Unzip GeoNames:

    `unzip allCountries.zip`

5. Compile

    `mvn compile`

6. Create the Lucene Index (this may take a little while):

    `MAVEN_OPTS="-Xmx2048M" mvn exec:java -Dexec.mainClass="com.vericotech.clavin.WorkflowDemo"`

    If you encounter an error that looks like this:

    `... InvocationTargetException: Java heap space ...`

    set the appropriate environmental variable controlling Maven's memory usage, and increase the size with `export MAVEN_OPTS=-Xmx3g` or similar.
7. Make Clavin-Index directory

    `$ mkdir /opt/lumify/clavin-index`

8. Copy CLAVIN into Lumify

    `$ cp -a CLAVIN/IndexDirectory/. /opt/lumify/clavin-index`