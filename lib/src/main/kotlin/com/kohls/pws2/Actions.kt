package com.kohls.pws2

data class ActionDependency(val name: String)
interface Action {
    val name: String
    val dependencies: Set<ActionDependency>
    fun execute()
}