package com.immomo.litebuild

import java.io.Serializable

open class LitebuildOptions(@JvmField
                            var moduleWhitelist: Array<String>? = null,
                            @JvmField
                            var moduleBlacklist: Array<String>? = null,
                            @JvmField
                            var kotlinSyntheticsEnable: Boolean = false) : Serializable {

}