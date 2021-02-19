#!/bin/sh

rc=$(git rev-parse --short HEAD)

cd iot-version

git add .
git commit -m "repositories@$rc"
git push https://$IOT_GITHUB_ACCESS_TOKEN@github.com/tonychanchen/iot-version.git

cd ../