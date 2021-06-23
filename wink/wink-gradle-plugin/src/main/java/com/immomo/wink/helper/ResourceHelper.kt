package com.immomo.wink.helper

import com.immomo.wink.Constant
import com.immomo.wink.Settings
import com.immomo.wink.util.Utils
import com.immomo.wink.util.WinkLog
import java.io.File
import java.io.FileNotFoundException

class ResourceHelper {
    var st : Long = 0

    fun checkResource() {
        WinkLog.d(" \n ResourceHelper process, changed=${Settings.data.hasResourceChanged}")
        if (!Settings.data.hasResourceChanged) return

        WinkLog.i("Process resources...")

        st = System.currentTimeMillis()
        compileResources()
    }

    private fun compileResources() {
        val stableId = File(Settings.env.tmpPath + "/stableIds.txt")
        if (stableId.exists()) {
            WinkLog.d("stableIds存在")
        } else {
            WinkLog.d("=================================")
            throw FileNotFoundException("stableIds不存在，请先完整编译一遍项目！")
        }

        Settings.data.needProcessDebugResources = true
    }

    var ap_path = ""
    fun findAp_(file: File) {
        if (file.isFile) {
            WinkLog.d("findAp_是文件 file=${file.name}")
            if (file.name.endsWith(".ap_")) {
                WinkLog.d("是ap_ file=${file.name}")
                ap_path = file.absolutePath
            }
        }
        if (file.isDirectory) {
            for (f : File in file.listFiles()) {
                WinkLog.d("递归找ap_ f=${f.name}")
                findAp_(f)
            }
        }
    }

    fun packageResources() {
        val ap_ParentDir = File("${Settings.env.appProjectDir}/build/intermediates/processed_res")
        findAp_(ap_ParentDir)

        WinkLog.d("packageResources-packageResources rootPath=====${ap_ParentDir.absolutePath}")
        WinkLog.d("找到的 findap==${ap_path}")

        if (!ap_path.isBlank()) {
            WinkLog.d("ap_文件存在")
        } else {
            throw FileNotFoundException("ap_文件 不 存在")
        }

        val lastPath = Settings.env.rootDir
        val winkFolderPath = Settings.env.tmpPath
        val patchName = Settings.env.version + "_resources-debug.apk"
        val apkPath = "$winkFolderPath/$patchName"
        val pushSdcardPath = "/sdcard/Android/data/${Settings.env.debugPackageName}/patch_file/apk"

        WinkLog.d("资源打包路径：$apkPath")
        val app = Settings.env.projectTreeRoot!!.name
        val localScript = """
            source ~/.bash_profile
            echo "开始资源解压，重新压缩！"
            rm -rf $lastPath/.idea/${Constant.TAG}/tempResFolder
            mkdir $lastPath/.idea/${Constant.TAG}/tempResFolder
            unzip -o -q ${ap_path} -d $lastPath/.idea/${Constant.TAG}/tempResFolder
            cp -R $lastPath/$app/build/intermediates/merged_assets/${Settings.env.variantName}/out/. $lastPath/.idea/${Constant.TAG}/tempResFolder/assets
            cd $lastPath/.idea/${Constant.TAG}/tempResFolder
            zip -r -o -q $apkPath *
            cd ..
            rm -rf tempResFolder
        """.trimIndent()

        Utils.executeScript(localScript)

        WinkLog.d("资源编译耗时：" + (System.currentTimeMillis() - st))
    }



}