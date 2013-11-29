# Lumify

![ScreenShot](https://github.com/nearinfinity/lumify-enterprise/tree/develop/docs/web/src/main/webapp/img/lumify-icon.png)

Lumify is an open source big data analytical tool and knowledge discovery. See the [project page] (http://lumify.io) for more details. Basically, it's a way to aggregate your data and extract useful insights.

## System Requirements

The following system requirements must be installed and configured for Lumify execution:
* More than 4G Ram

## Getting Started

Lumify supplies a pre-built virtual machine, however if you would like to build the project from source please see the corresponding section below.

[Pre-built VM Instructions] (https://github.com/nearinfinity/lumify-enterprise/tree/develop/docs/PREBUILT_VM.md)
[Build from source Instructions] (https://github.com/nearinfinity/lumify-enterprise/tree/develop/docs/BUILD_FROM_SOURCE.md)

## Disclaimers
* The following Titan property is required on every vertex:
    * title
    * _subType: concept id associated with concept
    * type: type of concept (i.e Look at VertexType.java)
* Accumulo must have a row structure with a SHA256 hash for the row key.

## License

Lumify is released under [Apache v2.0] (https://www.apache.org/licenses/LICENSE-2.0.html).