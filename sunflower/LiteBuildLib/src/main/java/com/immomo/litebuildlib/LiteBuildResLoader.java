package com.immomo.litebuildlib;

import android.content.Context;
import android.os.Environment;
import android.util.Log;


import java.io.File;

public class LiteBuildResLoader {

    public static void tryLoad(Context application) {
        try {
            LiteBuildResourcePatcher.isResourceCanPatch(application);
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + application.getPackageName() + "/patch_file/apk/resources-debug.apk";
            File patchFile = new File(path);
            if (patchFile.exists()) {
                boolean loadResources = LiteBuildResourcePatcher.monkeyPatchExistingResources(application, path);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
