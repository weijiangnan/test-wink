package com.immomo.litebuild.helper

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext
import com.immomo.litebuild.util.Log
import com.immomo.litebuild.util.Utils
import org.gradle.api.Project
import java.io.File

class Snapshot(private val project: Project) {


    private val extensionList = listOf("java", "kt", "xml", "json", "png", "jpeg", "webp")

    private var diffDir: String
    private var csvPathCode: String
    private var csvPathRes: String
    private val scanPathCode: String
    private var scanPathRes: String

    private var csvReader: CsvReader
    private var csvWriter: CsvWriter

    init {
        val moduleName = project.path.replace(":", "")
        diffDir = "${project.rootDir}/.idea/litebuild/diff/${moduleName}"

        scanPathCode = "${project.projectDir}/src/main/java"
        scanPathRes = "${project.projectDir}/src/main/res"

        csvPathCode = "${diffDir}/md5_code.csv"
        csvPathRes = "${diffDir}/md5_res.csv"


        val ctxCsvWriter = CsvWriterContext()
        val ctxCsvReader = CsvReaderContext()
        csvWriter = CsvWriter(ctxCsvWriter)
        csvReader = CsvReader(ctxCsvReader)
    }


    fun initSnapshot() {
        genSnapshotAndSaveToDisk(scanPathCode, csvPathCode)
        genSnapshotAndSaveToDisk(scanPathRes, csvPathRes)
    }


    private fun genSnapshotAndSaveToDisk(path: String, csvPath: String) {
//        Log.v(TAG, "[${project.path}]:遍历目录[$path]下的java,kt文件,生成md5并保存到csv文件[$csvPath]")

        val csvFile = File(csvPath)
        if (!csvFile.exists()) {
            csvFile.parentFile.mkdirs()
            csvFile.createNewFile()
        }

        val timeBegin = System.currentTimeMillis()
        File(path).walk()
                .filter {
                    it.isFile
                }
                .filter {
                    it.extension in extensionList
                }
                .forEach {
                    val row = listOf(it.absolutePath, getSnapshot(it))
                    csvWriter.open(csvPath, append = true) {
                        writeRow(row)
                    }
                }

        Log.v(DiffHelper.TAG, "[${project.path}]:耗时:${System.currentTimeMillis() - timeBegin}ms")

    }

    private fun getSnapshot(it: File) = Utils.getFileMD5s(it, 64)

}