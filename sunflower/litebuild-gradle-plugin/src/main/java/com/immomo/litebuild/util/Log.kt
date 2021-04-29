package com.immomo.litebuild.util

object Log {

    fun v(kv: Map.Entry<*, *>) {
        v(kv.key.toString() + ":" + kv.value)
    }

    fun v(o: Any) {
        v(obj = o)
    }

    fun v(tag: String = "momo", obj: Any) {
        println(String.format("%s: %s", tag, obj.toString()))
    }
}