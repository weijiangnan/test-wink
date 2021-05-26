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

package com.immomo.litebuild.helper;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension;
import com.android.utils.FileUtils;
import com.immomo.litebuild.ModuleConfigs;
import com.immomo.litebuild.Settings;
import com.immomo.litebuild.util.AndroidManifestUtils;

import org.apache.http.util.TextUtils;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency;
import org.gradle.api.internal.project.DefaultProject;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

public class InitEnvHelper {

    static Properties sProperties = null;
    Project project;

    public Properties getPropertiesEnv() {
        if (null != sProperties) {
            return sProperties;
        }

        sProperties = new Properties();
        try {
            File file = new File(Settings.Data.TMP_PATH + "/env.properties");
            if (!file.exists()) {
                file.createNewFile();
            }

            FileInputStream iFile = new FileInputStream(file);
            sProperties.load(iFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sProperties;
    }

    // from retrolambda
    public String getJavaHome() {
        String javaHomeProp = System.getProperty("java.home");
        if (javaHomeProp != null && !javaHomeProp.equals("")) {
            int jreIndex = javaHomeProp.lastIndexOf("${File.separator}jre");
            if (jreIndex != -1) {
                return javaHomeProp.substring(0, jreIndex);
            } else {
                List<String> rets = new ArrayList<>();
                rets.toArray();
                Set<Integer> has = new HashSet<>();

                String str = "";
                new String(str.toCharArray());

                return javaHomeProp;
            }
        } else {
            return System.getenv("JAVA_HOME");
        }
    }

    public void initEnv(Project project) {
        this.project = project;
        Properties properties = getPropertiesEnv();

        System.out.println("-------------------------");

        AppExtension androidExt = (AppExtension) project.getExtensions().getByName("android");
        properties.setProperty("java_home", getJavaHome());
        properties.setProperty("root_dir", project.getPath());

        Settings.getData().APP_PROJECT_DIR = project.getProjectDir().getAbsolutePath();
        Settings.getData().TMP_PATH = project.getRootProject().getProjectDir().getAbsolutePath() + "/.idea/litebuild";

        properties.setProperty("java_home", getJavaHome());
        properties.setProperty("sdk_dir", androidExt.getSdkDirectory().getPath());
        properties.setProperty("build_tools_version", androidExt.getBuildToolsVersion());
        properties.setProperty("build_tools_dir", FileUtils.join(androidExt.getSdkDirectory().getPath(), "build-tools", properties.getProperty("build_tools_version")));
        properties.setProperty("compile_sdk_version", androidExt.getCompileSdkVersion());
        properties.setProperty("compile_sdk_dir", FileUtils.join(properties.getProperty("sdk_dir"), "platforms", properties.getProperty("compile_sdk_version")));

        String packageName = androidExt.getDefaultConfig().getApplicationId();
        String packageNameStuff = androidExt.getDefaultConfig().getApplicationId();
        Settings.getData().PACKAGE_NAME = packageName;
        System.out.println("获取包名 包名 包名 " + packageName + "， 后缀：" + packageNameStuff);
        while (androidExt.getApplicationVariants().iterator().hasNext()) {
            ApplicationVariant variant = androidExt.getApplicationVariants().iterator().next();
            if (variant.getName().equals("debug")) {
                packageNameStuff = variant.getApplicationId();
                break;
            }
        }
//        String applicationIdSuffix = ((BaseAppModuleExtension) androidExt).publicExtensionImpl.buildTypes.getByName("debug").getApplicationIdSuffix();
//        if (!TextUtils.isEmpty(applicationIdSuffix)) {
//            packageName += applicationIdSuffix;
//        }
        properties.setProperty("debug_package", packageNameStuff);

        String manifestPath = androidExt.getSourceSets().getByName("main").getManifest().getSrcFile().getPath();
//        System.out.println("manifestPath : " + manifestPath);

        String launcherActivity = AndroidManifestUtils.findLauncherActivity(manifestPath, packageName);
//        System.out.println("launcherActivity : " + launcherActivity);
        properties.setProperty("launcher_activity", launcherActivity);

        Settings.getData().projectBuildSortList = new ArrayList<>();

        long findModuleTreeStartTime = System.currentTimeMillis();
        findModuleTree(project, "");
        System.out.println("ywb 11111111111111 findModuleTree 耗时：" + (System.currentTimeMillis() - findModuleTreeStartTime) + " ms");
        long findModuleEndTime = System.currentTimeMillis();

        for (Settings.Data.ProjectInfo info : Settings.getData().projectBuildSortList) {
            initProjectData(info.getProject(), androidExt, properties);
        }
        long initProjectDataEndTime = System.currentTimeMillis();
        System.out.println("ywb 11111111111111 initProjectData 耗时：" + (System.currentTimeMillis() - findModuleEndTime) + " ms");

        try {
            FileOutputStream oFile = new FileOutputStream(Settings.Data.TMP_PATH + "/env.properties", false);
            properties.store(oFile, "Auto create by litebuild.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("ywb 11111111111111 findModuleTree 耗时：" + (System.currentTimeMillis() - initProjectDataEndTime) + " ms");
    }

    private void initProjectData(Project project, AppExtension androidExt, Properties properties) {
        long findModuleEndTime = System.currentTimeMillis();

        Iterator<ApplicationVariant> itApp = androidExt.getApplicationVariants().iterator();

        properties.setProperty(project.getName() + "_project_name", project.getName());
        properties.setProperty(project.getName() + "_project_dir", project.getPath());
        properties.setProperty(project.getName() + "_build_dir", project.getBuildDir().getPath());
        properties.setProperty(project.getName() + "_manifest_path", androidExt.getSourceSets().getByName("main").getManifest().getSrcFile().getPath());

        ArrayList<String> args = new ArrayList<>();
        ArrayList<String> kotlinArgs = new ArrayList<>();

        System.out.println("ywb 2222222 initProjectData 1111 耗时：" + (System.currentTimeMillis() - findModuleEndTime) + " ms");

        while (itApp.hasNext()) {
            long findModuleEndTime2 = System.currentTimeMillis();
            ApplicationVariant variant = itApp.next();
            if (!variant.getName().equals("debug")) {
                continue;
            }

            JavaCompile javaCompile = variant.getJavaCompileProvider().get();

            args.add("-source");
            args.add(javaCompile.getTargetCompatibility());

            args.add("-target");
            args.add(javaCompile.getTargetCompatibility());

            args.add("-encoding");
            args.add(javaCompile.getOptions().getEncoding());

            args.add("-bootclasspath");
            args.add(javaCompile.getOptions().getBootstrapClasspath().getAsPath());

            args.add("-g");

//            args.add("-sourcepath");
//            args.add("");

            args.add("-processorpath");
            args.add(javaCompile.getOptions().getAnnotationProcessorPath().getAsPath());

            args.add("-classpath");
            args.add(javaCompile.getClasspath().getAsPath() + ":"
                    + project.getProjectDir().toString() + "/build/intermediates/javac/debug/classes");

            args.add("-d");
            args.add(Settings.Data.TMP_PATH + "/tmp_class");

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.size(); i++) {
                sb.append(" ");
                sb.append(args.get(i));
            }

            properties.setProperty(project.getName() + "_javac_args", sb.toString());

            // 路径可能包含空格，动态控制
//            kotlinArgs.add("-jdk-home");
//            kotlinArgs.add(getJavaHome());

            kotlinArgs.add("-classpath");
//            System.out.println("=============");
//            System.out.println("BootstrapClasspath =========== : " + javaCompile.getOptions().getBootstrapClasspath().getAsPath());
//            System.out.println("=============");

            System.out.println("projectDir : " + project.getProjectDir().toString());
            kotlinArgs.add(javaCompile.getOptions().getBootstrapClasspath().getAsPath() + ":"
                    + javaCompile.getClasspath().getAsPath()
                    + ":" + project.getProjectDir().toString() + "/build/intermediates/javac/debug/classes");

            kotlinArgs.add("-jvm-target");
            kotlinArgs.add(javaCompile.getTargetCompatibility());

            kotlinArgs.add("-d");
            kotlinArgs.add(Settings.Data.TMP_PATH + "/tmp_class");

            StringBuilder sbKotlin = new StringBuilder();
            for (int i = 0; i < kotlinArgs.size(); i++) {
                sbKotlin.append(" ");
                sbKotlin.append(kotlinArgs.get(i));
            }
            properties.setProperty(project.getName() + "_kotlinc_args", sbKotlin.toString());
            System.out.println("ywb 2222222 initProjectData 22222 耗时：" + (System.currentTimeMillis() - findModuleEndTime2) + " ms");

        }
    }

    private void findModuleTree(Project project, String productFlavor) {
        Settings.getData().projectTreeRoot = new Settings.Data.ProjectInfo();
        Settings.getData().projectTreeRoot.setProject(project);

        long startTimeOuter = System.currentTimeMillis();
        project.getRootProject().getSubprojects().forEach(subProject -> {
            if (subProject != null) { // && subProject.getChildProjects().size() == 0) {
                if (subProject.getName().equals("litebuild-gradle-plugin")) {
                    return;
                }
                // build.gradle liteBuildModuleExclude 配置
                ModuleConfigs moduleConfigs = project.getExtensions().getByType(ModuleConfigs.class);
                if (moduleConfigs.modules != null) {
                    System.out.println("===================================");
                    System.out.println("moduleConfigs: " + Arrays.toString(moduleConfigs.modules));
                    System.out.println("===================================");
                    System.out.println("subProject name==" + subProject.getName());
                    if (!Arrays.asList(moduleConfigs.modules).contains(subProject.getName())) {
                        addSubProject(subProject);
                    }
                } else {
                    addSubProject(subProject);
                }
            }
        });
        for (Settings.Data.ProjectInfo projectInfo : Settings.getData().projectBuildSortList) {
            System.out.println("Settings.getData().projectBuildItem : " + projectInfo.getProject().getName());
        }
        System.out.println("ywbbbbbbbbbb " + "project ~~~ " + project.getName() + ": 耗时：" + (System.currentTimeMillis() - startTimeOuter) + " ms");

//        handleAndroidProject(project, Settings.getData().projectTreeRoot, productFlavor, "debug");
//
//        sortBuildList(Settings.getData().projectTreeRoot, Settings.getData().projectBuildSortList);
    }

    private void addSubProject(Project subProject) {
        long startTime = System.currentTimeMillis();
        Settings.Data.ProjectInfo childNode = new Settings.Data.ProjectInfo();
        childNode.setProject(subProject);
        Settings.getData().projectTreeRoot.getChildren().add(childNode);
        Settings.getData().projectBuildSortList.add(childNode);
        System.out.println("ywbbbbbbbbbb " + "subProject " + subProject.getName() + ": 耗时：" + (System.currentTimeMillis() - startTime) + " ms");
    }

    private void sortBuildList(Settings.Data.ProjectInfo node, List<Settings.Data.ProjectInfo> out) {
        for (Settings.Data.ProjectInfo child : node.getChildren()) {
            sortBuildList(child, out);
        }

        out.add(node);
    }

    private void handleAndroidProject(Project project, Settings.Data.ProjectInfo node, String productFlavor, String buildType) {
        String[] compileNames = new String[] {"compile", "implementation", "api", "debugCompile"};
        for (String name : compileNames) {
            Configuration compile = project.getConfigurations().findByName(name);
            if (compile != null) {
                collectLocalDependency(node, compile, productFlavor, buildType);
            }
        }
    }

    private void collectLocalDependency(Settings.Data.ProjectInfo node,
                                               Configuration xxxCompile, String productFlavor, String buildType) {
        xxxCompile.getDependencies().forEach(new Consumer<Dependency>() {
            @Override
            public void accept(Dependency dependency) {
                if (dependency instanceof DefaultProjectDependency) {
                    DefaultProjectDependency dp = (DefaultProjectDependency) dependency;

                    // 孩子节点
                    Settings.Data.ProjectInfo childNode = new Settings.Data.ProjectInfo();
                    childNode.setProject(dp.getDependencyProject());
                    node.getChildren().add(childNode);

                    handleAndroidProject(childNode.getProject(), childNode,  productFlavor, buildType);
                }
            }
        });
    }
}
