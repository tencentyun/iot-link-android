#!/bin/sh

#当前时间
curTime=$(date +%s)
echo "当前时间---$curTime"

#基准差值
baseTime=$(date -d "2020-10-29 00:00:00" +%s)
timeStamp=$((curTime-baseTime))
echo "基准差值---$timeStamp"

#版本号
buildTime=$((timeStamp/(86400*7)+4))
echo "版本号---$buildTime"

sed -i 's#versionCode: 3#versionCode: '$buildTime'#g' config.gradle