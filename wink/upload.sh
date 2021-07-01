#!/bin/bash
is_upload_maven=$1
version=$2
echo "need upload maven: $1"
echo "wink version: $2"
if [ $is_upload_maven == true ]; then
    echo "upload maven center!!!"
    sed -i '' 's/gradle.ext.uploadMavenCenter=false/gradle.ext.uploadMavenCenter=true/g' UploadProperties.gradle
else
    echo "upload to inner maven!"
    sed -i '' 's/gradle.ext.uploadMavenCenter=true/gradle.ext.uploadMavenCenter=false/g' UploadProperties.gradle
fi
#sed -i '' "s/gradle.ext.winkVersion=*$/gradle.ext.winkVersion='"$version"'/g" UploadProperties.gradle

#sed -i '4s/BROADCAST_PORT=.*$/BROADCAST_PORT=9999/g' file

./gradlew wink-patch-lib:uploadArchives
./gradlew -p buildSrc :wink-gradle-plugin:uploadArchives



# package to CDN
cp ./wink-gradle-plugin/build/libs/wink-gradle-plugin.jar ./externalLib
cd externalLib
zip -r -o -q wink_package_${version}.zip *
sh cdn_upload.sh wink_package_${version}.zip
cd ..