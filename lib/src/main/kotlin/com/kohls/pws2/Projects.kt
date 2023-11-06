package com.kohls.pws2

data class ProjectDependency(val name: String)
data class Project(val name: String, val dependencies: Set<ProjectDependency> = emptySet(), val actions: Set<Action> = emptySet())
data class ProjectConfig(val name: String) {

    private val dependencies = mutableSetOf<ProjectDependency>()
    val actions = mutableSetOf<Action>()
    fun configure(): Project = Project(name, dependencies, actions)

    fun dependsOn(name: String) {
        dependencies += ProjectDependency(name)
    }

    inline fun <reified T : Action> action(name: String, noinline config: (T.() -> Unit)? = null) {
        val action = ActionRegistry.create(T::class, name)
        if (config != null && action is T) {
            action.config()
        }
        actions += action as T
    }
}