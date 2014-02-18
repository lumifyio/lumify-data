You may choose to install Lumify dependencies on your Linux host to allow you to run some parts of the application outside of a VM.


## CCExtractor

Download CCExtractor from [http://ccextractor.sourceforge.net/download-ccextractor.html](http://ccextractor.sourceforge.net/download-ccextractor.html)

    unzip ccextractor.src.0.66.zip
    cd ccextractor.0.66/linux
    ./build
    sudo mv ccextractor /usr/bin/ccextractor


## FFmpeg
### via aptitude

    sudo aptitude update
    sudo aptitude install ffmpeg

### from source

Follow the instruction at [https://trac.ffmpeg.org/wiki/CentosCompilationGuide](https://trac.ffmpeg.org/wiki/CentosCompilationGuide)

Then configure qt-faststart:

    cd ~/ffmpeg_sources/ffmpeg/tools/
    make qt-faststart
    sudo cp qt-faststart ~/bin/qt-faststart
    sudo ldconfig


## OpenCV

    cd /tmp
    curl -O http://www.us.apache.org/dist/ant/binaries/apache-ant-1.9.2-bin.tar.gz
    tar xzf apache-ant-1.9.2-bin.tar.gz

    cd /tmp
    curl -O http://superb-dca3.dl.sourceforge.net/project/opencvlibrary/opencv-unix/2.4.5/opencv-2.4.5.tar.gz
    tar xzf opencv-2.4.5.tar.gz

    cd /tmp/opencv-2.4.5
    sed -i 's/JNI_FOUND/1/g' modules/java/CMakeLists.txt

    mkdir /tmp/opencv-2.4.5/build
    cd /tmp/opencv-2.4.5/build
    ANT_DIR=/tmp/apache-ant-1.9.2 cmake -DBUILD_PERF_TESTS=OFF -DBUILD_TESTS=OFF ..
    make
    sudo make install

    sudo ln -s /usr/local/share/OpenCV/java/libopencv_java245.so /usr/local/lib/libopencv_java245.so
    sudo ldconfig


## Tesseract

    cd /tmp
    curl -O http://www.leptonica.com/source/leptonlib-1.67.tar.gz
    curl -O https://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.02.tar.gz
    curl -O https://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.eng.tar.gz

    cd /tmp
    tar xzf leptonlib-1.67.tar.gz
    cd /tmp/leptonlib-1.67
    LIBS=-lm ./configure
    make
    sudo make install

    cd /tmp
    tar xzvf tesseract-ocr-3.02.02.tar.gz
    cd /tmp/tesseract-ocr
    ./configure
    make
    sudo make install

    cd /tmp
    tar xzvf tesseract-ocr-3.02.eng.tar.gz
    sudo mv /tmp/tesseract-ocr/tessdata/eng.* /usr/local/share/tessdata/


If you encounter the following error at runtime:

> java.lang.UnsatisfiedLinkError: Unable to load library 'tesseract': libtesseract.so: cannot open shared object file

    cd /usr/lib/
    sudo ln -s /usr/local/lib/libtesseract.so


## NodeJS and modules

- http://nodejs.org/download
- https://npmjs.org/package/grunt-cli
- https://npmjs.org/package/bower
