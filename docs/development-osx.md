You may choose to install Lumify dependencies on your Mac to allow you to run some parts of the application outside of a VM.


## JDK

- 1.6 from Apple - [http://support.apple.com/kb/DL1572](http://support.apple.com/kb/DL1572)
- 1.7 from Oracle - [http://www.oracle.com/technetwork/java/javase/downloads](http://www.oracle.com/technetwork/java/javase/downloads)


## HomeBrew

Other dependencies will be installed via the [HomeBrew](http://brew.sh) package manager.

    ruby -e "$(curl -fsSL https://raw.github.com/Homebrew/homebrew/go/install)"


## FFmpeg

    brew install ffmpeg --with-libvorbis --with-libvpx --with-fdk-aac --with-opus --with-theora
    brew install qtfaststart
    brew install ccextractor


## OpenCV

    brew tap homebrew/science
    JAVA_HOME=$(/usr/libexec/java_home -v 1.6) brew install opencv


## Tesseract

    brew install tesseract --all-languages


## NodeJS and modules

    brew install node
    brew install npm

    npm install grunt-cli
    npm install bower
