package com.immomo.litebuildlib

import android.content.Context

object LiteBuildLib {

    fun init(context: Context) {
        HotFixEngine.loadPatch(context)
        LiteBuildResLoader.tryLoad(context)
    }

}