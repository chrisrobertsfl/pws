package com.kohls.pws2

data class NoOp(override val name: String, override var dependencies: Set<ActionDependency> = mutableSetOf(), ) : Action {
    fun dependsOn(name: String) {
        dependencies += ActionDependency(name)
    }
}