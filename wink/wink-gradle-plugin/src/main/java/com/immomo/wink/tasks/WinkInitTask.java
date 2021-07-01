package com.immomo.wink.tasks;

import com.immomo.wink.Constant;
import com.immomo.wink.util.DownloadUtil;
import com.immomo.wink.util.Utils;
import com.immomo.wink.util.ZipUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import javax.inject.Inject;

public class WinkInitTask extends DefaultTask {

    private List<String> downlaodUrls;
    private String projectPath;
    private String dir;
    private String JARS_PATH;

    @Inject
    public WinkInitTask(String[] urls, String projectPath) {
        initAction(urls, projectPath);
    }

    /**
     * Starts downloading
     *
     * @throws IOException if the file could not downloaded
     */
    @TaskAction
    public void download() throws IOException {
        if (downlaodUrls == null || downlaodUrls.size() == 0) {
            System.out.println("文件已存在！");
            return;
        } else {
            File[] files = DownloadUtil.downloadFiles(downlaodUrls, dir);
            unzipFiles(files);
        }
        copyShell();

    }


    private void unzipFiles(File[] files) {
        for (File file : files) {
            ZipUtils.unZip(file, dir);
        }
    }

    private void copyShell() {
        try {
            File shell = new File(getShellPath());
            if (shell.exists() && shell.isFile()) {
                File target = new File(getCopyShellPath());
                if (!target.exists()) {
                    target.createNewFile();
                }
                target.setExecutable(true, false);
                target.setReadable(true, false);
                target.setWritable(true, false);
                Utils.copyFile(shell, target);
            } else {
                System.out.println("wink.shell 文件不存在！");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initAction(String[] urls, String projectPath) {
        this.downlaodUrls = new ArrayList<String>();
        this.projectPath = projectPath;
        this.dir = getJarsPath();
        for (String url : urls) {
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            File target = new File(dir + File.separator + fileName);
            if (!target.exists() || target.isDirectory()) {
                downlaodUrls.add(url);
            }
        }

    }

    private String getJarsPath() {
        return projectPath + File.separator + Constant.IDEA + File.separator + Constant.TAG + File.separator + Constant.JARS;
    }

    private String getShellPath() {
        return projectPath + File.separator + Constant.IDEA + File.separator + Constant.TAG + File.separator + Constant.JARS + File.separator + Constant.SHELL;
    }

    private String getCopyShellPath() {
        return projectPath + File.separator + Constant.SHELL;
    }

}
