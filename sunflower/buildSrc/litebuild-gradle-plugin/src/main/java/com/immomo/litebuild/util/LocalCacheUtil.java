package com.immomo.litebuild.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class LocalCacheUtil {
    private static final String TAG =  "LocalCacheUtil";

    public static <T> T getCache(String filePath) {
        ObjectInputStream ois = null;
        File file = null;
        try {
            file = new File(filePath);
            if (file.exists()) {
                ois = new ObjectInputStream(new FileInputStream(file));
                return (T) ois.readObject();
            }
        } catch (Exception e) {
            delete(file);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 保存文件
     *
     * @param value
     */
    public static  <T> void save2File(T value, String filePath) {
        ObjectOutputStream oo = null;
        try {
            File file = new File(filePath);

            if (!file.exists()) {
                file.createNewFile();
            }
            oo = new ObjectOutputStream(new FileOutputStream(file));
            oo.writeObject(value);
            oo.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (oo != null) {
                    oo.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void delete(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}