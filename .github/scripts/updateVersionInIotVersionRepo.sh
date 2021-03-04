#!/bin/sh

rc=$(git rev-parse --short HEAD)

cd iot-version

echo "start commit..."

git config --global user.email "archurspace@gmail.com"
git config --global user.name "archurtan"

git add .
git commit -m "repositories@$rc"
git push

cd ../