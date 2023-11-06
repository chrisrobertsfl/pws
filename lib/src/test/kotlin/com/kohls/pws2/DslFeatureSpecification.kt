package com.kohls.pws2

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe

class DslFeatureSpecification : FeatureSpec({
    feature("Simple workspace creation using DSL") {
        scenario("creating a simple workspace with no projects") {
            workspace("simple") shouldBe Workspace("simple")
        }

        scenario("creating a simple workspace with one project") {
            workspace("workspace") {
                project("project")
            } shouldBe Workspace("workspace", setOf(Project("project")))
        }
    }

    feature("Complex workspace creation using DSL") {

        beforeTest {
            ActionRegistry.register(Maven::class) { name -> Maven(name) }
            ActionRegistry.register(NoOp::class) { name -> NoOp(name) }
        }
        scenario("creating a complex project") {
            workspace("complex-workspace") {
                project("complex") {
                    action<Maven>("complex build") {
                        goals("clean")
                    }
                    action<NoOp>("another") {
                        dependsOn("complex build")
                    }
                    dependsOn("projectA")
                    dependsOn("projectB")

                }
            } shouldBe Workspace(
                "complex-workspace", projects = setOf(
                    Project(
                        "complex",
                        setOf(ProjectDependency("projectA"), ProjectDependency("projectB")),
                        setOf(Maven("complex build", goals = mutableListOf("clean")), NoOp("another", dependencies = setOf(ActionDependency("complex build"))))
                    )
                )
            )
        }
    }
})

fun workspace(name: String, block: WorkspaceConfig.() -> Unit = {}): Workspace {
    return WorkspaceConfig(name).apply(block).configure()
}

data class Workspace(val name: String, val projects: Set<Project> = emptySet())

data class WorkspaceConfig(val name: String) {
    val projects = mutableSetOf<Project>()
    fun configure() = Workspace(name, projects)

    fun project(name: String, block: ProjectConfig.() -> Unit = {}) {
        projects += ProjectConfig(name).apply(block).configure()
    }
}