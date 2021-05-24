package com.immomo.litebuild.util

object Log {

    fun v(tag: String = "momo", map: Map<*, *>) {
        v(tag, "map:")
        map.entries.forEach {
            v(tag, it)
        }
    }

    fun v(tag: String = "momo", kv: Map.Entry<*, *>) {
        v(tag, "  ${kv.key.toString()}:${kv.value}")
    }

    fun v(tag: String = "momo", str: String) {
        println("${tag}: ${str}")
    }
}