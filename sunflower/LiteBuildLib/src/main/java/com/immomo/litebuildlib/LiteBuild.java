package com.immomo.litebuildlib;

import android.content.Context;

import static com.immomo.litebuildlib.FixDexUtil.getBuildConfigValue;

public class LiteBuild {
    public static void init(Context context) {
        Object version = getBuildConfigValue(context.getPackageName(), "LITEBUILD_VERSION");
        HotFixEngineWrapper.INSTANCE.loadPatch(context);
        LiteBuildResLoader.tryLoad(context);
    }
}
