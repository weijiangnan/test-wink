package com.immomo.litebuild.helper

import com.immomo.litebuild.Settings
import com.immomo.litebuild.util.Utils
import java.io.File
import java.io.FileNotFoundException

class ResourceHelper {

    fun process() {
        println("ResourceHelper process")
        if (!Settings.getData().hasResourceChanged) return
        compileResources()
        packageResources()
    }

    private fun compileResources() {
        val stableId = File(".idea/litebuild/stableIds.txt")
        if (stableId.exists()) {
            println("stableIds存在")
        } else {
            println("=================================")
            throw FileNotFoundException("stableIds不存在，请先完整编译一遍项目！")
        }
        Utils.executeScript("./gradlew processDebugResources --offline")
    }

    private fun packageResources() {
        val ap_ = File("./app/build/intermediates/processed_res/debug/out/resources-debug.ap_")
        if (ap_.exists()) {
            println("ap_文件存在")
        } else {
            throw FileNotFoundException("ap_文件 不 存在")
        }
        Utils.executeScript("sh ./resourcesApk.sh")
    }

}