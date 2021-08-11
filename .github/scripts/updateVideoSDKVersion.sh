#!/bin/bash

vtag=$VIDEO_TAG

# vtag video-v2.2.0
vtaglist=(${vtag//-/ })

# version v2.2.0
version=${vtaglist[1]}
version=${version#*v}

# replace videoSdkVersion with version
sed -i 's#def videoSdkVersion.*#def videoSdkVersion = \"'$version'\"#g' config.gradle
sed -i 's#def versionSDKDemoName.*#def versionSDKDemoName = \"'$version'\"#g' config.gradle

# remove '-SNAPSHOT'
sed -i '60,$s#-SNAPSHOT##g' sdk/video-link-android/build.gradle