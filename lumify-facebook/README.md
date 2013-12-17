# Lumify-Facebook

Lumify-Facebook is an closed source ingest example for the Lumify project. See the [Lumify website](http://lumify.io) for more information about Lumify.

## Build Requirements

* Please ensure that [Lumify] (https://github.com/altamiracorp/lumify/blob/master/README.md) has been installed before building.

## Integrating with [Lumify](https://lumify.io)

1. Generate Facebook App Keys.
   * For instructions, please visit the [Facebook Developers site](https://developers.facebook.com/) or [Generating Facebook API Keys](#generating-facebook-api-keys) below.
2. Add the following properties names and corresponding Facebook App keys to your ```/opt/lumify/config/configuration.properties:```

```
facebook.appId=
facebook.appSecret=
facebook.accessToken=
# When querying for multiple phrases it must be a semi-colon separated list, e.g. twitter; face book; instagram
facebook.userPermissions=#information you are interested in about a user's profile
facebook.locationPermissions=#information you are interested in about posts from a specified location
facebook.longitude=#longitude of the interested location
facebook.latitude=#latitude of the interested location
facebook.distance=#radius distance around the given location
```

3. Clone the repository from github using either of the links from the [main page](https://github.com/altamiracorp/lumify-facebook)
4. cd into your ```lumify-facebook``` directory
5. <a name="step-5"/>```mvn clean package```
6. ```cd target```
7. Copy the jar file to location of where you are running your Storm Topology for Lumify.
   * In the [Lumify Pre-Built VM](https://github.com/altamiracorp/lumify/blob/master/docs/PREBUILT_VM.md), please run the following command ```/opt/storm/bin/storm jar
   [location of jar file]
   com.altamiracorp.lumify.storm.facebook.StormRunner
```

Proceed if you are not using the Pre-built VM provided by [Lumify](https://lumify.io)

1. Deploy lumify storm topology.
2. Deploy lumify-facebook storm topology.
3. Deploy web war file.

## Generating Facebook API Keys

1.Sign In to [Facebook Developers site](https://developers.facebook.com/) using your Facebook credentials.
<br/>
<br/>
![ScreenShot](https://raw.github.com/altamiracorp/lumify-twitter/master/docs/screenshots/facebook_sign_in.png)
<br/>
<br/>
2. In the top right corner under your Facebook Profile’s picture and name, click the **Register Now** button.
<br/>
<br/>
![ScreenShot](https://raw.github.com/altamiracorp/lumify-twitter/master/docs/screenshots/facebook_register_now.png)
<br/>
<br/>
3. Re-enter your Facebook Profile password and follow the prompts to properly register as a developer.
<br/>
<br/>
![ScreenShot](https://raw.github.com/altamiracorp/lumify-twitter/master/docs/screenshots/facebook_developer_popup.png)
<br/>
<br/>
4. Select **Create New App** and fill out the form.
<br/>
<br/>
![ScreenShot](https://raw.github.com/altamiracorp/lumify-twitter/master/docs/screenshots/facebook_create_new_app.png)
<br/>
<br/>
5. Once completed, the App ID and App Secret for the App will be shown.
6. To generate you Access Token, search for **graph api explorer** and select the top result.
<br/>
<br/>
![ScreenShot](https://raw.github.com/altamiracorp/lumify-twitter/master/docs/screenshots/facebook_search_explorer.png)
<br/>
<br/>
7. Select **FQL Query**
<br/>
<br/>
![ScreenShot](https://raw.github.com/altamiracorp/lumify-twitter/master/docs/screenshots/facebook_fql_selection.png)
<br/>
<br/>
8. Click **Get Access Token** in the top right-hand corner and select the items you want access to then click **Get Access Token**.
<br/>
<br/>
![ScreenShot](https://raw.github.com/altamiracorp/lumify-twitter/master/docs/screenshots/facebook_access_token.png)
<br/>
<br/>
9. The Access Token should appear in the associated text box.


## Documentation

### Customizing Ontology

From the lumify-facebook directory:

1. ```cd data/ontology```.
2. Modify ```facebook.owl```, to customize different concepts (e.g. person, phone number), properties for each concept, relationships between concepts, and/or glyphIcons associated with concepts.
   * After modifications, ```cd lumify-facebook/bin/importOntology.sh```.
3. Proceed from step 5 in [Integrating with Lumify](#step-5)

