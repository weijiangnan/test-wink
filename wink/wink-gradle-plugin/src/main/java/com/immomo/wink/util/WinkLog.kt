package com.immomo.wink.util

import com.immomo.wink.Constant
import com.immomo.wink.Settings

object WinkLog {


    object WinkLogLevel {
        val LOG_LEVEL_NONE = 0

        // 正常信息，比如目前执行哪个阶段
        val LOG_LEVEL_VERBOSE = 1

        // debug信息，比如javac指令
        val LOG_LEVEL_DEBUG= 2

        // 预留，打印所有
        val LOG_LEVEL_ALL= 10
    }

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
    fun vMap(tag: String = Constant.TAG, map: Map<*, *>) {
        v(tag, "map:")
        map.entries.forEach {
            vMapTag(tag, it)
        }
    }

    @JvmStatic
    fun vMapTag(tag: String = Constant.TAG, kv: Map.Entry<*, *>) {
        v(tag, "  ${kv.key.toString()}:${kv.value}")
    }

    @JvmStatic
    fun v(tag: String = Constant.TAG, str: String) {
        winkPrintln("${tag}: $str", WinkLogLevel.LOG_LEVEL_VERBOSE, TEXT_WHITE)
    }

    @JvmStatic
    fun v(str: String) {
        winkPrintln("${Constant.TAG}: $str", WinkLogLevel.LOG_LEVEL_VERBOSE, TEXT_WHITE)
    }

    @JvmStatic
    fun vNoLimit(str: String) {
        println("${Constant.TAG}: $str")
    }

    @JvmStatic
    fun e(tag: String = Constant.TAG, str: String) {
        winkPrintln("${Constant.TAG}: $str", WinkLogLevel.LOG_LEVEL_DEBUG, TEXT_RED)
    }

    @JvmStatic
    fun e(str: String) {
        winkPrintln("${Constant.TAG}: $str", WinkLogLevel.LOG_LEVEL_DEBUG, TEXT_RED)
    }

    @JvmStatic
    fun d(str: String) {
        winkPrintln("${Constant.TAG}: $str", WinkLogLevel.LOG_LEVEL_DEBUG, TEXT_YELLOW)
    }

    @JvmStatic
    fun cyan(tag: String = Constant.TAG, str: String) {
        winkPrintln("${tag}: $str", WinkLogLevel.LOG_LEVEL_DEBUG, TEXT_CYAN)
    }

    @JvmStatic
    fun cyan(str: String) {
        winkPrintln("${Constant.TAG}: $str", WinkLogLevel.LOG_LEVEL_DEBUG, TEXT_CYAN)
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

    @JvmStatic
    fun winkPrintln(msg: String, level: Int, color: String) {
        if (Settings.env.options == null || Settings.env.options!!.logLevel == -1 ) {
            println(color + msg + TEXT_RESET)
        } else {
            if (Settings.env.options!!.logLevel >= level) {
                println(color + msg + TEXT_RESET)
            }
        }
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