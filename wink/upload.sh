#!/bin/bash
is_upload_maven=$1
version=$2
echo "need upload maven: $1"
echo "wink version: $2"
if [ $is_upload_maven == true ]; then
    echo "need upload maven!"
#    sed -e 'gradle/SharedProperties.gradle'
    sed -i '' 's/gradle.ext.uploadMavenCenter=false/gradle.ext.uploadMavenCenter=true/g' gradle/SharedProperties.gradle
else
    sed -i '' 's/gradle.ext.uploadMavenCenter=true/gradle.ext.uploadMavenCenter=false/g' gradle/SharedProperties.gradle
fi
#sed -i '' "2c/gradle\.ext\.winkVersion\='"$version"'/g" 'gradle/SharedProperties.gradle'
#sed -i '' '2c\/gradle.ext.winkVersion=false' gradle/SharedProperties.gradle

./gradlew wink-patch-lib:uploadArchives
./gradlew -p buildSrc :wink-gradle-plugin:uploadArchives

