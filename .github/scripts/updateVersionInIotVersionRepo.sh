#!/bin/sh

rc=$(git rev-parse --short HEAD)

cd iot-version

git add .
git commit -m "repositories@$rc"
git push

cd ../