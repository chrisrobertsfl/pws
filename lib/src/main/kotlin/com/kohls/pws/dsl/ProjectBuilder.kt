package com.kohls.pws.dsl

import com.kohls.pws.model.Action
import com.kohls.pws.model.Project
import com.kohls.pws.model.ProjectName

class ProjectBuilder(val name: String) {
    val actions = mutableListOf<Action>()
    fun build(): Project {
        return Project(ProjectName(name), actions)
    }

    inline fun <reified T : ActionBuilder<*>> action(name: String, noinline block: T.() -> Unit) {
        val builder = ActionRegistry.create(T::class, name)
        builder.block()
        actions += builder.build()
    }
}