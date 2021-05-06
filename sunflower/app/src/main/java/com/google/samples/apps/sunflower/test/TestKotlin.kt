package com.google.samples.apps.sunflower.test

class TestKotlin {

    fun getTitle(): String {
        println("===============")
        var aaa = "222"
        aaa += "333"
        aaa += "444"
        aaa += getString1();
        return aaa
    }

    fun getString1(): String {
        return "555"

    }
}
