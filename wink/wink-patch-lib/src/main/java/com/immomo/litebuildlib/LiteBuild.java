package com.immomo.litebuildlib;

import android.content.Context;

public class LiteBuild {
    public static void init(Context context) {
        HotFixEngineWrapper.INSTANCE.loadPatch(context);
        LiteBuildResLoader.tryLoad(context);
    }
}
