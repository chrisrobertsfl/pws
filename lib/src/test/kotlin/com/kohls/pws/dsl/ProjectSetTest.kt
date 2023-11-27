package com.kohls.pws.dsl

import com.kohls.pws.model.Action
import com.kohls.pws.model.ActionName
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe

class ProjectSetTest : FeatureSpec({

    beforeTest {
        ActionRegistry.unregisterAll()
    }

    feature("Creation") {
        scenario("Simple") {
            ActionRegistry.register(Simple::class) { name -> Simple(name) }
            val projectSet = projectSet("simple") {
                project("project1") {
                    action<Simple>("simple1") {

                    }
                }
            }
            projectSet shouldBe ProjectSet(
                name = ProjectSetName("simple"), projects = setOf(
                    Project(
                        name = ProjectName("project1"), actions = listOf(SimpleAction(ActionName("simple1")))
                    )
                )
            )

            projectSet.projects.first().actions.first().perform() shouldBe "Hello from simple1"
        }
    }
})

data class Simple(override val name: String) : ActionBuilder<SimpleAction> {
    override fun build(): SimpleAction {
        return SimpleAction(ActionName(name))
    }
}

data class SimpleAction(override val name: ActionName) : Action {
    override fun perform(): String {
        return "Hello from ${name.contents}"
    }
}

fun projectSet(name: String, block: ProjectSetBuilder.() -> Unit): ProjectSet {
    val builder = ProjectSetBuilder(name)
    return builder.apply(block).build()
}


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

class ProjectSetBuilder(val name: String) {
    val projects = mutableSetOf<Project>()
    fun build(): ProjectSet {
        return ProjectSet(ProjectSetName(name), projects)
    }

    fun project(name: String, block: ProjectBuilder.() -> Unit) {
        val builder = ProjectBuilder(name)
        projects += builder.apply(block).build()
    }
}

data class ProjectSet(val name: ProjectSetName, val projects: Set<Project>)
data class ProjectSetName(val contents: String)
data class Project(val name: ProjectName, val actions: List<Action>)
data class ProjectName(val contents: String)