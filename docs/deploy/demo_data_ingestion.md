# Demo Data Ingestion

-	Stop Puppet Agent on the Web Server while we ingest data
	
	```
	<local> $ 
	<local> $ ssh -A root@10.0.3.YN
	<web-server> $ service puppet stop
	```
-	Start yarn jobs

	```
	<yarn-server> $ su - yarn
	
	// ensure no processes are running (this should be an empty list)
	<yarn-server> $ yarn application -list
	// if processes are running, kill them
	<yarn-server> $ yarn application -kill <ID>
	
	// start jobs
	<yarn-server> $ yarn jar /tmp/yarn/lumify-graph-property-worker-yarn-${VERSION}-with-dependencies.jar \
	                -jar /tmp/yarn/lumify-graph-property-worker-yarn-${VERSION}-with-dependencies.jar \
	                -envPATH=$PATH -envLD_LIBRARY_PATH=/usr/local/lib:/usr/local/share/OpenCV/java
	<yarn-server> $ yarn jar /tmp/yarn/lumify-long-running-process-yarn-${VERSION}-with-dependencies.jar \
	                -jar /tmp/yarn/lumify-long-running-process-yarn-${VERSION}-with-dependencies.jar
	
	// verify jobs are running (this should show both applications)
	<yarn-server> $ yarn application -list                
	``` 
-	Build the Sinaloa data set

	```
	<local> $ cd $LUMIFY_ALL/lumify-sinaloa
	<local> $ ./build.sh
	<local> $ scp build/sinaloa-data* root@$NODE_IP:~
	```
-	Login to web application with any user account (https://$NODE_IP)
-	List users to identify desired admin user, note User ID

	```
	<web-server> $ java -jar lumify-cli-0.4.1-SNAPSHOT-with-dependencies.jar UserAdmin list
	```
-	Grant all privileges to user to allow for data ingestion

	```
	<web-server> $ java -jar lumify-cli-0.4.1-SNAPSHOT-with-dependencies.jar UserAdmin set-privileges \
	               -i <user_id> -p ALL
	```
-	Upload Sinaloa ontology
	-	Login to web application with admin account (https://$NODE_IP)
	-	Click on `Admin` icon
	-	Select `Upload` under `ONTOLOGY`
	-	Upload the `$LUMIFY_ALL/lumify-sinaloa/build/sinaloa-ontology.zip` file
	-	Refresh browser once `Upload Successful` message is displayed
-	Unzip sinaloa-data packages and import data

	```
	// if no unzip
	<yarn-server> $ yum install unzip
	
	<yarn-server> $ unzip sinaloa-data.zip
	<yarn-server> $ unzip sinaloa-data-rdf.zip
	
	<yarn-server> $ java -jar lumify-cli-${VERSION}-with-dependencies.jar RdfImport \
	                -infile sinaloa-data-rdf/sinaloa.rdf.xml
	<yarn-server> $ java -jar lumify-cli-${VERSION}-with-dependencies.jar Import \
	                -datadir sinaloa-data
	```

-	Start Puppet Agent

	```
	<web-server> $ service puppet start
	```
