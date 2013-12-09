# Lumify Dependency Installation
## OS X
---

These instructions will help you install and configure the Lumify tools and dependencies on Macintosh OS X. They have been tested on OS X Mavericks (10.9).

The following tools are included in this guide:

*	[Java](#java)
*	[Homebrew](#homebrew)
*	[ffmpeg](#ffmpeg)
*	[OpenCV](#opencv)
*	[tesseract](#tesseract)
*	[NodeJS](#nodejs)

## [Java](id:java)
---

You must have JDK version 1.6+ installed to compile and run Lumify. It is recommended to install both Oracle's latest Java 7 build and Apple's Java 6 as the older Java 6 is required for building some of Lumify's dependencies.

### Apple JDK (1.6)

Download and run the installation package from [http://support.apple.com/kb/DL1572](http://support.apple.com/kb/DL1572).

### Oracle JDK (1.7)

Download and run the installation package from [http://www.oracle.com/technetwork/java/javase/downloads](http://www.oracle.com/technetwork/java/javase/downloads).

## [Homebrew](id:homebrew)
---

[Homebrew](http://brew.sh) is a package management system for OS X.  The instructions below use Homebrew to install the Lumify dependencies. Before running Homebrew for the first time, please ensure [XCode](https://itunes.apple.com/us/app/xcode/id497799835?mt=12) and the XCode command line tools have been installed.

Run the following commands from Terminal to install Homebrew if you do not already have it.

```
ruby -e "$(curl -fsSL https://raw.github.com/mxcl/homebrew/go/install)"
brew update
brew doctor
```

## [FFmpeg](id:ffmpeg)
---

Lumify's video processing jobs require [ffmpeg](http://ffmpeg.org), qt-faststart and ccextractor.

Run the following commands from Terminal to install these libraries.

```
brew install ffmpeg --with-libvorbis --with-libvpx --with-fdk-aac --with-opus --with-theora
brew install qtfaststart
brew install ccextractor
```

## [OpenCV](id:opencv)
---

Lumify uses the [OpenCV](http://opencv.org) library to run image recognition jobs.

**NOTE: Lumify targets Java 1.6+ so this library must be compiled using JDK 1.6.**

```
brew tap homebrew/science
export JAVA_HOME=`/usr/libexec/java_home -v 1.6`
brew install opencv
export JAVA_HOME=`/usr/libexec/java_home -v 1.7`
ln -sf /usr/local/share/OpenCV/java/libopencv_java246.dylib /usr/local/lib
```

## [Tesseract](id:tesseract)
---

Install Tesseract with all language packs.

```
brew install tesseract --all-languages
```

## [NodeJS](id:nodejs)
---

Lumify requires NodeJS and the Node modules `grunt-cli` and `bower`.

```
brew install node
npm install grunt-cli
npm install bower
```
