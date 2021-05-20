package com.immomo.litebuild.helper

import com.immomo.litebuild.Settings
import com.immomo.litebuild.util.Utils
import java.io.File
import java.io.FileNotFoundException

class ResourceHelper {
    var st : Long = 0

    fun checkResource() {
        println("ResourceHelper process, changed=${Settings.getData().hasResourceChanged}")
        if (!Settings.getData().hasResourceChanged) return

        st = System.currentTimeMillis()
        compileResources()
        val pkgtime = System.currentTimeMillis()
//        println("资源编译耗时：" + (System.currentTimeMillis() - st))
//        packageResources()
//        println("资源打包耗时：" + (System.currentTimeMillis() - pkgtime))
    }

    private fun compileResources() {
        val stableId = File(Settings.Data.TMP_PATH + "/stableIds.txt")
        if (stableId.exists()) {
            println("stableIds存在")
        } else {
            println("=================================")
            throw FileNotFoundException("stableIds不存在，请先完整编译一遍项目！")
        }

        Settings.getData().needProcessDebugResources = true;
//        Utils.executeScript("./gradlew processDebugResources --offline")
    }

    fun packageResources() {
        val ap_ = File("${Settings.project.rootDir.path}/app/build/intermediates/processed_res/debug/out/resources-debug.ap_")
        println("packageResources-packageResources rootPath=====${Settings.project.rootDir.path}")

        if (ap_.exists()) {
            println("ap_文件存在")
        } else {
            throw FileNotFoundException("ap_文件 不 存在")
        }

        var cmds = String()
        cmds += "source ~/.bash_profile"
//        cmds += "\n cd ../"
        cmds += "\n sh resourcesApk.sh"

        println("准备执行resourcesApk.sh")
        Utils.executeScript(cmds);

        println("资源编译耗时：" + (System.currentTimeMillis() - st))
    }

}