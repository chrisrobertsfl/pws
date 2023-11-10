package com.kohls.pws2

data class Maven(override val name: String, override var dependencies: Set<ActionDependency> = mutableSetOf(), val goals : MutableList<String> = mutableListOf()) : Action {
    fun dependsOn(name: String) {
        dependencies += ActionDependency(name)
    }

    fun goals(vararg goals : String) {
        this.goals.addAll(goals)
    }
}