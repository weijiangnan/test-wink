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

public class CleanupHelper {
    public void cleanup() {
        deleteAllApk();
        deleteAllDex();
        delete("diff");
        delete("tmp_class");
        delete("env.properties");
        delete("stableIds.txt");

        // 删除手机上的patch文件
        deletePatchFileOnPhone();
    }

    public void cleanOnAssemble() {
        deleteAllApk();
        deleteAllDex();

        delete("diff");
        delete("tmp_class");
        delete("env.properties");

        // 删除手机上的patch文件
        deletePatchFileOnPhone();
    }


    public void deletePatchFileOnPhone() {
        String destPath = "/sdcard/Android/data/" + Settings.Data.PACKAGE_NAME + "/patch_file/";
        String cmds = "";
        cmds += "source ~/.bash_profile";
        cmds += '\n' + "adb shell rm -rf " + destPath;
        cmds += '\n' + "adb shell mkdir " + destPath;
        Utils.runShell(cmds);
    }

    public void deleteAllApk() {
        System.out.println("删除文件deleteAllApk :" + Settings.Data.TMP_PATH);
        File f = new File(Settings.Data.TMP_PATH);
        if (f.exists() && f.isDirectory()) {
            File[] apks = f.listFiles(pathname -> pathname.getName().endsWith("apk"));
            if (apks != null) {
                for (File a : apks) {
                    a.delete();
                }
            }
        }
    }

    public void deleteAllDex() {
        System.out.println("删除文件deleteAllDex :" + Settings.Data.TMP_PATH);
        File f = new File(Settings.Data.TMP_PATH);
        if (f.exists() && f.isDirectory()) {
            File[] files = f.listFiles(pathname -> pathname.getName().endsWith("dex"));
            if (files != null) {
                for (File a : files) {
                    a.delete();
                }
            }
        }
    }

    public void delete(String path) {
        System.out.println("删除文件:" + Settings.Data.TMP_PATH + "/" + path);
        File f = new File(Settings.Data.TMP_PATH + "/" + path);
        deleteFile(f);
    }

    public boolean deleteFile(File dirFile) {
        // 如果dir对应的文件不存在，则退出
        if (!dirFile.exists()) {
            return false;
        }

        if (dirFile.isFile()) {
            return dirFile.delete();
        } else {

            for (File file : dirFile.listFiles()) {
                deleteFile(file);
            }
        }

        return dirFile.delete();
    }
}