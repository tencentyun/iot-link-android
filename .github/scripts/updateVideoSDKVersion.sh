#!/bin/bash

vtag=$VIDEO_TAG

# vtag 2.2.0-video.0
vtaglist=(${vtag//-/ })

# version 2.2.0
version=${vtaglist[0]}

# replace videoSdkVersion with version
sed -i 's#def videoSdkVersion.*#def videoSdkVersion = \"'$version'\"#g' config.gradle

# remove '-SNAPSHOT'
sed -i '60,$s#-SNAPSHOT##g' sdk/video-link-android/build.gradle