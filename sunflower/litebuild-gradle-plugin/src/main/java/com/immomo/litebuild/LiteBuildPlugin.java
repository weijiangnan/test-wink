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
import com.immomo.litebuild.helper.CompileHelper;
import com.immomo.litebuild.helper.DiffHelper;
import com.immomo.litebuild.helper.IncrementPatchHelper;
import com.immomo.litebuild.helper.ResourceHelper;

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

        addAssembleLastTask(project);

        project.getTasks().register("litebuild", task -> {
            task.setGroup("momo");
            System.out.println("litebuild apply()------------------------------------------------>>>>> " + "taskStartTime：" + System.currentTimeMillis());
            task.doLast(new Action<Task>() {
                @Override
                public void execute(Task task) {
                    long startTime = System.currentTimeMillis();
                    System.out.println("litebuild execute() =============================================>>>>> " + "task doLast() startTime：" + startTime);
                    System.out.println("插件执行中...1");

                    boolean hasAppPlugin = project.getPlugins().hasPlugin("com.android.application");
                    if (!hasAppPlugin) {
                        System.out.println("该module未包含com.android.application插件");
                        return;
                    }

                    AppExtension androidExt = (AppExtension) project.getExtensions().getByName("android");

                    Iterator<ApplicationVariant> itApp = androidExt.getApplicationVariants().iterator();
                    System.out.println("插件执行中...2  itApp=" + itApp.hasNext());
                    while (itApp.hasNext()) {
                        ApplicationVariant variant = itApp.next();
                        System.out.println("variant..." + variant.getName());
                        if (variant.getName().equals("debug")) {

                            variant.getOutputs().all(new Action<BaseVariantOutput>() {
                                @Override
                                public void execute(BaseVariantOutput baseVariantOutput) {
                                    String path = baseVariantOutput.getOutputFile().getAbsolutePath();
                                    System.out.println("variant..." + path);
                                }
                            });

                            System.out.println("插件执行中...3  main()");
                            main(project);
                            break;
                        }
                    }
                    System.out.println("=============================================>>>>> " + "task doLast() endTime：" + (System.currentTimeMillis() - startTime) + " ms");
                }
            });

        });

    }



    public void main(Project project) {
        long mainStartTime = System.currentTimeMillis();
        System.out.println("进入了main函数");
        // init
        Settings.init(project);
        System.out.println("【【【===================================================>>>>> " + "init 耗时：" + (System.currentTimeMillis() - mainStartTime) + " ms");

//        System.out.println("=================>>>>>> projectBuildSortList size : " +  Settings.getData().projectBuildSortList.size() + " === " + Settings.getData().projectBuildSortList.toString());
//        System.out.println("=============================================>>>>>> ");
//        for (Settings.Data.ProjectInfo projectInfo : Settings.getData().projectBuildSortList) {
//            System.out.println("=================>>>>>> " + projectInfo.getProject().getName());
//        }
//        System.out.println("=============================================>>>>>> ");


        long diffStartTime = System.currentTimeMillis();

        for (Settings.Data.ProjectInfo projectInfo : Settings.getData().projectBuildSortList) {
            //
            long startTime = System.currentTimeMillis();
            new DiffHelper(projectInfo.getProject()).diff(projectInfo);
            System.out.println("=================>>>>>> " + projectInfo.getProject().getName() + "结束一组耗时：" + (System.currentTimeMillis() - startTime) + " ms");
            // compile java & kotlin
            new CompileHelper().compileCode(projectInfo);
        }
        for (Settings.Data.ProjectInfo projectInfo : Settings.getData().projectBuildSortList) {
            if (projectInfo.hasResourceChanged) {
                System.out.println("遍历是否有资源修改, name=" + projectInfo.getDir());
                System.out.println("遍历是否有资源修改, changed=" + projectInfo.hasResourceChanged);
                Settings.getData().hasResourceChanged = true;
                break;
            }
        }

        System.out.println("【【【===================================================>>>>>> " + "diff 耗时：" + (System.currentTimeMillis() - diffStartTime) + " ms");

        // compile resource.
        long resStartTime = System.currentTimeMillis();
        new ResourceHelper().process();

        System.out.println("【【【===================================================>>>>> " + "res 耗时" + (System.currentTimeMillis() - resStartTime) + " ms");
        // Increment patch to app.
        new IncrementPatchHelper().patchToApp();
        long pathEndTime = System.currentTimeMillis();
        System.out.println("【【【===================================================>>>>> " + "path 结束耗时" + (System.currentTimeMillis() - pathEndTime) + " ms");
        System.out.println("【【【===================================================>>>>> " + "main 函数结束" + (System.currentTimeMillis() - mainStartTime) + " ms");
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