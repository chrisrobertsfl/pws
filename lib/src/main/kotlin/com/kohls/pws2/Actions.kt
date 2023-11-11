package com.kohls.pws2

data class ActionDependency(val name: String)
interface Action {
    val name: String
    fun execute(): Unit = TODO("Need to implement")
}