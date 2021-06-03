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
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.LibraryVariant;
import com.android.utils.FileUtils;
import com.immomo.litebuild.LitebuildOptions;
import com.immomo.litebuild.Settings;
import com.immomo.litebuild.util.AndroidManifestUtils;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class InitEnvHelper {
    Project project;
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

    public void initEnv(Project project, boolean reload) {
        if (reload) {
            createEnv(project);
        } else {
//            reloadEnv(project);
            Settings.restoreEnv(project.getRootDir()
                    + "/.idea/" + Settings.NAME + "/env");
        }

        // Data每次初始化
        Settings.initData();

//        Log.v(Constant.TAG, Settings.env.toString());
    }

    protected void createEnv(Project project) {
        this.project = project;

        AppExtension androidExt = (AppExtension) project.getExtensions().getByName("android");

        Settings.Env env = Settings.env;
        env.javaHome = getJavaHome();
        env.sdkDir = androidExt.getSdkDirectory().getPath();
        env.buildToolsVersion = androidExt.getBuildToolsVersion();
        env.buildToolsDir = FileUtils.join(androidExt.getSdkDirectory().getPath(),
                "build-tools", env.buildToolsVersion);
        env.compileSdkVersion = androidExt.getCompileSdkVersion();
        env.compileSdkDir = FileUtils.join(env.sdkDir, "platforms", env.compileSdkVersion);

        env.rootDir = project.getRootDir().getAbsolutePath();

        if (!Settings.data.newVersion.isEmpty()) {
            env.version = Settings.data.newVersion;
            Settings.data.newVersion = "";
        }

        env.appProjectDir = project.getProjectDir().getAbsolutePath();
        env.tmpPath = project.getRootProject().getProjectDir().getAbsolutePath() + "/.idea/" + Settings.NAME;

        env.packageName = androidExt.getDefaultConfig().getApplicationId();
        while (androidExt.getApplicationVariants().iterator().hasNext()) {
            ApplicationVariant variant = androidExt.getApplicationVariants().iterator().next();
            if (variant.getName().equals("debug")) {
                env.debugPackageName = variant.getApplicationId();
                break;
            }
        }

        String manifestPath = androidExt.getSourceSets().getByName("main").getManifest().getSrcFile().getPath();
        env.launcherActivity = AndroidManifestUtils.findLauncherActivity(manifestPath, env.packageName);

        LitebuildOptions options = project.getExtensions().getByType(LitebuildOptions.class);
        env.options = new LitebuildOptions();
        env.options.moduleBlacklist = options.moduleBlacklist;
        env.options.moduleWhitelist = options.moduleWhitelist;
        env.options.kotlinSyntheticsEnable = options.kotlinSyntheticsEnable;

        findModuleTree(project, "");

        Settings.storeEnv(env, project.getRootDir() + "/.idea/" + Settings.NAME + "/env");
    }

    private void initProjectData(Settings.ProjectFixedInfo fixedInfo, Project project) {
        long findModuleEndTime = System.currentTimeMillis();
        fixedInfo.name = project.getName();
        fixedInfo.isProjectIgnore = isIgnoreProject(fixedInfo.name);
        if (fixedInfo.isProjectIgnore) {
            return;
        }

        fixedInfo.dir = project.getProjectDir().getAbsolutePath();
        fixedInfo.buildDir = project.getBuildDir().getPath();

        ArrayList<String> args = new ArrayList<>();
        ArrayList<String> kotlinArgs = new ArrayList<>();

        System.out.println("ywb 2222222 initProjectData 1111 耗时：" + (System.currentTimeMillis() - findModuleEndTime) + " ms");

        Object extension = project.getExtensions().getByName("android");
        JavaCompile javaCompile = null;
        if (extension instanceof AppExtension) {
            Iterator<ApplicationVariant> itApp = ((AppExtension) extension).getApplicationVariants().iterator();
            while (itApp.hasNext()) {
                ApplicationVariant variant = itApp.next();
                if (variant.getName().equals("debug")) {
                    javaCompile = variant.getJavaCompileProvider().get();
                    break;
                }
            }
        } else if (extension instanceof LibraryExtension) {
            Iterator<LibraryVariant> it = ((LibraryExtension) extension).getLibraryVariants().iterator();
            while (it.hasNext()) {
                LibraryVariant variant = it.next();
                if (variant.getName().equals("debug")) {
                    javaCompile = variant.getJavaCompileProvider().get();
                    break;
                }
            }
        }

        if (javaCompile == null) {
            return;
        }

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

        String processorpath = javaCompile.getOptions().getAnnotationProcessorPath().getAsPath();
        if (!processorpath.trim().isEmpty()) {
            args.add("-processorpath");
            args.add(processorpath);
        }

        args.add("-classpath");
        args.add(javaCompile.getClasspath().getAsPath() + ":"
                + project.getProjectDir().toString() + "/build/intermediates/javac/debug/classes");

        args.add("-d");
        args.add(Settings.env.tmpPath+ "/tmp_class");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            sb.append(" ");
            sb.append(args.get(i));
        }

        fixedInfo.javacArgs = sb.toString();

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
        kotlinArgs.add(getSupportVersion(javaCompile.getTargetCompatibility()));

        kotlinArgs.add("-d");
        kotlinArgs.add(Settings.env.tmpPath + "/tmp_class");

        StringBuilder sbKotlin = new StringBuilder();
        for (int i = 0; i < kotlinArgs.size(); i++) {
            sbKotlin.append(" ");
            sbKotlin.append(kotlinArgs.get(i));
        }

        fixedInfo.kotlincArgs = sbKotlin.toString();
    }

    private String getSupportVersion(String jvmVersion) {
        if ("1.7".equals(jvmVersion)) {
            return "1.8";
        }

        return jvmVersion;
    }

    private void findModuleTree(Project project, String productFlavor) {
        Settings.env.projectTreeRoot = new Settings.ProjectFixedInfo();

        HashSet<String> hasAddProject = new HashSet<>();
        handleAndroidProject(project, Settings.env.projectTreeRoot, hasAddProject, productFlavor, "debug");

        sortBuildList(Settings.env.projectTreeRoot, Settings.env.projectBuildSortList);
    }

    private boolean isIgnoreProject(String moduleName) {
        LitebuildOptions litebuildOptions = Settings.env.options;
        if (litebuildOptions == null) {
            return false;
        }

        if (litebuildOptions.moduleWhitelist != null
                && litebuildOptions.moduleWhitelist.length > 0) {
            for (String module : litebuildOptions.moduleWhitelist) {
                if (moduleName.equals(module)) {
                    return false;

                }
            }

            return true;
        } else if (litebuildOptions.moduleBlacklist != null
                && litebuildOptions.moduleBlacklist.length > 0) {
            for (String module : litebuildOptions.moduleBlacklist) {
                if (moduleName.equals(module)) {
                    return true;
                }
            }

            return false;
        }

        return false;
    }

    private void sortBuildList(Settings.ProjectFixedInfo node, List<Settings.ProjectFixedInfo> out) {
        for (Settings.ProjectFixedInfo child : node.children) {
            sortBuildList(child, out);
        }

        if (!node.isProjectIgnore) {
            out.add(node);
        }
    }

    private void handleAndroidProject(Project project, Settings.ProjectFixedInfo node,
                                      HashSet<String> hasAddProject, String productFlavor, String buildType) {
        initProjectData(node, project);

        String[] compileNames = new String[] {"compile", "implementation", "api", "debugCompile"};
        for (String name : compileNames) {
            Configuration compile = project.getConfigurations().findByName(name);
            if (compile != null) {
                collectLocalDependency(node, compile, hasAddProject, productFlavor, buildType);
            }
        }
    }

    private void collectLocalDependency(Settings.ProjectFixedInfo node,
                                        Configuration xxxCompile,
                                        HashSet<String> hasAddProject, String productFlavor, String buildType) {
        xxxCompile.getDependencies().forEach(new Consumer<Dependency>() {
            @Override
            public void accept(Dependency dependency) {

                if (dependency instanceof DefaultProjectDependency) {
                    DefaultProjectDependency dp = (DefaultProjectDependency) dependency;
                    // 孩子节点
                    String name = dp.getDependencyProject().getName();
                    if (name.equals("litebuild-gradle-plugin")
                            || name.equals("LiteBuildLib")
                            || hasAddProject.contains(name)) {
                        return;
                    }

                    Settings.ProjectFixedInfo childNode = new Settings.ProjectFixedInfo();
                    node.children.add(childNode);
                    hasAddProject.add(name);

                    handleAndroidProject(dp.getDependencyProject(), childNode,  hasAddProject, productFlavor, buildType);
                }
            }
        });
    }
}
