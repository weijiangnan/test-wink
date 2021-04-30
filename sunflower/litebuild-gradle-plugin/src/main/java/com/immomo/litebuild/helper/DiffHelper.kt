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
import com.immomo.litebuild.util.Log
import com.immomo.litebuild.util.Utils
import org.gradle.BuildAdapter
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import java.io.File

class DiffHelper(val project: Project) {
    companion object {
        const val TAG = "litebuild.diff"
    }

    private var csvPath: String
    private var diffDir: String
    private var csvReader: CsvReader
    private var csvWriter: CsvWriter

    init {
        Log.v(TAG, "init")

        diffDir = "${project.rootDir}/${com.immomo.litebuild.Settings.Data.TMP_PATH}"
        csvPath = "${diffDir}/md5.csv"

        val ctxCsvWriter = CsvWriterContext()
        val ctxCsvReader = CsvReaderContext()
        csvWriter = CsvWriter(ctxCsvWriter)
        csvReader = CsvReader(ctxCsvReader)


        Log.v(TAG, "rootDir=${project.rootDir}")
        project.allprojects.forEach {
            Log.v(TAG, "rootDir=${it.rootDir}")
            Log.v(TAG, "projectDir=${it.projectDir}")
        }

        project.gradle.addBuildListener(object : BuildAdapter() {
            override fun buildFinished(result: BuildResult) {
                Log.v(TAG, "构建结束:[${if (result.failure == null) "成功" else "失败"}]")

                if (result.failure == null) {
                    File(csvPath).takeIf { it.exists() }?.delete()

                    project.allprojects.forEach {
                        genMd5AndSaveToCsv("${it.projectDir}/src/main/java/", csvPath)
                    }
                }
            }
        })
    }

    fun diff() {
        Log.v(TAG, "获取差异...")

        val mapOrigin = loadMd5MapFromCSV(csvPath)

        //Log.v(TAG, "原始数据:")
        //Log.v(TAG, mapOrigin)

        if (mapOrigin.isEmpty()) {
            Log.v(TAG, "原始数据为空")
            return
        } else {
            val mapNew = hashMapOf<String, String>()
            project.allprojects.forEach {
                genMd5AndSaveToMap("${it.projectDir}/src/main/java/", mapNew)
            }

            //Log.v(TAG, "新数据:")
            //Log.v(TAG, mapNew)

            Log.v(TAG, "计算差异数据...")
            compareMap(mapOrigin, mapNew).forEach {
                Log.v(TAG, "差异数据:$it")
//                com.immomo.litebuild.Settings.getData().changedJavaFiles.add("app/src/main/java/com/google/samples/apps/sunflower/test/TestJava.java")
                com.immomo.litebuild.Settings.getData().changedJavaFiles.add(it)
            }

        }
    }

    private fun compareMap(map1: Map<String, String>, map2: Map<String, String>): Set<String> {
        Log.v(TAG, "compareMap...")

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


    private fun genMd5AndSaveToMap(path: String, map: HashMap<String, String>) {
        Log.v(TAG, "遍历目录[$path]下的java,kt文件,并生成md5并保存到map...")
        val timeBegin = System.currentTimeMillis()
        File(path).walk()
                .filter {
                    it.isFile
                }
                .filter {
                    it.extension in listOf<String>("java", "kt")
                }
                .forEach {
                    map[it.absolutePath] = Utils.getFileMD5s(it, 64)
                }

        Log.v(TAG, "耗时:${System.currentTimeMillis() - timeBegin}ms")
    }

    private fun genMd5AndSaveToCsv(path: String, csvPath: String) {
        Log.v(TAG, "遍历目录[$path]下的java,kt文件,生成md5并保存到csv文件[$csvPath]")

        val timeBegin = System.currentTimeMillis()
        File(path).walk()
                .filter {
                    it.isFile
                }
                .filter {
                    it.extension in listOf<String>("java", "kt")
                }
                .forEach {
                    val row = listOf(it.absolutePath, Utils.getFileMD5s(it, 64))
                    csvWriter.open(csvPath, append = true) {
                        writeRow(row)
                    }
                }

        Log.v(TAG, "耗时:${System.currentTimeMillis() - timeBegin}ms")

    }

    private fun loadMd5MapFromCSV(path: String): Map<String, String> {
        Log.v(TAG, "从[${path}]加载md5信息...")

        return if (!File(path).exists()) {
            Log.v(TAG, "文件[${path}]不存在")
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


}