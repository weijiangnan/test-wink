package com.immomo.litebuild.util

import com.immomo.litebuild.Constant

object Log {

    // Define color constants
    const val TEXT_RESET = "\u001B[0m"
    const val TEXT_BLACK = "\u001B[30m"
    const val TEXT_RED = "\u001B[31m"
    const val TEXT_GREEN = "\u001B[32m"
    const val TEXT_YELLOW = "\u001B[33m"
    const val TEXT_BLUE = "\u001B[34m"
    const val TEXT_PURPLE = "\u001B[35m"
    const val TEXT_CYAN = "\u001B[36m"
    const val TEXT_WHITE = "\u001B[37m"
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
    fun v(str: String) {
        println("${Constant.TAG}: $str")
    }

    @JvmOverloads
    fun e(tag: String = "momo", str: String) {
        println("${tag}: " + TEXT_RED + str + TEXT_RESET)
    }

    @JvmStatic
    @JvmOverloads
    fun cyan(tag: String = "momo", str: String) {
        println(TEXT_CYAN + "${tag}: " + str + TEXT_RESET)
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