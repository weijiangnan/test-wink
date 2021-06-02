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
package com.immomo.litebuild

import com.immomo.litebuild.util.LocalCacheUtil
import java.io.Serializable
import java.util.*

object Settings {
    @JvmField
    var NAME = "litebuild"

    @JvmField
    var env = Env()

    @JvmStatic
    fun restoreEnv(filePath: String?): Env? {
        var newEnv = LocalCacheUtil.getCache<Env>(filePath)
        newEnv?.let {
            env = newEnv
        }
        return env
    }

    @JvmStatic
    fun storeEnv(env: Env, filePath: String?) {
        LocalCacheUtil.save2File(env, filePath)
        this.env = env
    }

    @JvmField
    var data = Data()

    @JvmStatic
    fun initData(): Data {
        data = Data()
        env!!.projectBuildSortList.forEach {
            data.projectBuildSortList.add(ProjectTmpInfo(it))
        }

        return data
    }

    data class Env(@JvmField var javaHome: String? = null,
                   @JvmField var rootDir: String? = null,
                   @JvmField var version: String? = null,
                   @JvmField var sdkDir: String? = null,
                   @JvmField var buildToolsVersion: String? = null,
                   @JvmField var buildToolsDir: String? = null,
                   @JvmField var compileSdkVersion: String? = null,
                   @JvmField var compileSdkDir: String? = null,
                   @JvmField var debugPackageName: String? = null,
                   @JvmField var launcherActivity: String? = null,
                   @JvmField var appProjectDir: String? = null,
                   @JvmField var tmpPath: String = "",
                   @JvmField var packageName: String? = null,
                   @JvmField var projectTreeRoot: ProjectFixedInfo? = null,
                   @JvmField var projectBuildSortList: MutableList<ProjectFixedInfo> = ArrayList(),
                   @JvmField var options: LitebuildOptions? = null) : Serializable {
    }

    data class Data(@JvmField var projectBuildSortList: MutableList<ProjectTmpInfo> = ArrayList(),
                    @JvmField var hasResourceChanged: Boolean = false,
                    @JvmField var hasClassChanged: Boolean = false,
                    @JvmField var needProcessDebugResources: Boolean = false,
                    @JvmField var newVersion: String = "") {
    }

    data class ProjectFixedInfo(@JvmField var children: MutableList<ProjectFixedInfo> = ArrayList(),
                                @JvmField var isProjectIgnore: Boolean = false,
                                @JvmField var name: String = "",
                                @JvmField var dir: String = "",
                                @JvmField var buildDir: String? = null,
                                @JvmField var manifestPath: String? = null,
                                @JvmField var javacArgs: String? = null,
                                @JvmField var kotlincArgs: String? = null) : Serializable

    class ProjectTmpInfo(@JvmField var fixedInfo: ProjectFixedInfo,
                         @JvmField var changedJavaFiles: MutableList<String> = ArrayList(),
                         @JvmField var changedKotlinFiles: MutableList<String> = ArrayList(),
                         @JvmField var hasResourceChanged: Boolean = false)
}