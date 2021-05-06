#!/bin/bash 

echo "开始资源解压，重新压缩！";
echo "执行文件路径：$0";
#echo "需解压的文件：$1";
#echo "待拷贝的文件：$2";
pwd
#lastPath=$(dirname "$PWD")
lastPath=$(pwd)
echo "$lastPath"
echo "$lastPath""/app/build/intermediates/processed_res/debug/out"

time=$(date "+%Y%m%d-%H%M%S")
echo "${time} - startUnZip"

rm -rf `pwd`"/.idea/litebuild/tempResFolder"
mkdir `pwd`"/.idea/litebuild/tempResFolder"
# -q 控制台不输出日志
unzip -o -q "$lastPath""/app/build/intermediates/processed_res/debug/out/resources-debug.ap_" -d .idea/litebuild/tempResFolder

echo `pwd`"/tempResFolder"

time=$(date "+%Y%m%d-%H%M%S")
echo "${time} - moveFolder"

cp -R "$lastPath""/app/build/intermediates/merged_assets/debug/out/." `pwd`"/.idea/litebuild/tempResFolder/assets"

echo "============= "`pwd`"/tempResFolder/*"

time=$(date "+%Y%m%d-%H%M%S")
echo "${time} - startZip"
cd `pwd`"/.idea/litebuild/tempResFolder"

#echo "============= "`pwd`

apkPath="$(dirname "$PWD")""/resources-debug.apk"
echo "============= ""$apkPath"
zip -r -o -q $apkPath *
time=$(date "+%Y%m%d-%H%M%S")
echo "${time} - end"

cd ..
pwd
rm -rf tempResFolder

adb shell rm -rf /sdcard/Android/data/com.immomo.momo.dev/files/MOMOCARD/patch_file
adb shell mkdir /sdcard/Android/data/com.immomo.momo.dev/files/MOMOCARD/patch_file
adb push resources-debug.apk /sdcard/Android/data/com.immomo.momo.dev/files/MOMOCARD/patch_file/