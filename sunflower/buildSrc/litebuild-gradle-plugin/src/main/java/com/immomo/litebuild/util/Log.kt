package com.immomo.litebuild.util

import com.immomo.litebuild.Constant

object Log {
    @JvmStatic
    fun v(tag: String = "momo", map: Map<*, *>) {
        v(tag, "map:")
        map.entries.forEach {
            v(tag, it)
        }
    }

    @JvmStatic
    fun v(tag: String = "momo", kv: Map.Entry<*, *>) {
        v(tag, "  ${kv.key.toString()}:${kv.value}")
    }

    @JvmStatic
    fun v(tag: String = "momo", str: String) {
        println("${tag}: $str")
    }

    @JvmStatic
    fun timerStart(name: String, other: String = ""): TimerLog {
        var log = TimerLog(name, other)
        v(Constant.TAG, " ${log.name} start. $other >>>>>>>>")
        return log
    }

    @JvmStatic
    fun timerStart(name: String): TimerLog {
        return timerStart(name, "")
    }

    class TimerLog(var name: String = "", var other: String = "") {
        var starTime = System.currentTimeMillis()

        fun end(other: String = "") {
            v(
                Constant.TAG,
                "$name end, duration: ${System.currentTimeMillis() - starTime}. $other <<<<<<<<"
            )
        }

        fun end() {
            end("")
        }
    }
}