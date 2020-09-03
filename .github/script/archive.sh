#!/bin/sh

#bin/bsah - l

rb=$(echo "${{ github.ref }}" | sed -e 's,.*/\(.*\),\1,')
rc=$(git rev-parse --short HEAD)

if [ $1 == 'Debug' ]; then
  sed -i 's#def versionAppName.*#def versionAppName = \"'$rb'+git.'$rc'\"#g' config.gradle
else
  sed -i 's#def versionAppName.*#def versionAppName = \"'$rb'\"#g' config.gradle
fi
