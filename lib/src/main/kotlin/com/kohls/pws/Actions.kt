package com.kohls.pws

data class ActionDependency(val name: String)
interface Action {
    val name: String
    fun execute(): Unit = TODO("Need to implement")
}