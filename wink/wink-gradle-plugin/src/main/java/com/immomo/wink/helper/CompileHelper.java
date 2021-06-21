package com.immomo.wink.helper;


import com.immomo.wink.WinkOptions;
import com.immomo.wink.Settings;
import com.immomo.wink.util.WinkLog;
import com.immomo.wink.util.Utils;

import java.io.File;
import java.util.HashSet;
import java.util.List;
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
        WinkLog.d("compileJava ================================");
        WinkLog.d("changedJavaFiles : " + project.changedJavaFiles.toString());
        WinkLog.d("compileJava ================================");

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
        WinkLog.d("[LiteBuild] : javac shellCommand = " + shellCommand);
        WinkLog.d("[LiteBuild] projectName : " + project.fixedInfo.name);
        Utils.runShell(
//                "javac" + Settings.getEnv().getProperty(project + "_javac_args")
                shellCommand
        );

        Settings.data.hasClassChanged = true;

        return project.changedJavaFiles.size();
    }

    private void compileKotlin(Settings.ProjectTmpInfo project) {

        WinkLog.d("compileKotlin ================================");
        WinkLog.d("changedKotlinFiles : " + project.changedKotlinFiles.toString());
        WinkLog.d("compileKotlin ================================");

        if (project.changedKotlinFiles.size() <= 0) {
            WinkLog.d("LiteBuild: ================> 没有 Kotlin 文件变更。");
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

        WinkLog.d("[LiteBuild] kotlincHome : " + kotlinc);
        WinkLog.d("[LiteBuild] projectName : " + project.fixedInfo.name);
        try {
            String mainKotlincArgs = project.fixedInfo.kotlincArgs;

            // todo apt
//            String kotlinxArgs = buildKotlinAndroidPluginCommand(kotlinHome, project);

            String javaHomePath = Settings.env.javaHome;
            javaHomePath = javaHomePath.replace(" ", "\\ ");

            // todo apt
//            String shellCommand = "sh " + kotlinc + kotlinxArgs + " -jdk-home " + javaHomePath
//                    + mainKotlincArgs + sb.toString();
            String shellCommand = "sh " + kotlinc + " -jdk-home " + javaHomePath
                    + mainKotlincArgs + sb.toString();

//            WinkLog.d("[LiteBuild] kotlinc shellCommand : " + shellCommand);
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
                WinkLog.e("\n\n================== 请配置 KOTLINC_HOME ==================");
                WinkLog.e("1. 打开：~/.bash_profile");
                WinkLog.e("2. 添加：export KOTLINC_HOME=\"/Applications/Android\\ Studio.app/Contents/plugins/Kotlin/kotlinc/bin/kotlinc\"");
                WinkLog.e("3. 执行：source ~/.bash_profile");
                WinkLog.e("========================================================\n\n");
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
        WinkOptions options = Settings.env.options;
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
        WinkLog.i("【compile kotlinx.android.synthetic】 \n" + args);
        return args;
    }

    private void findAptRelativeFiles(List<String> changeJavaFiles) {
        HashSet<String> fileSet = new HashSet<>();
    }

    private void createDexPatch() {
        if (!Settings.data.hasClassChanged) {
            // 没有数据变更
            return;
        }

        String patchName = Settings.env.version + "_patch.jar";
        String cmds = useD8(patchName);

        WinkLog.TimerLog log = WinkLog.timerStart("开始打DexPatch！");

        Utils.runShell(cmds);

        log.end();
    }

    public String useD8(String patchName) {
        String cmds = "";
        String dest = Settings.env.tmpPath + "/tmp_class.zip";
        cmds += "source ~/.bash_profile";
        cmds += '\n' + "rm -rf " + dest;
        cmds += '\n' + "cd " + Settings.env.tmpPath + "/tmp_class";
        cmds += '\n' + "zip -r -o -q " + dest +  " *";
        cmds += '\n' + Settings.env.buildToolsDir + "/d8 --intermediate --output " + Settings.env.tmpPath + "/" + patchName
                + " " + Settings.env.tmpPath + "/tmp_class.zip";

        if (Settings.data.hasResourceAddOrRename) {
            cmds += " " + Settings.env.appProjectDir + "/build/intermediates/compile_and_runtime_not_namespaced_r_class_jar/" + Settings.env.variantName + "/R.jar";
        }
        return cmds;
    }

    public String useDx(String patchName, String destPath) {
//        Utils.runShell("source ~/.bash_profile" +
//                '\n' + "adb shell mkdir " + destPath);

//        String classpath = " --classpath " + Settings.env.projectTreeRoot.classPath.replace(":", " --classpath ");
//        classpath = "";

//        WinkLog.v("Dex生成命令cmd =======\n" + cmds);
        String cmds = "";
        cmds += '\n' + Settings.env.buildToolsDir + "/dx --dex --no-strict --output "
                + Settings.env.tmpPath + "/" + patchName + " " +  Settings.env.tmpPath + "/tmp_class/";

        cmds += '\n' + "adb shell mkdir " + destPath;
        cmds += '\n' + "adb push " + Settings.env.tmpPath + "/" + patchName + " " + destPath;
        return cmds;
    }
}