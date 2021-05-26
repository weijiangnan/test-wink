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
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.BaseVariantOutput;
import com.immomo.litebuild.helper.CleanupHelper;
import com.immomo.litebuild.helper.CompileHelper;
import com.immomo.litebuild.helper.DiffHelper;
import com.immomo.litebuild.helper.IncrementPatchHelper;
import com.immomo.litebuild.helper.ResourceHelper;
import com.immomo.litebuild.util.Log;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionGraph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.function.Consumer;

public class LiteBuildPlugin implements Plugin<Project> {

    public static final String GROUP = "momo";

    @Override
    public void apply(Project project) {
        Log.TimerLog timer = Log.timerStart("apply init", "_________");

        addAssembleLastTask(project);

        project.getExtensions().create("litebuildOptions",
                LitebuildOptions.class);

        project.afterEvaluate(it -> {
            Log.TimerLog timerAfterEvaluate = Log.timerStart("timerAfterEvaluate");
            createInitTask(it);
            createDiffTask(it);
            createCompileTask(it);
            createResourcesTask(it);
            createLiteBuildTask(it);
            createCleanupTask(it);

            combineTask(it);

            timerAfterEvaluate.end();

            timer.end("_________");
        });
        // implementation project(":pipline")
        project.getDependencies().add("implementation",
                project.getDependencies().create("com.immomo.litebuild:build-lib:0.0.62-SNAPSHOT"));
    }

    public void combineTask(Project project) {
        System.out.println("执行了我们插件");
        Task taskInit = project.getTasks().getByName("litebuildInit");
        Task taskDiff = project.getTasks().getByName("litebuildDiff");
        Task taskCompile = project.getTasks().getByName("litebuildCompile");

        Task taskProcessResources = project.getTasks().getByName("litebuildProcessResources");
        Task taskResources = project.getTasks().getByName("litebuildResources");
        Task taskGradleProcessDebugResources = project.getTasks().getByName("processDebugResources");
        Task taskLitebuild = project.getTasks().getByName("litebuild");
        Task taskPackageResources = project.getTasks().getByName("litebuildPackageResources");

        Task cleanUp = project.getTasks().getByName("litebuildCleanup");
        Task clean = project.getTasks().getByName("clean");

//        cleanUp.dependsOn(clean);
        clean.dependsOn(cleanUp);
        cleanUp.dependsOn(taskInit);

        taskDiff.dependsOn(taskInit);
        taskCompile.dependsOn(taskDiff);
        taskLitebuild.dependsOn(taskCompile);
        taskLitebuild.dependsOn(taskResources);
        taskLitebuild.dependsOn(taskPackageResources);

        taskPackageResources.dependsOn(taskResources);
        taskPackageResources.dependsOn(taskProcessResources);
        taskProcessResources.mustRunAfter(taskResources);
        taskProcessResources.dependsOn(taskGradleProcessDebugResources);
    }

    public void createInitTask(Project project) {
        project.getTasks().register("litebuildInit", task -> {
            task.doLast(it -> {
                Log.TimerLog timer = Log.timerStart("litebuildInit");
                // init
                Settings.init(project);
                timer.end();
            });

        }).get().setGroup(Settings.getData().NAME);
    }

    public void createCompileTask(Project project) {
        project.getTasks().register("litebuildCompile", task -> {
            task.doLast(it -> {
                Log.TimerLog timer = Log.timerStart("litebuildCompile");
                new CompileHelper().compileCode();
                timer.end();
            });
        }).get().setGroup(Settings.getData().NAME);
    }

    public void createResourcesTask(Project project) {
        project.getTasks().register("litebuildProcessResources", task -> {
        }).get().setGroup(Settings.getData().NAME);

        project.getTasks().getByName("litebuildProcessResources").setOnlyIf(it2 -> {
            return Settings.getData().hasResourceChanged;
        });

        project.getTasks().register("litebuildResources", task -> {
            task.doLast(it -> {
                Log.TimerLog timer = Log.timerStart("litebuildResources");
                // compile resource.
                new ResourceHelper().checkResource();
                timer.end();
            });
        }).get().setGroup(Settings.getData().NAME);

        project.getTasks().register("litebuildPackageResources", task -> {
            task.doLast(it -> {
                Log.TimerLog timer = Log.timerStart("litebuildPackageResources");
                if (Settings.getData().hasResourceChanged) {
                    new ResourceHelper().packageResources();
                }

                timer.end();
            });
        }).get().setGroup(Settings.getData().NAME);
    }

    public void createLiteBuildTask(Project project) {
        project.getTasks().register("litebuild", task -> {
            task.doLast(new Action<Task>() {
                @Override
                public void execute(Task task) {
                    Log.TimerLog timer = Log.timerStart("litebuild", "patchToApp...");
                    // patch
                    new IncrementPatchHelper().patchToApp();
                    timer.end("patchToApp...");
                }
            });
        }).get().setGroup(Settings.getData().NAME);
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
        }).get().setGroup(Settings.getData().NAME);
    }

    public void createDiffTask(Project project) {
        project.getTasks().register("litebuildDiff", task -> {

            task.doLast(it2-> {
                long diffStartTime = System.currentTimeMillis();

                for (Settings.Data.ProjectInfo projectInfo : Settings.getData().projectBuildSortList) {
                    //
                    long startTime = System.currentTimeMillis();
                    new DiffHelper(projectInfo.getProject()).diff(projectInfo);
                System.out.println("=================>>>>>> " + projectInfo.getProject().getName() + "结束一组耗时：" + (System.currentTimeMillis() - startTime) + " ms");
                }
//
            for (Settings.Data.ProjectInfo projectInfo : Settings.getData().projectBuildSortList) {
                if (projectInfo.hasResourceChanged) {
                    System.out.println("遍历是否有资源修改, name=" + projectInfo.getDir());
                    System.out.println("遍历是否有资源修改, changed=" + projectInfo.hasResourceChanged);
                    Settings.getData().hasResourceChanged = true;
                    break;
                }
            }

                System.out.println("【【【===================================================>>>>>> " + "diff 耗时：" + (System.currentTimeMillis() - diffStartTime) + " ms");
            });

        }).get().setGroup(Settings.getData().NAME);
    }

    private void addAssembleLastTask(Project project) {
        project.getRootProject().getAllprojects().forEach(new Consumer<Project>() {
            @Override
            public void accept(Project it) {


                it.getGradle().getTaskGraph().whenReady(new Action<TaskExecutionGraph>() {
                    @Override
                    public void execute(TaskExecutionGraph taskExecutionGraph) {
                        taskExecutionGraph.getAllTasks().forEach(new Consumer<Task>() {
                            @Override
                            public void accept(Task task) {
                                if (task.getName().toLowerCase().contains("assemble") || task.getName().toLowerCase().contains("install")) {
                                    task.doLast(new Action<Task>() {
                                        @Override
                                        public void execute(Task task) {
                                            new DiffHelper(it).initSnapshot();

                                            //copy apk to litebuild dir
                                            boolean hasAppPlugin = it.getPlugins().hasPlugin("com.android.application");
                                            if (hasAppPlugin) {
                                                System.out.println("该module未包含com.android.application插件");
                                                AppExtension androidExt = (AppExtension) project.getExtensions().getByName("android");
                                                Iterator<ApplicationVariant> itApp = androidExt.getApplicationVariants().iterator();
                                                while (itApp.hasNext()) {
                                                    ApplicationVariant variant = itApp.next();
                                                    if (variant.getName().equals("debug")) {
                                                        variant.getOutputs().all(new Action<BaseVariantOutput>() {
                                                            @Override
                                                            public void execute(BaseVariantOutput baseVariantOutput) {
                                                                File srcFile = baseVariantOutput.getOutputFile();
                                                                String moduleName = it.getPath().replace(":", "");
                                                                String diffDir = project.getRootDir() + "/.idea/litebuild/diff/" + moduleName;
                                                                File destFile = new File(diffDir, "snapshot.apk");
                                                                if (destFile.exists()) {
                                                                    destFile.delete();
                                                                }
                                                                try {
                                                                    Files.copy(srcFile.toPath(), destFile.toPath());
                                                                } catch (IOException e) {
                                                                    e.printStackTrace();
                                                                }
                                                                System.out.println("momomomomomomomomomomomomomo Output path=" + srcFile);
                                                            }
                                                        });

                                                        //
                                                        String outputApkPath = variant.getPackageApplicationProvider().get().getOutputDirectory().get().toString();
                                                        System.out.println("momomomomomomomomomomomomomo outputApkPath=" + outputApkPath);
                                                    }
                                                }
                                            }

                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }
        });
    }
}