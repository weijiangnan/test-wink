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

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public class LiteBuildPlugin implements Plugin<Project> {

    public void main(Project project) {
        System.out.println("进入了main函数");
        // init
        Settings.init(project);

        for (Settings.Data.ProjectInfo projectInfo : Settings.getData().projectBuildSortList) {
            //
            new DiffHelper(projectInfo).diff();
            // compile java & kotlin
            new CompileHelper().compileCode(projectInfo);
        }


        // compile resource.
        new ResourceHelper().process();

        // Increment patch to app.
        new IncrementPatchHelper().patchToApp();
    }

    @Override
    public void apply(Project project) {
        project.getTasks().register("litebuild", task -> {

            System.out.println("插件执行中...11");

            AppExtension androidExt = (AppExtension) project.getExtensions().getByName("android");
            Iterator<ApplicationVariant> itApp = androidExt.getApplicationVariants().iterator();
            System.out.println("插件执行中...2  itApp=" + itApp.hasNext());
//            while (itApp.hasNext()) {
//                ApplicationVariant variant = itApp.next();
//                System.out.println("variant..." + variant.getName());
//                if (!variant.getName().equals("debug")) {
//                    
//                }
//            }
            Map<String, Project> allProjectMap = new HashMap<>();
            project.getRootProject().getAllprojects().forEach(new Consumer<Project>() {
                @Override
                public void accept(Project it) {
                    System.out.println("插件执行中...3 accept itApp=" + itApp.hasNext());
                    allProjectMap.put(it.getName(), it);
                }
            });

            main(project);
        });

    }
}