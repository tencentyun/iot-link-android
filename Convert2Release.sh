#!/bin/sh

# 将腾讯连连开源代码转成公版代码
# 仅供本地调试使用，切勿外传

rm -rf app-config.json app/google-services.json
gpg -d --passphrase "TtnZ7yv@54TW3@9.mZqg6_9pwhuXY" --batch --quiet .github/tencent_official_keystore.jks.asc > tencent_official_keystore.jks
gpg -d --passphrase "TtnZ7yv@54TW3@9.mZqg6_9pwhuXY" --batch --quiet .github/app-config.json.asc > app-config.json
gpg -d --passphrase "TtnZ7yv@54TW3@9.mZqg6_9pwhuXY" --batch --quiet .github/app/google-services.json.asc > app/google-services.json

begin=$(grep -n "opensource {" app/build.gradle | cut -f1 -d:)
((end=$begin + 4))

sed -i "" 's#opensource_keystore#tencent_official_keystore#g' app/build.gradle
sed -i "" "$begin,$end d" app/build.gradle
sed -i "" 's#//-##g' app/build.gradle
sed -i "" "s#'111222'#'xDypzhVkt91kkupDXSdS9Ec'#g" app/build.gradle
sed -i "" 's#"1.1.0"#"1.0.0"#g' config.gradle

echo "Over!!!"