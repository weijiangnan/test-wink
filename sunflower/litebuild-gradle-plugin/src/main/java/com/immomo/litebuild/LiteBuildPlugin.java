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
import com.immomo.litebuild.helper.CompileHelper;
import com.immomo.litebuild.helper.DiffHelper;
import com.immomo.litebuild.helper.IncrementPatchHelper;
import com.immomo.litebuild.helper.ResourceHelper;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.Iterator;

public class LiteBuildPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().register("litebuild", task -> {
            System.out.println("litebuild apply()------------------------------------------------>>>>> " + "taskStartTime：" + System.currentTimeMillis());
            task.doLast(new Action<Task>() {
                @Override
                public void execute(Task task) {
                    long startTime = System.currentTimeMillis();
                    System.out.println("litebuild execute() =============================================>>>>> " + "task doLast() startTime：" + startTime);
                    System.out.println("插件执行中...1");

                    AppExtension androidExt = (AppExtension) project.getExtensions().getByName("android");
                    Iterator<ApplicationVariant> itApp = androidExt.getApplicationVariants().iterator();
                    System.out.println("插件执行中...2  itApp=" + itApp.hasNext());
                    while (itApp.hasNext()) {
                        ApplicationVariant variant = itApp.next();
                        System.out.println("variant..." + variant.getName());
                        if (variant.getName().equals("debug")) {
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
            new DiffHelper(projectInfo).diff();
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

}