package com.kohls.pws


data class Project(val name: String = generateName(), val actions: List<Action> = emptyList())
data class ProjectConfig(val name : String) {
    val actions = mutableListOf<Action>()

    inline fun <reified T : Action, reified C : ActionConfiguration<T>> action(name: String = generateName(), noinline configure: C.() -> Unit) {
        val actionInstance = ActionRegistry.create<T, C>(name, configure)
        actions.add(actionInstance)
    }


    inline fun <reified T> action(
        name: String = generateName(),
        noinline configure: T.() -> Unit
    ) where T : Action, T : ActionConfiguration<T> {
        val actionInstance = T::class.constructors.firstOrNull()?.call(name) as? T
            ?: throw IllegalStateException("Could not instantiate action: ${T::class.simpleName}")
        configure(actionInstance)
        actions.add(actionInstance.configure())
    }


    fun configure(): Project {
        return Project(name, actions)
    }
}




