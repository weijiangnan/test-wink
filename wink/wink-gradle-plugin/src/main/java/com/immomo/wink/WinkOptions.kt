package com.immomo.wink

import java.io.Serializable

open class WinkOptions(@JvmField
                       var moduleWhitelist: Array<String>? = null,
                       @JvmField
                       var moduleBlacklist: Array<String>? = null,
                       @JvmField
                       var kotlinSyntheticsEnable: Boolean = false) : Serializable {

}