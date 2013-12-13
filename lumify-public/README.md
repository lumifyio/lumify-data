# Lumify

![ScreenShot](lumify-web/src/main/webapp/img/lumify-logo.png?raw=true)

Lumify is an open source big data integration, analytics, and visualization platform. Please see http://lumify.io for more details.

## Getting Started

Lumify supplies a pre-built virtual machine to get you started quickly, but you can also build from source. Please choose your adventure from the linked documents below.

[Use the pre-built VM] (docs/PREBUILT_VM.md)

[Build from source] (docs/BUILD_FROM_SOURCE.md)

## Important Notes

* The following Titan properties are required on every vertex:
    * ```title```
    * ```_subType```: concept id
    * ```type```: type of concept (see [VertexType.java](core/src/main/java/com/altamiracorp/lumify/core/model/ontology/VertexType.java))
* Accumulo must have a row structure with a SHA256 hash for the row key

## License

Copyright 2013 Altamira Technologies Corporation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

