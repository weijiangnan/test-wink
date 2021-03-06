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
import com.google.gson.Gson
import com.immomo.litebuild.Settings
import com.immomo.litebuild.util.Log
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.*
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevTree
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.gradle.api.Project
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

const val KEY_COMMIT_ID = "key_commit_id"

class DiffHelper(var project: Settings.ProjectTmpInfo) {
    companion object {
        const val TAG = "litebuild.diff"
    }


    private var git: Git
    private var repo: Repository
    private var diffDir: String
    private var diffPropertiesPath: String
    private var csvPathCode: String
    private var csvPathRes: String
    private val scanPathCode: String
    private var scanPathRes: String
    private val extensionList = listOf("java", "kt", "xml", "json", "png", "jpeg", "webp")

    private var csvReader: CsvReader
    private var csvWriter: CsvWriter
    private val properties = Properties()

    init {
        Log.v(TAG, "[${project.fixedInfo.name}]:init")

        val moduleName = project.fixedInfo.name
        diffDir = "${Settings.env!!.rootDir}/.idea/litebuild/diff/${moduleName}"

        scanPathCode = "${project.fixedInfo.dir}/src/main/java"
        scanPathRes = "${project.fixedInfo.dir}/src/main/res"

        csvPathCode = "${diffDir}/md5_code.csv"
        csvPathRes = "${diffDir}/md5_res.csv"

        diffPropertiesPath = "${diffDir}/ps_diff.properties"

        val ctxCsvWriter = CsvWriterContext()
        val ctxCsvReader = CsvReaderContext()
        csvWriter = CsvWriter(ctxCsvWriter)
        csvReader = CsvReader(ctxCsvReader)

        if (!File(diffPropertiesPath).exists()) {
            File(diffPropertiesPath).parentFile.mkdirs()
            File(diffPropertiesPath).createNewFile()
        }
        properties.load(FileReader(diffPropertiesPath))

        //git
        repo = FileRepositoryBuilder()
                .findGitDir(File(Settings.env.rootDir))
                .build()

        git = Git(repo)
    }


    fun initSnapshot() {
        Log.v(TAG, "[${project.fixedInfo.name}]:initSnapshot ...")

        initSnapshotByMd5()
//        initSnapshotByGit()
    }

    fun initSnapshotForCode() {
        Log.v(TAG, "[${project.fixedInfo.name}]:initSnapshot ...")

        File(csvPathCode).takeIf { it.exists() }?.let { it.delete() }
        genSnapshotAndSaveToDisk(scanPathCode, csvPathCode)
    }

    fun initSnapshotForRes() {
        Log.v(TAG, "[${project.fixedInfo.name}]:initSnapshot ...")

        File(csvPathRes).takeIf { it.exists() }?.let { it.delete() }
        genSnapshotAndSaveToDisk(scanPathRes, csvPathRes)
    }

    fun diff(projectInfo: Settings.ProjectTmpInfo) {
        Log.v(TAG, "[${project.fixedInfo.name}]:????????????...")

        diffByMd5(projectInfo)
//        File(csvPathCode).takeIf { it.exists() }?.let { it.delete() }
//        File(csvPathRes).takeIf { it.exists() }?.let { it.delete() }

//        val triple = diffByGit()
//        projectInfo.changedJavaFiles.addAll(triple.first)
//        projectInfo.changedKotlinFiles.addAll(triple.second)
//        projectInfo.hasResourceChanged = triple.third
    }

    /**
     * ??????:git???
     */
    private fun initSnapshotByGit() {
        val lastCommitId: ObjectId = repo.resolve(Constants.HEAD)
        println("lastCommitId=$lastCommitId")

        properties.setProperty(KEY_COMMIT_ID, lastCommitId.name)
        properties.store(FileWriter(diffPropertiesPath), "diff properties")
    }

    /**
     * ??????:md5???
     */
    private fun initSnapshotByMd5() {
        File(csvPathCode).takeIf { it.exists() }?.let { it.delete() }
        File(csvPathRes).takeIf { it.exists() }?.let { it.delete() }

        genSnapshotAndSaveToDisk(scanPathCode, csvPathCode)
        genSnapshotAndSaveToDisk(scanPathRes, csvPathRes)
    }


    private fun diffByGit(): Triple<Set<String>, Set<String>, Boolean> {
        val setJava = mutableSetOf<String>()
        val setKt = mutableSetOf<String>()
        var isResChanged = false
        val rst = Triple<Set<String>, Set<String>, Boolean>(setJava, setKt, isResChanged)

        val lastCommitId = properties.getProperty(KEY_COMMIT_ID, "")
        val lastCommit = git.repository.resolve(lastCommitId)
        val revCommitOld = git.repository.parseCommit(lastCommit)

        val treeOld: AbstractTreeIterator? = prepareTreeParser(repo, revCommitOld)
        val diff = git.diff()
                .setShowNameAndStatusOnly(true)
                .setOldTree(treeOld)
                .call()

        println("Gson().toJson(diff)")
        println(Gson().toJson(diff))

        val rootParentPath = "${git.repository.directory.absolutePath.replace(".git", "")}"
        Log.v(str = "rootParentPath=${rootParentPath}")

        diff.forEach {
            when (it.changeType) {
                DiffEntry.ChangeType.ADD, DiffEntry.ChangeType.MODIFY, DiffEntry.ChangeType.COPY, DiffEntry.ChangeType.RENAME -> {
                    Log.v(str = "${it.changeType} newPath=${it.newPath}")
                    if (rootParentPath.plus(it.newPath).startsWith(project.fixedInfo.dir)) {
                        if (it.newPath.endsWith(".java")) {
                            setJava.add(rootParentPath.plus(it.newPath))
                        }
                        if (it.newPath.endsWith(".kt")) {
                            setKt.add(rootParentPath.plus(it.newPath))
                        }
                        if (it.newPath.endsWith(".kt")) {
                            isResChanged = true
                        }
                    }
                }

                DiffEntry.ChangeType.DELETE, DiffEntry.ChangeType.RENAME -> {
                    if (rootParentPath.plus(it.oldPath).startsWith(project.fixedInfo.dir)) {
                        Log.v(str = "${it.changeType} oldPath=${it.oldPath}")
                        if (it.oldPath.endsWith(".java")) {
                            setJava.add(rootParentPath.plus(it.oldPath))
                        }
                        if (it.oldPath.endsWith(".kt")) {
                            setKt.add(rootParentPath.plus(it.oldPath))
                        }
                        if (it.oldPath.contains("/src/main/res")) {
                            isResChanged = true
                        }
                    }
                }


            }
        }


        return rst
    }

    private fun diffByMd5(projectInfo: Settings.ProjectTmpInfo) {
        diffInner(scanPathCode, csvPathCode) {
            Log.v(TAG, "[${project.fixedInfo.name}]:    ????????????:$it")
            when {
                it.endsWith(".java") -> {
                    if (!projectInfo.changedJavaFiles.contains(it)) {
                        projectInfo.changedJavaFiles.add(it)
                    }
                }
                it.endsWith(".kt") -> {
                    if (!projectInfo.changedKotlinFiles.contains(it)) {
                        projectInfo.changedKotlinFiles.add(it)
                    }
                }
            }
        }

        diffInner(scanPathRes, csvPathRes) {
            Log.v(TAG, "[${project.fixedInfo.name}]:???????????????????????????????????????????????????:$it")
            Log.v(TAG, "????????????????????????$scanPathRes")
            projectInfo.hasResourceChanged = true
        }
    }

    private fun prepareTreeParser(repository: Repository, commit: RevCommit): AbstractTreeIterator? {
        println(commit.id)
        try {
            RevWalk(repository).use { walk ->
                println(commit.tree.id)
                val tree: RevTree = walk.parseTree(commit.tree.id)
                val oldTreeParser = CanonicalTreeParser()
                repository.newObjectReader().use { oldReader -> oldTreeParser.reset(oldReader, tree.id) }
                walk.dispose()
                return oldTreeParser
            }
        } catch (e: java.lang.Exception) {
        }
        return null
    }

    private fun diffInner(scanPath: String, csvPath: String, block: (String) -> Unit) {
        val mapOrigin = loadSnapshotToCacheFromDisk(csvPath)

        //Log.v(TAG, "????????????:")
        //Log.v(TAG, mapOrigin)

        if (mapOrigin.isEmpty()) {
            Log.v(TAG, "[${project.fixedInfo.name}]:??????????????????")
            return
        } else {
            val mapNew = hashMapOf<String, String>()
            genSnapshotAndSaveToCache(scanPath, mapNew)

            //Log.v(TAG, "?????????:")
            //Log.v(TAG, mapNew)

            Log.v(TAG, "[${project.fixedInfo.name}]:??????????????????...")
            compareMap(mapOrigin, mapNew)
                    .also { if (it.isEmpty()) Log.v(TAG, "[${project.fixedInfo.name}]:??????????????????") }
                    .forEach {
                        System.out.println(it)
                        block(it)
                    }

        }
    }

    private fun compareMap(map1: Map<String, String>, map2: Map<String, String>): Set<String> {
        Log.v(TAG, "[${project.fixedInfo.name}]:compareMap...")

        val rst = hashSetOf<String>()
        var m1 = map1
        var m2 = map2
        if (map1.size < map2.size) {
            m1 = map2
            m2 = map1
        }

        m1.forEach { (k, v) ->
            if (m2.containsKey(k)) {
                //????????????,?????????value
                if (m2[k] != v) {
                    rst.add(k)
                }
            } else {
                //???????????????,???????????????
                rst.add(k)
            }
        }

        return rst

    }


    private fun genSnapshotAndSaveToCache(path: String, map: HashMap<String, String>) {
//        Log.v(TAG, "[${project.fixedInfo.name}]:????????????[$path]??????java,kt??????,?????????md5????????????map...")
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

        Log.v(TAG, "[${project.fixedInfo.name}]:??????:${System.currentTimeMillis() - timeBegin}ms")
    }

    private fun genSnapshotAndSaveToDisk(path: String, csvPath: String) {
//        Log.v(TAG, "[${project.fixedInfo.name}]:????????????[$path]??????java,kt??????,??????md5????????????csv??????[$csvPath]")

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

        Log.v(TAG, "[${project.fixedInfo.name}]:??????:${System.currentTimeMillis() - timeBegin}ms")

    }

    private fun loadSnapshotToCacheFromDisk(path: String): Map<String, String> {
//        Log.v(TAG, "[${project.fixedInfo.name}]:???[${path}]??????md5??????...")

        return if (!File(path).exists()) {
            Log.v(TAG, "[${project.fixedInfo.name}]:??????[${path}]?????????")
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