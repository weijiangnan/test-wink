/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.immomo.litebuild.helper

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvReaderContext
import com.github.doyaaaaaken.kotlincsv.dsl.context.CsvWriterContext
import com.immomo.litebuild.Settings
import com.immomo.litebuild.util.Log
import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.Project
import java.io.File

class DiffHelper(private val projectInfo: Settings.Data.ProjectInfo) {
    companion object {
        const val TAG = "litebuild.diff"
    }

    private var scanPathRes: String
    private var csvPathRes: String
    private val scanPathCode: String
    private val extensionList = listOf("java", "kt", "xml", "json", "png", "jpeg", "webp")
    private var csvPathCode: String
    private var diffDir: String
    private var csvReader: CsvReader
    private var csvWriter: CsvWriter
    private var project: Project = projectInfo.project

    init {
        Log.v(TAG, "[${project.path}]:init")

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

        project.gradle.addBuildListener(object : BuildAdapter() {
            override fun buildFinished(result: BuildResult) {
                Log.v(TAG, "构建结束:[${if (result.failure == null) "成功" else "失败"}]")

                if (result.failure == null) {
                    File(csvPathCode).takeIf { it.exists() }?.delete()

                    genSnapshotAndSaveToDisk(scanPathCode, csvPathCode)
                    genSnapshotAndSaveToDisk(scanPathRes, csvPathRes)
                }
            }
        })
    }

    fun diff() {
        Log.v(TAG, "[${project.path}]:获取差异...")

        diffInner(scanPathCode, csvPathCode) {
            Log.v(TAG, "[${project.path}]:    差异数据:$it")
            when {
                it.endsWith(".java") -> projectInfo.changedJavaFiles.add(it)
                it.endsWith(".kt") -> projectInfo.changedKotlinFiles.add(it)
            }
        }

        diffInner(scanPathRes, csvPathRes) {
            Log.v(TAG, "[${project.path}]:差异数据:$it")
            projectInfo.hasResourceChanged = true
        }
    }

    private fun diffInner(scanPath: String, csvPath: String, block: (String) -> Unit) {
        val mapOrigin = loadSnapshotToCacheFromDisk(csvPath)

        //Log.v(TAG, "原始数据:")
        //Log.v(TAG, mapOrigin)

        if (mapOrigin.isEmpty()) {
            Log.v(TAG, "[${project.path}]:原始数据为空")
            return
        } else {
            val mapNew = hashMapOf<String, String>()
            genSnapshotAndSaveToCache(scanPath, mapNew)

            //Log.v(TAG, "新数据:")
            //Log.v(TAG, mapNew)

            Log.v(TAG, "[${project.path}]:计算差异数据...")
            compareMap(mapOrigin, mapNew)
                    .also { if (it.isEmpty()) Log.v(TAG, "[${project.path}]:差异数据为空") }
                    .forEach {
                        block(it)
                    }

        }
    }

    private fun compareMap(map1: Map<String, String>, map2: Map<String, String>): Set<String> {
        Log.v(TAG, "[${project.path}]:compareMap...")

        val rst = hashSetOf<String>()
        var m1 = map1
        var m2 = map2
        if (map1.size < map2.size) {
            m1 = map2
            m2 = map1
        }

        m1.forEach { (k, v) ->
            if (m2.containsKey(k)) {
                //如果包含,则比较value
                if (m2[k] != v) {
                    rst.add(k)
                }
            } else {
                //如果不包含,则直接加入
                rst.add(k)
            }
        }

        return rst

    }


    private fun genSnapshotAndSaveToCache(path: String, map: HashMap<String, String>) {
        Log.v(TAG, "[${project.path}]:遍历目录[$path]下的java,kt文件,并生成md5并保存到map...")
        val timeBegin = System.currentTimeMillis()
        File(path).walk()
                .filter {
                    it.isFile
                }
                .filter {
                    it.extension in extensionList
                }
                .forEach {
                    map[it.absolutePath] = getSnapshot(it)
                }

        Log.v(TAG, "[${project.path}]:耗时:${System.currentTimeMillis() - timeBegin}ms")
    }

    private fun genSnapshotAndSaveToDisk(path: String, csvPath: String) {
        Log.v(TAG, "[${project.path}]:遍历目录[$path]下的java,kt文件,生成md5并保存到csv文件[$csvPath]")

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

        Log.v(TAG, "[${project.path}]:耗时:${System.currentTimeMillis() - timeBegin}ms")

    }

    private fun loadSnapshotToCacheFromDisk(path: String): Map<String, String> {
        Log.v(TAG, "[${project.path}]:从[${path}]加载md5信息...")

        return if (!File(path).exists()) {
            Log.v(TAG, "[${project.path}]:文件[${path}]不存在")
            hashMapOf()
        } else {
            val map = hashMapOf<String, String>()
            csvReader.open(path) {
                readAllAsSequence().forEach {
                    if (it.size < 2) return@forEach
                    map[it[0]] = it[1]
                }
            }
            map
        }
    }

    private fun getSnapshot(it: File) = it.lastModified().toString()//Utils.getFileMD5s(it, 64)


}