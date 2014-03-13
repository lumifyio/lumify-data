# Lumify-Facebook

Lumify-Facebook is an closed source ingest example for the Lumify project. See the [Lumify website](http://lumify.io) for more information about Lumify.

## Build Requirements

* Please ensure that [Lumify] (https://github.com/altamiracorp/lumify/blob/master/README.md) has been installed before building.

## Integrating with [Lumify](https://lumify.io)

1. Generate Facebook App Keys.
   * For instructions, please visit the [Facebook Developers site](https://developers.facebook.com/) or [Generating Facebook API Keys](#generating-facebook-api-keys) below.
2. Add the following properties names and corresponding Facebook App keys to your ```/opt/lumify/config/configuration.properties:```

```
#Facebook Specific
facebook.appId=
facebook.appSecret=
facebook.accessToken=
facebook.fileProcessDirectory=#directory where the files are to be processed
# When querying for multiple phrases it must be a semi-colon separated list, e.g. twitter; face book; instagram
facebook.tables=#tables to query 
facebook.userPermissions=#information you are interested in about a user's profile
facebook.locationPermissions=#information you are interested in about posts from a specified location
facebook.latitude=#latitude of the interested location
facebook.longitude=#longitude of the interested location
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
![ScreenShot](https://raw.github.com/altamiracorp/lumify-all/master/lumify-facebook/docs/Screenshots/facebook_sign_in.png?token=1760800__eyJzY29wZSI6IlJhd0Jsb2I6YWx0YW1pcmFjb3JwL2x1bWlmeS1hbGwvbWFzdGVyL2x1bWlmeS1mYWNlYm9vay9kb2NzL1NjcmVlbnNob3RzL2ZhY2Vib29rX3NpZ25faW4ucG5nIiwiZXhwaXJlcyI6MTM4OTczNTQyN30%3D--20aed47ed9c61deb579f13385bf37559b3b008a6)
<br/>
<br/>
2. In the top right corner under your Facebook Profile’s picture and name, click the **Register Now** button.
<br/>
<br/>
![ScreenShot](https://raw.github.com/altamiracorp/lumify-all/master/lumify-facebook/docs/Screenshots/facebook_register_now.png?token=1760800__eyJzY29wZSI6IlJhd0Jsb2I6YWx0YW1pcmFjb3JwL2x1bWlmeS1hbGwvbWFzdGVyL2x1bWlmeS1mYWNlYm9vay9kb2NzL1NjcmVlbnNob3RzL2ZhY2Vib29rX3JlZ2lzdGVyX25vdy5wbmciLCJleHBpcmVzIjoxMzg5NzM1MzY0fQ%3D%3D--5a0f31c229a95d73f677dee933734f36b261917a)
<br/>
<br/>
3. Re-enter your Facebook Profile password and follow the prompts to properly register as a developer.
<br/>
<br/>
![ScreenShot](https://raw.github.com/altamiracorp/lumify-all/master/lumify-facebook/docs/Screenshots/facebook_developer_popup.png?token=1760800__eyJzY29wZSI6IlJhd0Jsb2I6YWx0YW1pcmFjb3JwL2x1bWlmeS1hbGwvbWFzdGVyL2x1bWlmeS1mYWNlYm9vay9kb2NzL1NjcmVlbnNob3RzL2ZhY2Vib29rX2RldmVsb3Blcl9wb3B1cC5wbmciLCJleHBpcmVzIjoxMzg5NzM1MzIwfQ%3D%3D--31b0344d96398783707c0689a567d77134e98f6b)
<br/>
<br/>
4. Select **Create New App** and fill out the form.
<br/>
<br/>
![ScreenShot](https://raw.github.com/altamiracorp/lumify-all/master/lumify-facebook/docs/Screenshots/facebook_create_new_app.png?token=1760800__eyJzY29wZSI6IlJhd0Jsb2I6YWx0YW1pcmFjb3JwL2x1bWlmeS1hbGwvbWFzdGVyL2x1bWlmeS1mYWNlYm9vay9kb2NzL1NjcmVlbnNob3RzL2ZhY2Vib29rX2NyZWF0ZV9uZXdfYXBwLnBuZyIsImV4cGlyZXMiOjEzODk3MzUyNzV9--3bc2d7f17e76e448dc39bb9a4d006501764c453a)
<br/>
<br/>
5. Once completed, the App ID and App Secret for the App will be shown.
6. To generate you Access Token, click **Tools** and select **Graph Explorer**.
<br/>
<br/>
![ScreenShot](https://raw.github.com/altamiracorp/lumify-all/master/lumify-facebook/docs/Screenshots/facebook_tools.png?token=1760800__eyJzY29wZSI6IlJhd0Jsb2I6YWx0YW1pcmFjb3JwL2x1bWlmeS1hbGwvbWFzdGVyL2x1bWlmeS1mYWNlYm9vay9kb2NzL1NjcmVlbnNob3RzL2ZhY2Vib29rX3Rvb2xzLnBuZyIsImV4cGlyZXMiOjEzODk3MzU0NDV9--c14b4eccbf72ef93a3fd10c98fe3856d0cef44a8)
<br/>
<br/>
7. Select **FQL Query**
<br/>
<br/>
![ScreenShot](https://raw.github.com/altamiracorp/lumify-all/master/lumify-facebook/docs/Screenshots/facebook_fql_selection.png?token=1760800__eyJzY29wZSI6IlJhd0Jsb2I6YWx0YW1pcmFjb3JwL2x1bWlmeS1hbGwvbWFzdGVyL2x1bWlmeS1mYWNlYm9vay9kb2NzL1NjcmVlbnNob3RzL2ZhY2Vib29rX2ZxbF9zZWxlY3Rpb24ucG5nIiwiZXhwaXJlcyI6MTM4OTczNTM0MX0%3D--0587458fb426927f64f96a50915a5e6aed151afd)
<br/>
<br/>
8. Click **Get Access Token** in the top right-hand corner and select the items you want access to then click **Get Access Token**.
<br/>
<br/>
![ScreenShot](https://raw.github.com/altamiracorp/lumify-all/master/lumify-facebook/docs/Screenshots/facebook_access_token.png?token=1760800__eyJzY29wZSI6IlJhd0Jsb2I6YWx0YW1pcmFjb3JwL2x1bWlmeS1hbGwvbWFzdGVyL2x1bWlmeS1mYWNlYm9vay9kb2NzL1NjcmVlbnNob3RzL2ZhY2Vib29rX2FjY2Vzc190b2tlbi5wbmciLCJleHBpcmVzIjoxMzg5NzM1MjAzfQ%3D%3D--137aada7e08bdbb620f0fd3d641d83b7f60ff56d)
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

License
=======

Copyright 2014 Altamira Technologies Corporation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
