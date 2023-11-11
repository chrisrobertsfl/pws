package com.kohls.pws

import java.util.UUID.randomUUID

data class ProjectDependency(val name: String)
data class Project(val name: String = randomUUID().toString(), val actions: List<Action> = emptyList())
data class ProjectConfig(val name: String) {

    val actions = mutableListOf<Action>()
    fun configure(): Project = Project(name, actions)

    inline fun <reified T : Action> action(name: String, noinline config: (T.() -> Unit)? = null) {
        val action = ActionRegistry.create(T::class, name)
        if (config != null && action is T) {
            action.config()
        }
        actions += action as T
    }
}