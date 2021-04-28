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

import java.io.File;

public class CompileHelper {
    public void compileCode() {
        File file = new File(Settings.Data.TMP_PATH + "/tmp_class");
        if (!file.exists()) {
            file.mkdirs();
        }

        compileJava();
        compileKotlin();

        createDexPatch();
    }

    private int compileJava() {
        if (Settings.getData().changedJavaFiles.size() <= 0) {
            return 0;
        }

        StringBuilder sb = new StringBuilder();
        for (String path : Settings.getData().changedJavaFiles) {
            sb.append(" ");
            sb.append(path);
        }

        Utils.runShell(
                "javac" + Settings.getEnv().getProperty("main_javac_args")
                        + sb.toString()
        );

        return Settings.getData().changedJavaFiles.size();
    }

    private void compileKotlin() {

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
