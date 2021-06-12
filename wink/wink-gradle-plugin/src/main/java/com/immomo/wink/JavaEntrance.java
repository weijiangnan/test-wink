package com.immomo.wink;

import com.immomo.wink.helper.ResourceHelper;
import com.immomo.wink.helper.CompileHelper;
import com.immomo.wink.helper.DiffHelper;
import com.immomo.wink.helper.IncrementPatchHelper;
import com.immomo.wink.helper.InitEnvHelper;
import com.immomo.wink.util.Log;
import com.immomo.wink.util.Utils;

import java.util.List;

public class JavaEntrance {

    public static void main(String[] args) {

        if (args == null || args.length == 0) {
            com.immomo.wink.util.Log.e("Java 命令需要指定参数：path");
            return;
        }

        com.immomo.wink.util.Log.cyan("====== 开始执行 Java 任务 ======");

        String path = args[0];
//        String func = args[1];

        com.immomo.wink.util.Log.cyan("====== path : " + path);
//        System.out.println("====== Func : " + func);

        InitEnvHelper helper = new InitEnvHelper();
        boolean envFileExist = helper.isEnvExist(path);
        com.immomo.wink.util.Log.cyan("======> envFileExist : " + envFileExist);

        if (!envFileExist) {
            runWinkCommand(path);
            return;
        }

        helper.initEnvByPath(path);
//        new InitEnvHelper().initEnvByPath("/Users/momo/Documents/MomoProject/wink/sunflower");

        List<com.immomo.wink.Settings.ProjectTmpInfo> projectBuildSortList = com.immomo.wink.Settings.data.projectBuildSortList;
        System.out.println("projectBuildSortList : " + projectBuildSortList.toString());

        boolean hasFileChanged = diff();  // 更新：Settings.data.hasResourceChanged

        if (!hasFileChanged) {
            com.immomo.wink.util.Log.cyan("======>>> 没有文件变更");
        }

        new ResourceHelper().checkResource(); // 内部判断：Settings.data.hasResourceChanged

        // 编译资源
        if (com.immomo.wink.Settings.data.needProcessDebugResources) {
//            new ResourceHelper().packageResources();
            com.immomo.wink.util.Log.cyan("======>>> 资源变更，执行 gradle task");
            runWinkCommand(path);
            return;
        }

        Log.cyan("======>>> 没有资源变更");
        new CompileHelper().compileCode();
        if (new IncrementPatchHelper().patchToApp()) {
            updateSnapShot();
        }

    }

    private static void runWinkCommand(String path) {
        String scriptStr = "cd " + path + " && " + " ./gradlew wink";
        try {
            Utils.executeScript(scriptStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateSnapShot() {
        for (com.immomo.wink.Settings.ProjectTmpInfo info : com.immomo.wink.Settings.data.projectBuildSortList) {
            if (info.changedJavaFiles.size() > 0 || info.changedKotlinFiles.size() > 0) {
                new com.immomo.wink.helper.DiffHelper(info).initSnapshotForCode();
            }

            if (info.hasResourceChanged) {
                new com.immomo.wink.helper.DiffHelper(info).initSnapshotForRes();
            }
        }
    }

    public static boolean diff() {
        System.out.println("====== diff run ~~~ ======");

        for (com.immomo.wink.Settings.ProjectTmpInfo projectInfo : com.immomo.wink.Settings.data.projectBuildSortList) {
            long startTime = System.currentTimeMillis();
            new DiffHelper(projectInfo).diff(projectInfo);
            System.out.println("=================>>>>>> " + projectInfo.fixedInfo.name + "结束一组耗时：" + (System.currentTimeMillis() - startTime) + " ms");
        }
//
        for (com.immomo.wink.Settings.ProjectTmpInfo projectInfo : com.immomo.wink.Settings.data.projectBuildSortList) {
            if (projectInfo.hasResourceChanged) {
                System.out.println("遍历是否有资源修改, name=" + projectInfo.fixedInfo.dir);
                System.out.println("遍历是否有资源修改, changed=" + projectInfo.hasResourceChanged);
                com.immomo.wink.Settings.data.hasResourceChanged = true;
                break;
            }
        }
        return Settings.data.hasResourceChanged;
    }

}
