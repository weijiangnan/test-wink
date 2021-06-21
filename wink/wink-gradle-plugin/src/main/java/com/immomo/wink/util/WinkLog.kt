package com.immomo.wink.util

import com.immomo.wink.Constant
import com.immomo.wink.Settings

object WinkLog {
    object WinkLogLevel {
        // 预留，打印所有
        const val LOG_LEVEL_ALL = 0

        // 正常信息，比如目前执行哪个阶段
        const val LOG_LEVEL_INFO = 4

        // debug信息，比如javac指令
        const val LOG_LEVEL_DEBUG = 3
        const val LOG_LEVEL_NONE = 10
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
        i(tag, "map:")
        map.entries.forEach {
            vMapTag(tag, it)
        }
    }

    @JvmStatic
    fun vMapTag(tag: String = Constant.TAG, kv: Map.Entry<*, *>) {
        i(tag, "  ${kv.key.toString()}:${kv.value}")
    }

    @JvmStatic
    fun i(tag: String = Constant.TAG, str: String) {
        if (Settings.env.options == null || Settings.env.options!!.logLevel == -1) {
            println(TEXT_CYAN + "[${tag}] $str" + TEXT_CYAN)
        } else {
            if (Settings.env.options!!.logLevel >= WinkLogLevel.LOG_LEVEL_INFO) {
                println(TEXT_CYAN + "[${tag}] $str" + TEXT_CYAN)
            }
        }
    }

    @JvmStatic
    fun i(str: String) {
        if (Settings.env.options == null || Settings.env.options!!.logLevel == -1) {
            println(TEXT_CYAN + "[${Constant.TAG}] $str" + TEXT_CYAN)
        } else {
            if (Settings.env.options!!.logLevel >= WinkLogLevel.LOG_LEVEL_INFO) {
                println(TEXT_CYAN + "[${Constant.TAG}] $str" + TEXT_CYAN)
            }
        }
    }

    @JvmStatic
    fun vNoLimit(str: String) {
        println("[${Constant.TAG}] $str")
    }

    @JvmStatic
    fun e(tag: String = Constant.TAG, str: String) {
        if (Settings.env.options == null || Settings.env.options!!.logLevel == -1) {
            println(TEXT_RED + "[${tag}] " + str + TEXT_RESET)
        } else {
            if (Settings.env.options!!.logLevel >= WinkLogLevel.LOG_LEVEL_DEBUG) {
                println(TEXT_RED + "[${tag}] " + str + TEXT_RESET)
            }
        }
    }

    @JvmStatic
    fun e(str: String) {
        if (Settings.env.options == null || Settings.env.options!!.logLevel == -1) {
            println(TEXT_RED + "[${Constant.TAG}] " + str + TEXT_RESET)
        } else {
            if (Settings.env.options!!.logLevel >= WinkLogLevel.LOG_LEVEL_DEBUG) {
                println(TEXT_RED + "[${Constant.TAG}] " + str + TEXT_RESET)
            }
        }
    }

    @JvmStatic
    fun d(str: String) {
        Settings.env.options?.let {
            if (Settings.env.options!!.logLevel >= WinkLogLevel.LOG_LEVEL_DEBUG) {
                println(TEXT_YELLOW + "[${Constant.TAG}] " + str + TEXT_RESET)
            }
        }
    }

    @JvmStatic
    fun cyan(tag: String = Constant.TAG, str: String) {
        Settings.env.options?.let {
            if (Settings.env.options!!.logLevel >= WinkLogLevel.LOG_LEVEL_DEBUG) {
                println(TEXT_CYAN + "[${tag}] " + str + TEXT_RESET)
            }
        }
    }

    @JvmStatic
    fun cyan(str: String) {
        Settings.env.options?.let {
            if (Settings.env.options!!.logLevel >= WinkLogLevel.LOG_LEVEL_DEBUG) {
                println(TEXT_CYAN + "[${Constant.TAG}] " + str + TEXT_RESET)
            }
        }
    }

    @JvmStatic
    fun timerStart(name: String, other: String = ""): TimerLog {
        var log = TimerLog(name, other)
        i(Constant.TAG, " ${log.name} start. $other >>>>>>>>")
        return log
    }

    @JvmStatic
    fun timerStart(name: String): TimerLog {
        return timerStart(name, "")
    }

    class TimerLog(var name: String = "", var other: String = "") {
        var starTime = System.currentTimeMillis()

        fun end(other: String = "") {
            d("$name end, duration: ${System.currentTimeMillis() - starTime}ms. $other <<<<<<<<")
        }

        fun end() {
            end("")
        }
    }
}