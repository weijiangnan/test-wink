package com.immomo.litebuild.helper;


import com.immomo.litebuild.LitebuildOptions;
import com.immomo.litebuild.Settings;
import com.immomo.litebuild.util.Log;
import com.immomo.litebuild.util.Utils;

import java.io.File;
import java.util.Locale;

public class CompileHelper {

    public void compileCode() {
        File file = new File(Settings.env.tmpPath + "/tmp_class");
        if (!file.exists()) {
            file.mkdirs();
        }

        for (Settings.ProjectTmpInfo project : Settings.data.projectBuildSortList) {
            compileKotlin(project);
        }

        for (Settings.ProjectTmpInfo project : Settings.data.projectBuildSortList) {
            compileJava(project);
        }

        createDexPatch();
    }

    private int compileJava(Settings.ProjectTmpInfo project) {
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

        String shellCommand = "javac" + project.fixedInfo.javacArgs
                + sb.toString();
//        System.out.println("[LiteBuild] : javac shellCommand = " + shellCommand);
        System.out.println("[LiteBuild] projectName : " + project.fixedInfo.name);
        Utils.runShell(
//                "javac" + Settings.getEnv().getProperty(project + "_javac_args")
                shellCommand
        );

        Settings.data.hasClassChanged = true;

        return project.changedJavaFiles.size();
    }

    private void compileKotlin(Settings.ProjectTmpInfo project) {

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

        String kotlinHome = System.getenv("KOTLIN_HOME");
        if (kotlinHome == null || kotlinHome.equals("")) {
            kotlinHome = "/Applications/Android Studio.app/Contents/plugins/Kotlin";
        }

        String kotlinc = getKotlinc();

        System.out.println("[LiteBuild] kotlincHome : " + kotlinc);
        System.out.println("[LiteBuild] projectName : " + project.fixedInfo.name);
        try {
            String mainKotlincArgs = project.fixedInfo.kotlincArgs;
            String kotlinxArgs = buildKotlinAndroidPluginCommand(kotlinHome, project);
            String javaHomePath = Settings.env.javaHome;
            javaHomePath = javaHomePath.replace(" ", "\\ ");
            String shellCommand = "sh " + kotlinc + kotlinxArgs + " -jdk-home " + javaHomePath + mainKotlincArgs + sb.toString();
//            System.out.println("[LiteBuild] kotlinc shellCommand : " + shellCommand);
            Utils.runShell(shellCommand);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Settings.data.hasClassChanged = true;
    }

    private String getKotlinc() {
        String kotlinc = System.getenv("KOTLINC_HOME");
        if (kotlinc == null || kotlinc.equals("")) {
            kotlinc = "/Applications/Android Studio.app/Contents/plugins/Kotlin/kotlinc/bin/kotlinc";
        }

        if (kotlinc == null || kotlinc.equals("") || !new File(kotlinc).exists()) {
            kotlinc = "/Applications/AndroidStudio.app/Contents/plugins/Kotlin/kotlinc/bin/kotlinc";
        }

        if (kotlinc == null || kotlinc.equals("")) {
            if (!new File(kotlinc).exists()) {
                System.out.println();
                Log.e("================== 请配置 KOTLINC_HOME ==================");
                Log.e("1. 打开：~/.bash_profile");
                Log.e("2. 添加：export KOTLINC_HOME=\"/Applications/Android\\ Studio.app/Contents/plugins/Kotlin/kotlinc/bin/kotlinc\"");
                Log.e("3. 执行：source ~/.bash_profile");
                Log.e("========================================================");
                System.out.println();
            }

            return "";
        }

        // 如果路径包含空格，需要替换 " " 为 "\ "
        if (!kotlinc.contains("\\")) {
            kotlinc = kotlinc.replace(" ", "\\ ");
        }

        return kotlinc;
    }

    private String buildKotlinAndroidPluginCommand(String kotlinHome, Settings.ProjectTmpInfo projectInfo) {
        LitebuildOptions options = Settings.env.options;
        String args = "";
        if (options.kotlinSyntheticsEnable) {
            String pluginHome = kotlinHome + "/kotlinc/lib/android-extensions-compiler.jar";
            String packageName = Settings.env.debugPackageName;
            String flavor = "main";
            String resPath = projectInfo.fixedInfo.dir + "/src/" + flavor + "/res";

            args = String.format(Locale.US, " -Xplugin=%s " +
                    "-P plugin:org.jetbrains.kotlin.android:package=%s " +
                    "-P plugin:org.jetbrains.kotlin.android:variant='%s;%s' ", pluginHome, packageName, flavor, resPath);
        }
        System.out.println("【compile kotlinx.android.synthetic】 \n" + args);
        return args;
    }

    private void createDexPatch() {
        if (!Settings.data.hasClassChanged) {
            // 没有数据变更
            return;
        }
        String destPath = "/sdcard/Android/data/" + Settings.env.debugPackageName + "/patch_file/";

        String patchName = Settings.env.version + "_patch.jar";
        String cmds = new String();

        Log.TimerLog log = Log.timerStart("!!!!!!!!!!!!!!!");

        Utils.runShell("source ~/.bash_profile" +
                '\n' + "adb shell mkdir " + destPath);

//        String classpath = " --classpath " + Settings.env.projectTreeRoot.classPath.replace(":", " --classpath ");
//        classpath = "";
        String dest = Settings.env.tmpPath + "/tmp_class.zip";
        cmds += "source ~/.bash_profile";
        cmds += '\n' + "rm -rf " + dest;
        cmds += '\n' + "cd " + Settings.env.tmpPath + "/tmp_class";
        cmds += '\n' + "zip -r -o -q " + dest +  " *";
        cmds += '\n' + Settings.env.buildToolsDir + "/d8 --intermediate --output " + Settings.env.tmpPath + "/" + patchName
                + " " + Settings.env.tmpPath + "/tmp_class.zip";

//        cmds += '\n' + Settings.env.buildToolsDir + "/dx --dex --no-strict --output "
//                + Settings.env.tmpPath + "/" + patchName + " " +  Settings.env.tmpPath + "/tmp_class/";

//        /Users/weijiangnan/Desktop/opt/sdk/build-tools/30.0.3/d8 /Users/weijiangnan/Desktop/git2/litebuild/sunflower/.idea/litebuild/tmp_class/com/immomo/wink/*.class --intermediate --output /Users/weijiangnan/Desktop/git2/litebuild/sunflower/.idea/litebuild/1622793498989_patch.jar --lib /Users/weijiangnan/Desktop/opt/sdk/platforms/android-29/android.jar

        cmds += '\n' + "adb shell mkdir " + destPath;
        cmds += '\n' + "adb push " + Settings.env.tmpPath + "/" + patchName + " " + destPath;

        System.out.println("安装 CMD 命令：" + cmds);

        Utils.runShell(cmds);

        log.end();
    }
}