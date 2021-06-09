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

import com.immomo.litebuild.Constant;
import com.immomo.litebuild.Settings;
import com.immomo.litebuild.util.Log;
import com.immomo.litebuild.util.Utils;

public class IncrementPatchHelper {
    public boolean patchToApp() {
        if (!Settings.data.hasClassChanged && !Settings.data.hasResourceChanged) {
            Log.v("No..nothing to do.");
            return false;
        }


        Log.v(Constant.TAG, "No ------ !!" + Settings.data.hasClassChanged + Settings.data.hasResourceChanged);

        createPatchFile();
        patchDex();
        patchResources();
        restartApp();

        return true;
    }

    public void createPatchFile() {
        Settings.data.patchPath = "/sdcard/Android/data/" + Settings.env.debugPackageName + "/patch_file/";
        Utils.ShellResult result = Utils.runShells("source ~/.bash_profile",
                "adb shell mkdir " + Settings.data.patchPath);
        boolean noPermission = false;
        for (String error: result.getErrorResult()) {
            if (error.contains("Permission denied")) {
                // 标志没文件权限
                noPermission = true;
                break;
            }
        }

//        if (noPermission) {
            Settings.data.patchPath = "/sdcard/" + Settings.NAME + "/patch_file/";
            Utils.runShells("source ~/.bash_profile\n" +
                    "adb shell mkdir " + Settings.data.patchPath);
//        }
    }

    public void patchDex() {
        if (!Settings.data.hasClassChanged) {
            return;
        }

        String patchName = Settings.env.version + "_patch.jar";
        Utils.runShells("source ~/.bash_profile\n" + "adb push " + Settings.env.tmpPath + "/" + patchName + " " + Settings.data.patchPath);
//        Utils.runShells("source ~/.bash_profile",
//                "adb push " + Settings.env.tmpPath + "/" + patchName + " " + Settings.data.patchPath);
    }

    public void patchResources() {
        if (!Settings.data.hasResourceChanged) {
            return;
        }

        String patchName = Settings.env.version + "_resources-debug.apk";
        Utils.runShells("source ~/.bash_profile\n" +
                "adb shell rm -rf " + Settings.data.patchPath + "apk\n" +
                "adb shell mkdir " + Settings.data.patchPath + "apk\n" +
                "adb push " + Settings.env.tmpPath + "/" + patchName + " " + Settings.data.patchPath + "apk/");

//        Utils.runShells("source ~/.bash_profile",
//                "adb shell rm -rf " + Settings.data.patchPath + "apk",
//                "adb shell mkdir " + Settings.data.patchPath + "apk",
//                "source ~/.bash_profile\nadb push " + Settings.env.tmpPath + "/" + patchName + " " + Settings.data.patchPath + "apk/");

    }

    public void restartApp() {
        String cmds = "";
        cmds += "source ~/.bash_profile";
        cmds += '\n' + "adb shell am force-stop " + Settings.env.debugPackageName;
        cmds += '\n' + "adb shell am start -n " + Settings.env.debugPackageName + "/" + Settings.env.launcherActivity;
        Utils.runShell(cmds);
    }
}
