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

package com.immomo.litebuild;

import org.gradle.api.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import com.immomo.litebuild.helper.InitEnvHelper;
import com.immomo.litebuild.util.Log;

public class Settings {
    static InitEnvHelper sEnvHelper = new InitEnvHelper();
    public static Project project;
    public static Properties getPropertiesEnv() {
        return sEnvHelper.getPropertiesEnv();
    }

    public static void init(Project p) {
        Log.TimerLog timer = Log.timerStart("litebuildInit");

        project = p;
        // 创建文件夹
        Data.TMP_PATH = project.getRootDir().getPath() + "/.idea/litebuild";
        File file = new File(Data.TMP_PATH);
        if(!file.exists()) { //如果文件夹不存在
            file.mkdirs(); //创建文件夹
        }

        sEnvHelper.initEnv(p);

        timer.end();
    }

    static Data sData = new Data();
    public static Data getData() {
        return sData;
    }

    public static Data initData() {
        sData = new Data();
        return sData;
    }

    public static class Data {
        public static String APP_PROJECT_DIR = "";
        public static String TMP_PATH = "../.idea/litebuild";
        public static String NAME = "litebuild";
        public static String PACKAGE_NAME = "";
        public List<String> changedJavaFiles = new ArrayList<>();
        public List<String> changedKotlinFiles = new ArrayList<>();
        public ProjectInfo projectTreeRoot = null;
        public List<ProjectInfo> projectBuildSortList = new ArrayList<>();
        public boolean hasResourceChanged = false;
        public boolean hasClassChanged = false;
        public boolean needProcessDebugResources = false;
        public String newVersion = "";
        public String version = "";

        public static class ProjectInfo {
            private Project project;
            private String dir;
            private String javacArgs;
            private List<ProjectInfo> children = new ArrayList<>();
            public List<String> changedJavaFiles = new ArrayList<>();
            public List<String> changedKotlinFiles = new ArrayList<>();
            public boolean hasResourceChanged = false;
            public boolean projectIgnore = false;

            public Project getProject() {
                return project;
            }

            public ProjectInfo setProject(Project project) {
                this.project = project;
                return this;
            }

            public String getDir() {
                return dir;
            }

            public ProjectInfo setDir(String dir) {
                this.dir = dir;
                return this;
            }

            public String getJavacArgs() {
                return javacArgs;
            }

            public ProjectInfo setJavacArgs(String javacArgs) {
                this.javacArgs = javacArgs;
                return this;
            }

            public List<ProjectInfo> getChildren() {
                return children;
            }

            public ProjectInfo setChildren(List<ProjectInfo> children) {
                this.children = children;
                return this;
            }
        }
    }
}
