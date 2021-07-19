#!/bin/bash

# ---------------- VIDEO BEGIN ----------------
video_tag=$LATEST_VIDEO_TAG

# 最新tag，例如：video-v2.2.0-beta
echo ">>>latest video tag: $video_tag"

# video-v2.2.0-beta --> v2.2.0
vtag_no_beta=(${video_tag//-/ })
vtag_no_beta=${vtag_no_beta[1]}

# v2.2.0 --> 2.2.0
vtag_no_beta=${vtag_no_beta#*v}
echo ">>>latest video tag(no v): $vtag_no_beta"


# snapshot版本自增（最新tag+1）
vtaglist=(${vtag_no_beta//./ })
firsttag=${vtaglist[0]}
secondtag=${vtaglist[1]}
thirdtag=${vtaglist[2]}
thirdtag=`expr $thirdtag + 1`

video_new_version=$firsttag.$secondtag.$thirdtag
echo ">>>video new version: $video_new_version"
# ---------------- VIDEO END ----------------


# ---------------- LINK BEGIN ----------------
link_tag=$LATEST_LINK_TAG

# 最新tag，例如：v1.0.0
echo ">>>latest link tag: $link_tag"

# 最新tag，例如：1.0.0
ltag=${link_tag#*v}
echo ">>>latest link tag(no v): $ltag"

ltag_no_beta=(${ltag//-/ })
ltag_no_beta=${ltag_no_beta[0]}

ltaglist=(${ltag_no_beta//./ })
firsttag=${ltaglist[0]}
secondtag=${ltaglist[1]}
thirdtag=${ltaglist[2]}
thirdtag=`expr $thirdtag + 1`

link_new_version=$firsttag.$secondtag.$thirdtag
echo ">>>link new version: $link_new_version"
# ---------------- LINK END ----------------

if [ -z "$video_new_version" ]; then
    video_new_version=$link_new_version
fi

sed -i 's#def sdkVersion.*#def sdkVersion = \"'$link_new_version'\"#g' config.gradle
sed -i 's#def videoSdkVersion.*#def videoSdkVersion = \"'$video_new_version'\"#g' config.gradle

echo ">>>over!!!"



