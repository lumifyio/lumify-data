# Lumify-Twitter

Lumify-Twitter is an open source ingest example for the Lumify project. See the [Lumify website](http://lumify.io) for more information about Lumify.

## Build Requirements

* Please ensure that [Lumify] (https://github.com/altamiracorp/lumify/blob/master/README.md) has been installed before building.

## Integrating with [Lumify](https://lumify.io)

1. Generate Twitter API Keys. 
   * For instructions, please visit the [Twitter Developers site](https://dev.twitter.com/) or [Generating Twitter API Keys](#generating-twitter-api-keys) below. 
2. Add the following properties names and corresponding Twitter API keys to your ```/opt/lumify/config/configuration.properties:
	```
	twitter.consumerKey=
	twitter.consumerSecret=
	twitter.token=
	twitter.tokenSecret=
	twitter.query= # Keywords to search Twitter for, e.g. twitter
	```
3. Clone the repository from github using either of the links from the [main page](https://github.com/altamiracorp/lumify-twitter)
4. cd into your ```lumify-twitter``` directory
5. <a name="step-5"/>```mvn clean package```
6. ```cd target```
7. Copy the jar file to location of where you are running your Storm Topology for Lumify.
   * In the [Lumify Pre-Built VM](https://github.com/altamiracorp/lumify/blob/master/docs/PREBUILT_VM.md), please run the following command ```/opt/storm/bin/storm jar
   [location of jar file]
   com.altamiracorp.lumify.storm.twitter.StormRunner
```

Proceed if you are not using the Pre-built VM provided by [Lumify](https://lumify.io)

1. Deploy lumify storm topology.
2. Deploy lumify-twitter storm topology. 
3. Deploy web war file.

## Generating Twitter API Keys

1. Sign In to [Twitter Developers site](https://dev.twitter.com/user/login?destination=home) using your Twitter credentials.
<br/>
<br/>
![ScreenShot](https://raw.github.com/altamiracorp/lumify-twitter/master/docs/screenshots/twitter_sign_in.png)
<br/>
<br/>
2. In the top right corner hover over your Twitter Handler’s picture and select **My Applications** from the drop-down menu.
<br/>
<br/>
![ScreenShot](https://raw.github.com/altamiracorp/lumify-twitter/master/docs/screenshots/twitter_my_app.png)
<br/>
<br/>
3. Select **Create a new application** and fill out the form.
<br/>
<br/>
![ScreenShot](https://raw.github.com/altamiracorp/lumify-twitter/master/docs/screenshots/twitter_create_new_app.png)
<br/>
<br/>
4. Once completed, scroll down and select **Create my access token**.
<br/>
<br/>
![ScreenShot](https://raw.github.com/altamiracorp/lumify-twitter/master/docs/screenshots/twitter_access_token.png)
<br/>
<br/>
5. Refresh the page until you see Access Token, Access Token Secret, and Access Level under **Your Access Token**.


## Documentation

### Customizing Ontology

From the lumify-twitter directory: 

1. ```cd data/ontology```.
2. Modify ```twitter.owl```, to customize different concepts (e.g. person, phone number), properties for each concept, relationships between concepts, and/or glyphIcons associated with concepts.
   * After modifications, ```cd lumify-twitter/bin/importOntology.sh```.
3. Proceed from step 5 in [Integrating with Lumify](#step-5)

