package com.immomo.wink.tasks;

import com.immomo.wink.util.DownloadUtil;
import com.immomo.wink.util.ZipUtils;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import javax.inject.Inject;

public class WinkJarsTask extends DefaultTask {

    private List<String> downlaodUrls;
    private String dir;

    @Inject
    public WinkJarsTask(String[] urls, String dir) {
        initAction(urls, dir);
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
        }
        File[] files = DownloadUtil.downloadFiles(downlaodUrls,dir);
        unzipFiles(files);
    }


    private void unzipFiles(File[] files){
        for (File file : files) {
            ZipUtils.unZip(file,dir);
        }
    }

    private void initAction(String[] urls, String dir) {
        this.downlaodUrls = new ArrayList<String>();
        this.dir = dir;
        for (String url : urls) {
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            File target = new File(dir + File.pathSeparator + fileName);
            if (!target.exists() || target.isDirectory()) {
                downlaodUrls.add(url);
            }
        }

    }

}
