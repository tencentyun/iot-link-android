#!/bin/sh

git clone https://$IOT_GITHUB_ACCESS_TOKEN@github.com/tonychanchen/iot-version.git
cd iot-version

### modify version
old_version=$(cat iot-version-android.txt)
new_version=$((old_version+1))
echo "版本号---$new_version"

### update version in link repo
sed -i 's#versionCode: 3#versionCode: '$new_version'#g' ../config.gradle

### update version in iot-version repo
echo $new_version > iot-version-android.txt

cd ../