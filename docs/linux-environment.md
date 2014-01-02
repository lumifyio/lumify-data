# Lumify Dependency Installation
## Linux
---

These instructions will help you install and configure the Lumify tools and dependencies in a Linux environment.  These instructions target the CentOS (RHEL) distribution.

The following tools are included in this guide:

*	[ffmpeg](#ffmpeg)
*	[OpenCV](#opencv)
*	[tesseract](#tesseract)
*	[NodeJS](#nodejs)

## [FFmpeg](id:ffmpeg)
---

To get started, go to [http://ffmpeg.org/trac/ffmpeg/wiki/CentosCompilationGuide](http://ffmpeg.org/trac/ffmpeg/wiki/CentosCompilationGuide) and run the commands for the following sections:

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
cd ~/ffmpeg_sources
git clone --depth 1 git://source.ffmpeg.org/ffmpeg
cd ffmpeg
PKG_CONFIG_PATH="$HOME/ffmpeg_build/lib/pkgconfig"
./configure --prefix="$HOME/ffmpeg_build" --extra-cflags="-I$HOME/ffmpeg_build/include" --extra-ldflags="-L$HOME/ffmpeg_build/lib" --bindir="$HOME/bin" --extra-libs="-ldl" --enable-gpl --enable-nonfree --enable-libfdk_aac --enable-libmp3lame --enable-libvorbis --enable-libvpx --enable-libx264 --enable-version3
make
sudo make install
make distclean
hash -r
```

To configure qt-faststart, run the following commands next:

```
cd ~/ffmpeg_sources/ffmpeg/tools/
make qt-faststart
sudo cp qt-faststart ~/bin/qt-faststart
sudo ldconfig
```

Download [ccextractor source](http://ccextractor.sourceforge.net/download-ccextractor.html)

```
unzip ccextractor.src.0.66.zip
cd ccextractor.0.66/linux
./build
sudo mv ccextractor ~/bin/ccextractor
```

## [OpenCV](id:opencv)
---

**NOTE: Lumify targets Java 1.6+ so this library must be compiled using JDK 1.6.**

```
cd /tmp

curl -o apache-ant-1.9.2-bin.tar.gz "http://www.us.apache.org/dist/ant/binaries/apache-ant-1.9.2-bin.tar.gz"
tar xzf apache-ant-1.9.2-bin.tar.gz

curl -o opencv-2.4.5.tar.gz "http://superb-dca3.dl.sourceforge.net/project/opencvlibrary/opencv-unix/2.4.5/opencv-2.4.5.tar.gz"
tar xzf opencv-2.4.5.tar.gz

cd opencv-2.4.5
sed -i 's/JNI_FOUND/1/g' modules/java/CMakeLists.txt

mkdir build
cd build
ANT_DIR=/tmp/apache-ant-1.9.2 cmake -DBUILD_PERF_TESTS=OFF -DBUILD_TESTS=OFF ..
make
sudo make install

sudo ln -s /usr/local/share/OpenCV/java/libopencv_java245.so /usr/local/lib/libopencv_java245.so
sudo ldconfig
```

## [Tesseract](id:tesseract)
---

Download [Leptonica](http://www.leptonica.com/source/leptonlib-1.67.tar.gz)

```
tar xzvf leptonlib-1.67.tar.gz
cd leptonlib-1.67
LIBS=-lm ./configure
make
sudo make install
```

Download [Tesseract](https://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.02.tar.gz)

```
tar xzvf tesseract-ocr-3.02.02.tar.gz
cd tesseract-ocr
./configure
make
sudo make install
```

Download [English Language Pack for Tesseract](https://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.eng.tar.gz)

```
tar xzvf tesseract-ocr-3.02.eng.tar.gz
sudo mv tesseract-ocr/tessdata/eng.* /usr/local/share/tessdata/
```

If you get an error:

```
java.lang.UnsatisfiedLinkError: Unable to load library 'tesseract': libtesseract.so: cannot open shared object file
```
then navigate to /usr/lib/ and create a symbolic link to the main tesseract shared object libraries.

```
ln -s /usr/local/lib/libtesseract.so
```

## [NodeJS](id:nodejs)
---

Follow the instructions to install:

*	[NodeJS 0.10.21](http://blog.nodejs.org/2013/10/18/node-v0-10-21-stable/).
* 	[Bower 1.2.7](https://npmjs.org/package/bower)
* 	[Grunt-cli 0.1.11](https://npmjs.org/package/grunt-cli)
