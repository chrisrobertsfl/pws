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
            } shouldBe Workspace("workspace", mutableMapOf("project" to Project("project")))
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
                "complex-workspace", projects = mutableMapOf(
                    "complex" to Project(
                        "complex",
                        setOf(ProjectDependency("projectA"), ProjectDependency("projectB")),
                        mapOf("complex build" to Maven("complex build", goals = mutableListOf("clean")), "another" to NoOp("another", dependencies = setOf(ActionDependency("complex build"))))
                    )
                )
            )
        }
    }
})

