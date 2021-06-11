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

package com.immomo.wink;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.internal.dsl.ProductFlavor;
import com.immomo.wink.helper.ResourceHelper;
import com.immomo.wink.helper.CleanupHelper;
import com.immomo.wink.helper.CompileHelper;
import com.immomo.wink.helper.DiffHelper;
import com.immomo.wink.helper.IncrementPatchHelper;
import com.immomo.wink.helper.InitEnvHelper;
import com.immomo.wink.hilt.HiltTransform;
import com.immomo.wink.util.Log;
import com.immomo.wink.util.Utils;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;

import java.io.File;
import java.util.Set;

public class WinkPlugin implements Plugin<Project> {

    public static final String GROUP = "momo";
    public boolean isFirstCompile = false;

    @Override
    public void apply(Project project) {
        com.immomo.wink.util.Log.TimerLog timer = com.immomo.wink.util.Log.timerStart("apply init", "_________");
        AppExtension appExtension = (AppExtension) project.getExtensions().getByName("android");
//        appExtension.getDefaultConfig().buildConfigField("String", "wink_VERSION", "20000912");
        appExtension.aaptOptions(aaptOptions -> {
            com.immomo.wink.util.Log.v("aaptOptions", "开始aapt配置 execute!");
            String stableIdPath = project.getRootDir() + "/.idea/" + Constant.TAG + "/stableIds.txt";
            String winkFolder = project.getRootDir() + "/.idea/" + Constant.TAG;
            File file = new File(stableIdPath);
            File lbfolder = new File(winkFolder);
            if (!lbfolder.exists()) {
                lbfolder.mkdir();
            }
            if (file.exists()) {
                com.immomo.wink.util.Log.v("aaptOptions", "开始aapt配置 execute! 文件存在  " + file.getAbsolutePath());
                aaptOptions.additionalParameters("--stable-ids", file.getAbsolutePath());
            } else {
                com.immomo.wink.util.Log.v("aaptOptions", "开始aapt配置 execute! 文件不存在");
                aaptOptions.additionalParameters("--emit-ids", file.getAbsolutePath());
            }
        });

        project.getExtensions().create("winkOptions",
                WinkOptions.class);

        project.afterEvaluate(it -> {
            com.immomo.wink.util.Log.TimerLog timerAfterEvaluate = com.immomo.wink.util.Log.timerStart("timerAfterEvaluate");
            createInitTask(it);
            createDiffTask(it);
            createCompileTask(it);
            //createTransformTask(it);
            createResourcesTask(it);
            createWinkBuildTask(it);
            createCleanupTask(it);

            combineTask(it);

            timerAfterEvaluate.end();

            timer.end("_________");
        });

        if (!project.getGroup().equals("wink")) {
            project.getDependencies().add("debugImplementation",
                    project.getDependencies().create("com.immomo.wink:patch-lib:0.1.61i"));
        }
    }

    public void combineTask(Project project) {
        System.out.println("执行了我们插件");
        Task taskInit = project.getTasks().getByName("winkInit");
        Task taskDiff = project.getTasks().getByName("winkDiff");
        Task taskCompile = project.getTasks().getByName("winkCompile");

        Task taskProcessResources = project.getTasks().getByName("winkProcessResources");
        Task taskResources = project.getTasks().getByName("winkResources");
        Task taskGradleProcessDebugResources = getFlavorProcessDebugResources(project);
        Task taskWink = project.getTasks().getByName("wink");
        Task taskPackageResources = project.getTasks().getByName("winkPackageResources");

        Task cleanUp = project.getTasks().getByName("winkCleanup");
        Task clean = project.getTasks().getByName("clean");

        Task packageDebug = getFlavorTask(project, "package", "Debug");
        packageDebug.doLast(task -> afterFullBuild(project));

        getFlavorPreDebugBuild(project)
                .doFirst(task -> {
                    com.immomo.wink.Settings.data.newVersion = System.currentTimeMillis() + "";
                    ((AppExtension) project.getExtensions().getByName("android"))
                            .getDefaultConfig().buildConfigField("String",
                            "WINK_VERSION", "\"" + com.immomo.wink.Settings.data.newVersion + "\"");
                });

        clean.dependsOn(cleanUp);
        cleanUp.dependsOn(taskInit);

        boolean isStableFileExist = com.immomo.wink.util.Utils.isStableFileExist(project);
        isFirstCompile = !isStableFileExist;

        if (isStableFileExist) {
            com.immomo.wink.util.Log.cyan("【WinkPlugin】", "=========== 开始增量编译 ===========");
            taskDiff.dependsOn(taskInit);
            taskCompile.dependsOn(taskDiff);
            taskWink.dependsOn(taskCompile);
            taskWink.dependsOn(taskResources);
            taskWink.dependsOn(taskPackageResources);
            taskPackageResources.dependsOn(taskResources);
            taskPackageResources.dependsOn(taskProcessResources);
            taskProcessResources.mustRunAfter(taskResources);
            taskProcessResources.dependsOn(taskGradleProcessDebugResources);
        } else {
            com.immomo.wink.util.Log.cyan("【WinkPlugin】", "=========== 本地项目没有编译过，自动编译项目 ===========");
            Task installDebug = getFlavorInstallDebug(project);
            taskWink.dependsOn(installDebug);
        }
    }

    private Task getFlavorProcessDebugResources(Project project){
        AppExtension appExtension = (AppExtension) project.getExtensions().getByName("android");
        NamedDomainObjectContainer<ProductFlavor> flavors = appExtension.getProductFlavors();
        if(flavors!=null && flavors.getNames().size()>0){
            Set<String> flavorNames = flavors.getNames();
            for(String name:flavorNames){
                String processDebugResources = "process"+ com.immomo.wink.util.Utils.upperCaseFirst(name)+"DebugResources";
                try {
                    Task targetTask = project.getTasks().getByName(processDebugResources);
                    return targetTask;
                }catch (UnknownTaskException e){
                    e.printStackTrace();
                }
            }
        }

        return project.getTasks().getByName("processDebugResources");
    }

    private Task getFlavorPreDebugBuild(Project project){
        //preDebugBuild
        AppExtension appExtension = (AppExtension) project.getExtensions().getByName("android");
        NamedDomainObjectContainer<ProductFlavor> flavors = appExtension.getProductFlavors();
        if(flavors!=null && flavors.getNames().size()>0){
            Set<String> flavorNames = flavors.getNames();
            for(String name:flavorNames){
                String processDebugResources = "pre"+ com.immomo.wink.util.Utils.upperCaseFirst(name)+"DebugBuild";
                try {
                    Task targetTask = project.getTasks().getByName(processDebugResources);
                    return targetTask;
                }catch (UnknownTaskException e){
                    e.printStackTrace();
                }
            }
        }

        return project.getTasks().getByName("preDebugBuild");
    }

    private Task getFlavorInstallDebug(Project project) {
        //preDebugBuild
        AppExtension appExtension = (AppExtension) project.getExtensions().getByName("android");
        NamedDomainObjectContainer<ProductFlavor> flavors = appExtension.getProductFlavors();
        if (flavors != null && flavors.getNames().size() > 0) {
            Set<String> flavorNames = flavors.getNames();
            for (String name : flavorNames) {
                String processDebugResources = "install" + com.immomo.wink.util.Utils.upperCaseFirst(name) + "Debug";
                try {
                    Task targetTask = project.getTasks().getByName(processDebugResources);
                    return targetTask;
                } catch (UnknownTaskException e) {
                    e.printStackTrace();
                }
            }
        }

        return project.getTasks().getByName("installDebug");
    }

    private Task getFlavorTask(Project project, String pre, String post){
        //preDebugBuild
        AppExtension appExtension = (AppExtension) project.getExtensions().getByName("android");
        NamedDomainObjectContainer<ProductFlavor> flavors = appExtension.getProductFlavors();
        if(flavors!=null && flavors.getNames().size()>0){
            Set<String> flavorNames = flavors.getNames();
            for(String name:flavorNames){
                String processDebugResources = pre + Utils.upperCaseFirst(name) + post;
                try {
                    Task targetTask = project.getTasks().getByName(processDebugResources);
                    return targetTask;
                }catch (UnknownTaskException e){
                    e.printStackTrace();
                }
            }
        }

        return project.getTasks().getByName(pre + post);
    }

    public void createInitTask(Project project) {
        project.getTasks().register("winkInit", task -> {
            task.doLast(it -> {
                // init
                new com.immomo.wink.helper.InitEnvHelper().initEnv(project, false);
            });

        }).get().setGroup(com.immomo.wink.Settings.NAME);
    }

    public void createCompileTask(Project project) {
        project.getTasks().register("winkCompile", task -> {
            task.doLast(it -> {
                com.immomo.wink.util.Log.TimerLog timer = com.immomo.wink.util.Log.timerStart("winkCompile");
                new CompileHelper().compileCode();
                timer.end();
            });
        }).get().setGroup(com.immomo.wink.Settings.NAME);
    }

    public void createTransformTask(Project project) {
        project.getTasks().register("winkTransform", task -> {
            task.doLast(it -> {
                com.immomo.wink.util.Log.TimerLog timer = com.immomo.wink.util.Log.timerStart("winkCompile");
                HiltTransform.INSTANCE.transform();
                timer.end();
            });
        }).get().setGroup(com.immomo.wink.Settings.NAME);
//        BaseExtension androidExtension = project.getExtensions().findByType(BaseExtension.class);
//        androidExtension.registerTransform(new AndroidEntryPointTransform());
    }

    public void createResourcesTask(Project project) {
        project.getTasks().register("winkProcessResources", task -> {
        }).get().setGroup(com.immomo.wink.Settings.NAME);

        project.getTasks().getByName("winkProcessResources").setOnlyIf(it2 -> {
            return com.immomo.wink.Settings.data.hasResourceChanged;
        });

        project.getTasks().register("winkResources", task -> {
            task.doLast(it -> {
                com.immomo.wink.util.Log.TimerLog timer = com.immomo.wink.util.Log.timerStart("winkResources");
                // compile resource.
                new ResourceHelper().checkResource();
                timer.end();
            });
        }).get().setGroup(com.immomo.wink.Settings.NAME);

        project.getTasks().register("winkPackageResources", task -> {
            task.doLast(it -> {
                com.immomo.wink.util.Log.TimerLog timer = com.immomo.wink.util.Log.timerStart("winkPackageResources");
                if (com.immomo.wink.Settings.data.hasResourceChanged) {
                    new ResourceHelper().packageResources();
                }

                timer.end();
            });
        }).get().setGroup(com.immomo.wink.Settings.NAME);
    }

    public void createWinkBuildTask(Project project) {
        project.getTasks().register("wink", task -> {
            task.doLast(new Action<Task>() {
                @Override
                public void execute(Task task) {
                    if (isFirstCompile) {
                        return;
                    }
                    com.immomo.wink.util.Log.TimerLog timer = com.immomo.wink.util.Log.timerStart("wink", "patchToApp...");
                    // patch
                    if (new IncrementPatchHelper().patchToApp()) {
                        updateSnapShot();
                    }
                    timer.end("patchToApp...");
                }
            });
        }).get().setGroup(com.immomo.wink.Settings.NAME);
    }

    public void createCleanupTask(Project project) {
        project.getTasks().register("winkCleanup", task -> {
            task.doLast(new Action<Task>() {
                @Override
                public void execute(Task task) {
                    com.immomo.wink.util.Log.TimerLog timer = Log.timerStart("winkCleanup", "cleanUp");
                    new com.immomo.wink.helper.CleanupHelper().cleanup();
                    timer.end("cleanUp");
                }
            });
        }).get().setGroup(com.immomo.wink.Settings.NAME);
    }

    public void createDiffTask(Project project) {
        project.getTasks().register("winkDiff", task -> {

            task.doLast(it2 -> {
                long diffStartTime = System.currentTimeMillis();

                for (com.immomo.wink.Settings.ProjectTmpInfo projectInfo : com.immomo.wink.Settings.data.projectBuildSortList) {
                    //
                    long startTime = System.currentTimeMillis();
                    new com.immomo.wink.helper.DiffHelper(projectInfo).diff(projectInfo);
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

                System.out.println("【【【===================================================>>>>>> " + "diff 耗时：" + (System.currentTimeMillis() - diffStartTime) + " ms");
            });

        }).get().setGroup(com.immomo.wink.Settings.NAME);
    }

    private void afterFullBuild(Project project) {
        // 清理
        new CleanupHelper().cleanOnAssemble();
        // 初始化
        new InitEnvHelper().initEnv(project, true);
        // 产生快照
        for (com.immomo.wink.Settings.ProjectTmpInfo info : com.immomo.wink.Settings.data.projectBuildSortList) {
            new com.immomo.wink.helper.DiffHelper(info).initSnapshot();
        }
    }

    private void updateSnapShot() {
        for (com.immomo.wink.Settings.ProjectTmpInfo info : Settings.data.projectBuildSortList) {
            if (info.changedJavaFiles.size() > 0 || info.changedKotlinFiles.size() > 0) {
                new com.immomo.wink.helper.DiffHelper(info).initSnapshotForCode();
            }

            if (info.hasResourceChanged) {
                new DiffHelper(info).initSnapshotForRes();
            }
        }
    }
}