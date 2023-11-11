package com.kohls.pws

data class Maven(override val name: String, val goals: MutableList<String> = mutableListOf()) : Action {
    fun goals(vararg goals: String) {
        this.goals.addAll(goals)
    }
}