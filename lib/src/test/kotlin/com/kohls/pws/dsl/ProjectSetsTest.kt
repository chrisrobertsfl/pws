package com.kohls.pws.dsl

import com.kohls.pws.Parameters
import com.kohls.pws.model.*
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
                        favoriteColor = "red"
                    }
                }
            }
            projectSet shouldBe ProjectSet(
                name = ProjectSetName("simple"),
                projects = setOf(
                    Project(
                        name = ProjectName("project1"), actions = listOf(SimpleAction(ActionName("simple1"), color = "red"))
                    )
                ),
            parameters = Parameters.create("projectSetName" to ProjectSetName("simple")))
            projectSet.projects.first().actions.first().perform() shouldBe "Hello from simple1"
            projectSet.parameters shouldBe Parameters.create("projectSetName" to projectSet.name)
        }
    }
})

data class Simple(override val name: String) : ActionBuilder<SimpleAction> {

    var favoriteColor: String? = null
    override fun build(): SimpleAction {
        return SimpleAction(name = ActionName(name), color = requireNotNull(favoriteColor) { "Missing favorite color" })
    }
}

data class SimpleAction(override val name: ActionName, val color: String) : Action {
    override fun perform(): String {
        return "Hello from ${name.contents}"
    }
}

