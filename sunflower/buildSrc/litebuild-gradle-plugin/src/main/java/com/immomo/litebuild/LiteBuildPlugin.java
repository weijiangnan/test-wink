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

package com.immomo.litebuild;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.internal.dsl.ProductFlavor;
import com.immomo.litebuild.helper.CleanupHelper;
import com.immomo.litebuild.helper.CompileHelper;
import com.immomo.litebuild.helper.DiffHelper;
import com.immomo.litebuild.helper.IncrementPatchHelper;
import com.immomo.litebuild.helper.InitEnvHelper;
import com.immomo.litebuild.helper.ResourceHelper;
import com.immomo.litebuild.hilt.HiltTransform;
import com.immomo.litebuild.util.Log;
import com.immomo.litebuild.util.Utils;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class LiteBuildPlugin implements Plugin<Project> {

    public static final String GROUP = "momo";
    public boolean isFirstCompile = false;

    @Override
    public void apply(Project project) {
        Log.TimerLog timer = Log.timerStart("apply init", "_________");
        AppExtension appExtension = (AppExtension) project.getExtensions().getByName("android");
//        appExtension.getDefaultConfig().buildConfigField("String", "LITEBUILD_VERSION", "20000912");
        appExtension.aaptOptions(aaptOptions -> {
            Log.v("aaptOptions", "开始aapt配置 execute!");
            String stableIdPath = project.getRootDir() + "/.idea/litebuild/stableIds.txt";
            String litebuildFolder = project.getRootDir() + "/.idea/litebuild";
            File file = new File(stableIdPath);
            File lbfolder = new File(litebuildFolder);
            if (!lbfolder.exists()) {
                lbfolder.mkdir();
            }
            if (file.exists()) {
                Log.v("aaptOptions", "开始aapt配置 execute! 文件存在  " + file.getAbsolutePath());
                aaptOptions.additionalParameters("--stable-ids", file.getAbsolutePath());
            } else {
                Log.v("aaptOptions", "开始aapt配置 execute! 文件不存在");
                aaptOptions.additionalParameters("--emit-ids", file.getAbsolutePath());
            }
        });

        project.getExtensions().create("litebuildOptions",
                LitebuildOptions.class);

        project.afterEvaluate(it -> {
            Log.TimerLog timerAfterEvaluate = Log.timerStart("timerAfterEvaluate");
            createInitTask(it);
            createDiffTask(it);
            createCompileTask(it);
            //createTransformTask(it);
            createResourcesTask(it);
            createLiteBuildTask(it);
            createCleanupTask(it);

            combineTask(it);

            timerAfterEvaluate.end();

            timer.end("_________");
        });

        if (!project.getGroup().equals("sunflower")) {
            project.getDependencies().add("implementation",
                    project.getDependencies().create("com.immomo.litebuild:build-lib:0.1.40"));
        }
    }

    public void combineTask(Project project) {
        System.out.println("执行了我们插件");
        Task taskInit = project.getTasks().getByName("litebuildInit");
        Task taskDiff = project.getTasks().getByName("litebuildDiff");
        Task taskCompile = project.getTasks().getByName("litebuildCompile");

        Task taskProcessResources = project.getTasks().getByName("litebuildProcessResources");
        Task taskResources = project.getTasks().getByName("litebuildResources");
        Task taskGradleProcessDebugResources = getFlavorProcessDebugResources(project);
        Task taskLitebuild = project.getTasks().getByName("litebuild");
        Task taskPackageResources = project.getTasks().getByName("litebuildPackageResources");

        Task cleanUp = project.getTasks().getByName("litebuildCleanup");
        Task clean = project.getTasks().getByName("clean");

        Task assembleDebug = project.getTasks().getByName("assembleDebug");
        assembleDebug.doLast(task -> afterFullBuild(project));

        getFlavorPreDebugBuild(project)
                .doFirst(task -> {
                    Settings.data.newVersion = System.currentTimeMillis() + "";
                    ((AppExtension) project.getExtensions().getByName("android"))
                            .getDefaultConfig().buildConfigField("String",
                            "LITEBUILD_VERSION", "\"" + Settings.data.newVersion + "\"");
                });

        clean.dependsOn(cleanUp);
        cleanUp.dependsOn(taskInit);

        boolean isStableFileExist = Utils.isStableFileExist(project);
        isFirstCompile = !isStableFileExist;

        if (isStableFileExist) {
            Log.cyan("【LiteBuildPlugin】", "=========== 开始增量编译 ===========");
            taskDiff.dependsOn(taskInit);
            taskCompile.dependsOn(taskDiff);
            taskLitebuild.dependsOn(taskCompile);
            taskLitebuild.dependsOn(taskResources);
            taskLitebuild.dependsOn(taskPackageResources);
            taskPackageResources.dependsOn(taskResources);
            taskPackageResources.dependsOn(taskProcessResources);
            taskProcessResources.mustRunAfter(taskResources);
            taskProcessResources.dependsOn(taskGradleProcessDebugResources);
        } else {
            Log.cyan("【LiteBuildPlugin】", "=========== 本地项目没有编译过，自动编译项目 ===========");
            Task installDebug = project.getTasks().getByName("installDebug");
            taskLitebuild.dependsOn(installDebug);
        }
    }

    private Task getFlavorProcessDebugResources(Project project){
        AppExtension appExtension = (AppExtension) project.getExtensions().getByName("android");
        NamedDomainObjectContainer<ProductFlavor> flavors = appExtension.getProductFlavors();
        if(flavors!=null && flavors.getNames().size()>0){
            Set<String> flavorNames = flavors.getNames();
            for(String name:flavorNames){
                String processDebugResources = "process"+Utils.upperCaseFirst(name)+"DebugResources";
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
                String processDebugResources = "pre"+Utils.upperCaseFirst(name)+"DebugBuild";
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


    public void createInitTask(Project project) {
        project.getTasks().register("litebuildInit", task -> {
            task.doLast(it -> {
                // init
                new InitEnvHelper().initEnv(project, false);
            });

        }).get().setGroup(Settings.NAME);
    }

    public void createCompileTask(Project project) {
        project.getTasks().register("litebuildCompile", task -> {
            task.doLast(it -> {
                Log.TimerLog timer = Log.timerStart("litebuildCompile");
                new CompileHelper().compileCode();
                timer.end();
            });
        }).get().setGroup(Settings.NAME);
    }

    public void createTransformTask(Project project) {
        project.getTasks().register("litebuildTransform", task -> {
            task.doLast(it -> {
                Log.TimerLog timer = Log.timerStart("litebuildCompile");
                HiltTransform.INSTANCE.transform();
                timer.end();
            });
        }).get().setGroup(Settings.NAME);
//        BaseExtension androidExtension = project.getExtensions().findByType(BaseExtension.class);
//        androidExtension.registerTransform(new AndroidEntryPointTransform());
    }

    public void createResourcesTask(Project project) {
        project.getTasks().register("litebuildProcessResources", task -> {
        }).get().setGroup(Settings.NAME);

        project.getTasks().getByName("litebuildProcessResources").setOnlyIf(it2 -> {
            return Settings.data.hasResourceChanged;
        });

        project.getTasks().register("litebuildResources", task -> {
            task.doLast(it -> {
                Log.TimerLog timer = Log.timerStart("litebuildResources");
                // compile resource.
                new ResourceHelper().checkResource();
                timer.end();
            });
        }).get().setGroup(Settings.NAME);

        project.getTasks().register("litebuildPackageResources", task -> {
            task.doLast(it -> {
                Log.TimerLog timer = Log.timerStart("litebuildPackageResources");
                if (Settings.data.hasResourceChanged) {
                    new ResourceHelper().packageResources();
                }

                timer.end();
            });
        }).get().setGroup(Settings.NAME);
    }

    public void createLiteBuildTask(Project project) {
        project.getTasks().register("litebuild", task -> {
            task.doLast(new Action<Task>() {
                @Override
                public void execute(Task task) {
                    if (isFirstCompile) {
                        return;
                    }
                    Log.TimerLog timer = Log.timerStart("litebuild", "patchToApp...");
                    // patch
                    if (new IncrementPatchHelper().patchToApp()) {
                        updateSnapShot();
                    }
                    timer.end("patchToApp...");
                }
            });
        }).get().setGroup(Settings.NAME);
    }

    public void createCleanupTask(Project project) {
        project.getTasks().register("litebuildCleanup", task -> {
            task.doLast(new Action<Task>() {
                @Override
                public void execute(Task task) {
                    Log.TimerLog timer = Log.timerStart("litebuildCleanup", "cleanUp");
                    new CleanupHelper().cleanup();
                    timer.end("cleanUp");
                }
            });
        }).get().setGroup(Settings.NAME);
    }

    public void createDiffTask(Project project) {
        project.getTasks().register("litebuildDiff", task -> {

            task.doLast(it2 -> {
                long diffStartTime = System.currentTimeMillis();

                for (Settings.ProjectTmpInfo projectInfo : Settings.data.projectBuildSortList) {
                    //
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

                System.out.println("【【【===================================================>>>>>> " + "diff 耗时：" + (System.currentTimeMillis() - diffStartTime) + " ms");
            });

        }).get().setGroup(Settings.NAME);
    }

    private void afterFullBuild(Project project) {
        // 清理
        new CleanupHelper().cleanOnAssemble();
        // 初始化
        new InitEnvHelper().initEnv(project, true);
        // 产生快照
        for (Settings.ProjectTmpInfo info : Settings.data.projectBuildSortList) {
            new DiffHelper(info).initSnapshot();
        }
    }

    private void updateSnapShot() {
        for (Settings.ProjectTmpInfo info : Settings.data.projectBuildSortList) {
            if (info.changedJavaFiles.size() > 0 || info.changedKotlinFiles.size() > 0) {
                new DiffHelper(info).initSnapshotForCode();
            }

            if (info.hasResourceChanged) {
                new DiffHelper(info).initSnapshotForRes();
            }
        }
    }
}