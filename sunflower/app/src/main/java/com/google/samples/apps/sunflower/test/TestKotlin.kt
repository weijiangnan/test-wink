package com.google.samples.apps.sunflower.test

class TestKotlin {

    fun getTitle(): String {
        println("===============")
        var aaa = "123"
        aaa += "xxx"
        aaa += getString1();
        return aaa
    }

    fun getString1(): String {
        return "getString1"

    }
}
