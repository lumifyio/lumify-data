The development environment is configured and controlled by Vagrant. The Vagrantfile in the root of the project will install everything you need to get started and keep you in sync with other developers.

## Vagrant Setup

From the root of your project directory:

1. install [VitualBox (v. 4.2.12)](https://www.virtualbox.org/wiki/Download_Old_Builds_4_2)
1. install the [VirtualBox Extension Pack](https://www.virtualbox.org/wiki/Download_Old_Builds_4_2)
1. install [Vagrant](http://docs.vagrantup.com/v2/installation/)
1. `$ vagrant up`
1. `$ vagrant ssh`
You'll be in the vagrant user's home directory upon ssh'ing into the Vagrant-controlled VM. The files in the directory where you ran `vagrant up` will be available in `/vagrant` on the guest VM. Project-related packages are installed in `/opt`.
 * you'll be logged in as the `vagrant` user who has `sudo` privileges
 * your `lumify` directory is shared as the `/vagrant` directory inside the VM
 * we're installing our stuff in `/opt`

`vagrant -h` will give you a list of all available commands.

`vagrant halt` to gracefully shutdown the VM

## ffmpeg Installation on Mac OSX

In order to run the video processing jobs locally on your Mac you'll need to install ffmpeg, qt-faststart, and ccextractor. The easiest way to do that is using [Homebrew](http://mxcl.github.io/homebrew/). After installing Homebrew, you can install `ffmpeg` and `qtfaststart` with the following commands.

```
$ brew install ffmpeg --with-libvorbis --with-libvpx --with-fdk-aac --with-opus --with-theora
$ brew install qtfaststart
$ brew install ccextractor
```

## ffmpeg Installation on Linux

Download ccextractor source (http://ccextractor.sourceforge.net/download-ccextractor.html)

```
$ sudo apt-get install ffmpeg
$ unzip ccextractor.src.0.66.zip
$ cd ccextractor.0.66/linux
$ ./build
$ sudo mv ccextractor /usr/bin/ccextractor
```

## ffmpeg Installation on CentOS (RHEL)

To get started, go to http://ffmpeg.org/trac/ffmpeg/wiki/CentosCompilationGuide and run the commands for the following sections:

* Get the Dependencies
* Yasm
* x264
* libfdk_aac
* libmp3lame
* libogg
* libvorbis
* libvpx

Install FFmpeg:

```
$ cd ~/ffmpeg_sources
$ git clone --depth 1 git://source.ffmpeg.org/ffmpeg
$ cd ffmpeg
$ PKG_CONFIG_PATH="$HOME/ffmpeg_build/lib/pkgconfig"
$ ./configure --prefix="$HOME/ffmpeg_build" --extra-cflags="-I$HOME/ffmpeg_build/include" --extra-ldflags="-L$HOME/ffmpeg_build/lib" --bindir="$HOME/bin" --extra-libs="-ldl" --enable-gpl --enable-nonfree --enable-libfdk_aac --enable-libmp3lame --enable-libvorbis --enable-libvpx --enable-libx264 --enable-version3
$ make
$ make install
$ make distclean
$ hash -r
```

To configure qt-faststart, run the following commands next:

```
$ cd ~/ffmpeg_sources/ffmpeg/tools/
$ make qt-faststart
$ sudo cp qt-faststart ~/bin/qt-faststart
$ sudo ldconfig
```

Download ccextractor source (http://ccextractor.sourceforge.net/download-ccextractor.html)

```
$ unzip ccextractor.src.0.66.zip
$ cd ccextractor.0.66/linux
$ ./build
$ sudo mv ccextractor ~/bin/ccextractor
```

## OpenCV Installation on Mac OSX

In order to run image recognition jobs locally on your Mac you'll need to install opencv from the homebrew/science tap. The easiest way to do that is using [Homebrew](http://mxcl.github.io/homebrew/). After installing Homebrew, you can install `opencv` with the following commands.

```
$ brew tap homebrew/science
$ brew install opencv --env=std
```

## OpenCV Installation on Linux

```
$ cd /tmp

$ curl -o apache-ant-1.9.2-bin.tar.gz "http://www.us.apache.org/dist/ant/binaries/apache-ant-1.9.2-bin.tar.gz"
$ tar xzf apache-ant-1.9.2-bin.tar.gz

$ curl -o opencv-2.4.5.tar.gz "http://superb-dca3.dl.sourceforge.net/project/opencvlibrary/opencv-unix/2.4.5/opencv-2.4.5.tar.gz"
$ tar xzf opencv-2.4.5.tar.gz

$ cd opencv-2.4.5
$ sed -i 's/JNI_FOUND/1/g' modules/java/CMakeLists.txt

$ mkdir build
$ cd build
$ ANT_DIR=/tmp/apache-ant-1.9.2 cmake -DBUILD_PERF_TESTS=OFF -DBUILD_TESTS=OFF ..
$ make
$ sudo make install

$ sudo ln -s /usr/local/share/OpenCV/java/libopencv_java245.so /usr/local/lib/libopencv_java245.so
$ sudo ldconfig
```

## tesseract Installation on CentOS

Download Leptonica: http://www.leptonica.com/source/leptonlib-1.67.tar.gz

```
$ tar xzvf leptonlib-1.67.tar.gz
$ cd leptonlib-1.67
$ LIBS=-lm ./configure
$ make
$ sudo make install
```

Download Tesseract: https://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.02.tar.gz

```
$ tar xzvf tesseract-ocr-3.02.02.tar.gz
$ cd tesseract-ocr
$ ./configure
$ make
$ sudo make install
```

Download English Language Pack for Tesseract: https://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.eng.tar.gz

```
$ tar xzvf tesseract-ocr-3.02.eng.tar.gz
$ sudo mv tesseract-ocr/tessdata/eng.* /usr/local/share/tessdata/
```

If you get an error:

```
$ java.lang.UnsatisfiedLinkError: Unable to load library 'tesseract': libtesseract.so: cannot open shared object file
```
then navigate to /usr/lib/ and create a symbolic link to the main tesseract shared object libraries.

```
$ ln -s /usr/local/lib/libtesseract.so
```

## tesseract Installation on Mac OSX

If you don't already have it, install HomeBrew:

```
$ ruby -e "$(curl -fsSL https://raw.github.com/mxcl/homebrew/go)"
```

Using HomeBrew, install tesseract, with all language packs:

```
$ brew install tesseract --all-languages
```

## Other necessary dependency installations
* [NodeJS 0.10.21](http://blog.nodejs.org/2013/10/18/node-v0-10-21-stable/)
* [Bower 1.2.7](https://npmjs.org/package/bower)
* [Grunt-cli 0.1.11](https://npmjs.org/package/grunt-cli)


## Sample Data

* Close caption video: (if you want the close captioning to appear the transcript and the video must be tarred together
 * http://ncam.wgbh.org/invent_build/web_multimedia/mobile-devices/sample-clips
* Sample Data Set:
 * https://github.com/altamiracorp/lumify-enterprise/blob/develop/docs/sample-data-sets.md
