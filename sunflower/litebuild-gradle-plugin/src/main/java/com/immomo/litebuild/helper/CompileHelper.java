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

package com.immomo.litebuild.helper;

import com.immomo.litebuild.Settings;
import com.immomo.litebuild.util.Utils;

import org.apache.http.util.TextUtils;
import org.gradle.api.Project;

import java.io.File;

public class CompileHelper {
    public void compileCode(Project project) {
//        for (Settings.Data.ProjectInfo info : Settings.getData().projectBuildSortList) {
            File file = new File(Settings.Data.TMP_PATH + "/tmp_class");
            if (!file.exists()) {
                file.mkdirs();
            }
//        }
        Settings.getData().changedJavaFiles.add("app/src/main/java/com/google/samples/apps/sunflower/test/TestJava.java");
        Settings.getData().changedKotlinFiles.add("app/src/main/java/com/google/samples/apps/sunflower/test/TestKotlin.kt");

        compileKotlin(project);
        compileJava(project);

        createDexPatch();
    }

    private int compileJava(Project project) {
        if (Settings.getData().changedJavaFiles.size() <= 0) {
            return 0;
        }

        StringBuilder sb = new StringBuilder();
        for (String path : Settings.getData().changedJavaFiles) {
            sb.append(" ");
            sb.append(path);
        }

        Utils.runShell(
                "javac" + Settings.getEnv().getProperty(project + "_javac_args")
                        + sb.toString()
        );

        return Settings.getData().changedJavaFiles.size();
    }

    private void compileKotlin(Project project) {
        if (Settings.getData().changedKotlinFiles.size() <= 0) {
            System.out.println("LiteBuild: ================> 没有 Kotlin 文件变更。");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String path : Settings.getData().changedKotlinFiles) {
            sb.append(" ");
            sb.append(path);
        }
        String kotlincHome = System.getenv("KOTLINC_HOME");
        if (TextUtils.isEmpty(kotlincHome)) {
            System.out.println();
            System.out.println("================== 请配置 KOTLINC_HOME ==================");
            System.out.println("1. 打开：~/.bash_profile");
            System.out.println("2. 添加：export KOTLINC_HOME=\"/Applications/Android\\ Studio.app/Contents/plugins/Kotlin/kotlinc/bin/kotlinc\"");
            System.out.println("3. 执行：source ~/.bash_profile");
            System.out.println("========================================================");
            System.out.println();
            return;
        }
        // 如果路径包含空格，需要替换 " " 为 "\ "
        if (!kotlincHome.contains("\\")) {
            kotlincHome = kotlincHome.replace(" ", "\\ ");
        }
        System.out.println("[LiteBuild] kotlincHome : " + kotlincHome);
        try {
            String mainKotlincArgs = Settings.getEnv().getProperty(project.getName() + "_kotlinc_args");
            String javaHomePath = Settings.getEnv().getProperty("java_home");
            javaHomePath = javaHomePath.replace(" ", "\\ ");
            String shellCommand = kotlincHome + " -jdk-home " + javaHomePath + mainKotlincArgs + sb.toString();
            System.out.println("[LiteBuild] shellCommand : " + kotlincHome);
            Utils.runShell(shellCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDexPatch() {
        String cmds = new String();
        cmds += "source ~/.bash_profile";
        cmds += '\n' + Settings.getEnv().getProperty("build_tools_dir") + "/dx --dex --no-strict --output "
                + Settings.Data.TMP_PATH + "/patch0.dex " +  Settings.Data.TMP_PATH + "/tmp_class/";
        cmds += '\n' + "adb push " + Settings.Data.TMP_PATH + "/patch0.dex /sdcard/";
//        cmds += '\n' + "adb shell am force-stop " + APP_PACKAGE;
//        cmds += '\n' + "adb shell am start -n " + APP_PACKAGE + "/" + LAUNCH_ACTIVITY;

        Utils.runShell(cmds);
    }
}
