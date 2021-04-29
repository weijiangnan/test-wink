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

import com.immomo.litebuild.util.Log
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle

const val KEY_FIRSTGEN = "DIFF_FIRST_GEN"

class DiffHelper(project: Project) {
    companion object {
        val TAG = "momo.diff"
    }

    fun diff() {
        // todo
        com.immomo.litebuild.Settings.getData().changedJavaFiles.add("app/src/main/java/com/google/samples/apps/sunflower/test/TestJava.java")
    }

    init {
        project.gradle.addBuildListener(object : BuildListener {
            override fun buildStarted(gradle: Gradle) {
                gradle.rootProject.childProjects.forEach {
                    Log.v(TAG, it)
                }

            }

            override fun settingsEvaluated(settings: Settings) {}
            override fun projectsLoaded(gradle: Gradle) {}
            override fun projectsEvaluated(gradle: Gradle) {}
            override fun buildFinished(result: BuildResult) {
                if (result.failure == null) {
                    updateMd5()
                }
            }
        })
    }

    private fun updateMd5() {


        if (InitEnvHelper.sProperties[KEY_FIRSTGEN]?.equals("true") == false) {
            saveMd5ToCSV()
        }
    }

    private fun saveMd5ToCSV() {
        Log.v(TAG, "saveMd5ToCSV")
    }
}