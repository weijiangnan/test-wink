package com.immomo.litebuild.helper

import com.immomo.litebuild.Settings
import com.immomo.litebuild.util.Utils
import java.io.File
import java.io.FileNotFoundException

class ResourceHelper {
    var st : Long = 0

    fun checkResource() {
        println("ResourceHelper process, changed=${Settings.data.hasResourceChanged}")
        if (!Settings.data.hasResourceChanged) return

        st = System.currentTimeMillis()
        compileResources()
    }

    private fun compileResources() {
        val stableId = File(Settings.env.tmpPath + "/stableIds.txt")
        if (stableId.exists()) {
            println("stableIds存在")
        } else {
            println("=================================")
            throw FileNotFoundException("stableIds不存在，请先完整编译一遍项目！")
        }

        Settings.data.needProcessDebugResources = true
    }

    fun packageResources() {
        val ap_ = File("${Settings.env.rootDir}/app/build/intermediates/processed_res/debug/out/resources-debug.ap_")
        println("packageResources-packageResources rootPath=====${Settings.env.rootDir}")

        if (ap_.exists()) {
            println("ap_文件存在")
        } else {
            throw FileNotFoundException("ap_文件 不 存在")
        }

        val lastPath = Settings.env.rootDir
        val litebuildFolderPath = Settings.env.tmpPath
        val patchName = Settings.env.version + "_resources-debug.apk"
        val apkPath = "$litebuildFolderPath/$patchName"
        val pushSdcardPath = "/sdcard/Android/data/${Settings.env.debugPackageName}/patch_file/apk"

        println("资源打包路径：$apkPath")
        val app = Settings.env.projectTreeRoot!!.name
        val localScript = """
            source ~/.bash_profile
            echo "开始资源解压，重新压缩！"
            echo $lastPath/$app/build/intermediates/processed_res/debug/out
            rm -rf $lastPath/.idea/litebuild/tempResFolder
            mkdir $lastPath/.idea/litebuild/tempResFolder
            unzip -o -q $lastPath/$app/build/intermediates/processed_res/debug/out/resources-debug.ap_ -d $lastPath/.idea/litebuild/tempResFolder
            cp -R $lastPath/$app/build/intermediates/merged_assets/debug/out/. $lastPath/.idea/litebuild/tempResFolder/assets
            cd $lastPath/.idea/litebuild/tempResFolder
            zip -r -o -q $apkPath *
            cd ..
            rm -rf tempResFolder
            adb shell rm -rf $pushSdcardPath
            adb shell mkdir $pushSdcardPath
            adb push $patchName $pushSdcardPath/
        """.trimIndent()

        Utils.executeScript(localScript)

        println("资源编译耗时：" + (System.currentTimeMillis() - st))
    }



}