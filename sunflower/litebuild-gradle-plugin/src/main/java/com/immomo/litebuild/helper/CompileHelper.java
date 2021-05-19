package com.immomo.litebuild.helper;


import com.immomo.litebuild.Settings;
import com.immomo.litebuild.util.Utils;
import org.apache.http.util.TextUtils;

import java.io.File;
import java.io.IOException;

public class CompileHelper {
    public void compileCode() {
        File file = new File(Settings.Data.TMP_PATH + "/tmp_class");
        if (!file.exists()) {
            file.mkdirs();
        }

        for (Settings.Data.ProjectInfo project : Settings.getData().projectBuildSortList) {
            compileKotlin(project);
        }

        for (Settings.Data.ProjectInfo project : Settings.getData().projectBuildSortList) {
            compileJava(project);
        }

        createDexPatch();
    }

    private int compileJava(Settings.Data.ProjectInfo project) {
        System.out.println("compileJava ================================");
        System.out.println("changedJavaFiles : " + project.changedJavaFiles.toString());
        System.out.println("compileJava ================================");

        if (project.changedJavaFiles.size() <= 0) {
            return 0;
        }

        StringBuilder sb = new StringBuilder();
        for (String path : project.changedJavaFiles) {
            sb.append(" ");
            sb.append(path);
        }

        String shellCommand = "javac" + Settings.getEnv().getProperty(project.getProject().getName() + "_javac_args")
                + sb.toString();
//        System.out.println("[LiteBuild] : javac shellCommand = " + shellCommand);
        System.out.println("[LiteBuild] projectName : " + project.getProject().getName());
        Utils.runShell(
//                "javac" + Settings.getEnv().getProperty(project + "_javac_args")
                shellCommand
        );

        Settings.getData().hasClassChanged = true;

        return project.changedJavaFiles.size();
    }

    private void compileKotlin(Settings.Data.ProjectInfo project) {

        System.out.println("compileKotlin ================================");
        System.out.println("changedKotlinFiles : " + project.changedKotlinFiles.toString());
        System.out.println("compileKotlin ================================");

        if (project.changedKotlinFiles.size() <= 0) {
            System.out.println("LiteBuild: ================> 没有 Kotlin 文件变更。");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String path : project.changedKotlinFiles) {
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
        System.out.println("[LiteBuild] projectName : " + project.getProject().getName());
        try {
            String mainKotlincArgs = Settings.getEnv().getProperty(project.getProject().getName() + "_kotlinc_args");
            String javaHomePath = Settings.getEnv().getProperty("java_home");
            javaHomePath = javaHomePath.replace(" ", "\\ ");
            String shellCommand = kotlincHome + " -jdk-home " + javaHomePath + mainKotlincArgs + sb.toString();
//            System.out.println("[LiteBuild] kotlinc shellCommand : " + shellCommand);
            Utils.runShell(shellCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Settings.getData().hasClassChanged = true;
    }

    private void createDexPatch() {
        if (!Settings.getData().hasClassChanged) {
            // 没有数据变更
            return;
        }

        String cmds = new String();
        cmds += "source ~/.bash_profile";
        cmds += '\n' + Settings.getEnv().getProperty("build_tools_dir") + "/dx --dex --no-strict --output "
                + Settings.Data.TMP_PATH + "/patch0.dex " +  Settings.Data.TMP_PATH + "/tmp_class/";
//        cmds += '\n' + "adb push " + Settings.Data.TMP_PATH + "/patch0.dex /sdcard/";
//        cmds += '\n' + "adb shell am force-stop " + APP_PACKAGE;
//        cmds += '\n' + "adb shell am start -n " + APP_PACKAGE + "/" + LAUNCH_ACTIVITY;

        System.out.println("安装 CMD 命令：" + cmds);
        Utils.runShell(cmds);
        try {
            Utils.executeScript("adb push " + Settings.Data.TMP_PATH + "/patch0.dex /sdcard/");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("adb push " + Settings.Data.TMP_PATH + "/patch0.dex /sdcard/");
    }
}
