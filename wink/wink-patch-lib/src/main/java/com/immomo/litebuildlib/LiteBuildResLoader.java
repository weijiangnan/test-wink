package com.immomo.litebuildlib;

import android.content.Context;
import android.os.Environment;
import android.util.Log;


import java.io.File;

public class LiteBuildResLoader {

    public static void tryLoad(Context application) {
        try {
            LiteBuildResourcePatcher.isResourceCanPatch(application);
//            String patchVersion = FixDexUtil.getPatchVersion(application);
//            String path = Environment.getExternalStorageDirectory().getAbsolutePath()
//                    + "/Android/data/" + application.getPackageName() + "/patch_file/apk/"
//                    + patchVersion + "_resources-debug.apk";
//            File patchFile = new File(path);
            File patchFile = FixDexUtil.getResourcesPatchFile(application);
            if (patchFile != null && patchFile.exists() && patchFile.canRead()) {
                boolean loadResources = LiteBuildResourcePatcher.monkeyPatchExistingResources(application, patchFile.getAbsolutePath());
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
