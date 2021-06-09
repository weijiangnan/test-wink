package com.immomo.litebuild;

import com.immomo.litebuild.helper.CompileHelper;
import com.immomo.litebuild.helper.DiffHelper;
import com.immomo.litebuild.helper.IncrementPatchHelper;
import com.immomo.litebuild.helper.InitEnvHelper;
import com.immomo.litebuild.helper.ResourceHelper;
import com.immomo.litebuild.util.Log;
import com.immomo.litebuild.util.Utils;

import java.util.List;

public class JavaEntrance {

    public static void main(String[] args) {

        if (args == null || args.length == 0) {
            Log.e("Java 命令需要指定参数：path");
            return;
        }

        Log.cyan("====== 开始执行 Java 任务 ======");

        String path = args[0];
//        String func = args[1];

        Log.cyan("====== path : " + path);
//        System.out.println("====== Func : " + func);

        // /Users/momo/Documents/MomoProject/litebuild/sunflower
        new InitEnvHelper().initEnvByPath(path);
//        new InitEnvHelper().initEnvByPath("/Users/momo/Documents/MomoProject/litebuild/sunflower");

        List<Settings.ProjectTmpInfo> projectBuildSortList = Settings.data.projectBuildSortList;
        System.out.println("projectBuildSortList : " + projectBuildSortList.toString());

        boolean hasFileChanged = diff();  // 更新：Settings.data.hasResourceChanged

        if (!hasFileChanged) {
            Log.cyan("======>>> 没有文件变更");
        }

        new ResourceHelper().checkResource(); // 内部判断：Settings.data.hasResourceChanged

        // 编译资源
        if (Settings.data.needProcessDebugResources) {
//            new ResourceHelper().packageResources();
            Log.cyan("======>>> 资源变更，执行 gradle task");
            String scriptStr = "cd " + path + " && " + " ./gradlew litebuild";
            try {
                Utils.executeScript(scriptStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        Log.cyan("======>>> 没有资源变更");
        new CompileHelper().compileCode();
        if (new IncrementPatchHelper().patchToApp()) {
            updateSnapShot();
        }

    }

    private static void updateSnapShot() {
        for (Settings.ProjectTmpInfo info : Settings.data.projectBuildSortList) {
            if (info.changedJavaFiles.size() > 0 || info.changedKotlinFiles.size() > 0) {
                new DiffHelper(info).initSnapshotForCode();
            }

            if (info.hasResourceChanged) {
                new DiffHelper(info).initSnapshotForRes();
            }
        }
    }

    public static boolean diff() {
        System.out.println("====== diff run ~~~ ======");

        for (Settings.ProjectTmpInfo projectInfo : Settings.data.projectBuildSortList) {
            long startTime = System.currentTimeMillis();
            new DiffHelper(projectInfo).diff(projectInfo);
            System.out.println("=================>>>>>> " + projectInfo.fixedInfo.name + "结束一组耗时：" + (System.currentTimeMillis() - startTime) + " ms");
        }
//
        for (Settings.ProjectTmpInfo projectInfo : Settings.data.projectBuildSortList) {
            if (projectInfo.hasResourceChanged) {
                System.out.println("遍历是否有资源修改, name=" + projectInfo.fixedInfo.dir);
                System.out.println("遍历是否有资源修改, changed=" + projectInfo.hasResourceChanged);
                Settings.data.hasResourceChanged = true;
                break;
            }
        }
        return Settings.data.hasResourceChanged;
    }

}
