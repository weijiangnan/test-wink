package com.immomo.litebuild.hilt

import com.immomo.litebuild.Settings
import java.io.File

object HiltTransform{

    private val srcPath = File(Settings.Data.TMP_PATH + "/tmp_class")

    private val transformer by lazy {
        val transformPath = File("/Users/weixin/Documents/litebuild/sunflower/app/build/intermediates/transforms/AndroidEntryPointTransform/debug/")
        val inputFiles: List<File> = transformPath.listFiles().toList()
        AndroidEntryPointClassTransformer("liteBuild", inputFiles, srcPath, true)
    }

    fun transform() {
        srcPath.walk().filter { it.extension == "class" }.forEach {
            transformer.transformFile(it)
        }
    }
}