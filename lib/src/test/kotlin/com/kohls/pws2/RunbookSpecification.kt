package com.kohls.pws2

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe

class RunbookSpecification : FeatureSpec({
    feature("Runbook creation:") {

        beforeTest {
            ActionRegistry.register(NoOp::class) { NoOp(it) }
        }

        scenario("Project A depends on Project B") {
            val workspace = workspace("workspace") {
                project("A") {
                    action<NoOp>("A1") {
                        dependsOn("A2")
                        dependsOn("A3")
                    }
                    action<NoOp>("A2")
                    action<NoOp>("A3") {
                        dependsOn("A4")
                    }
                    action<NoOp>("A4")
                }
                project("B") {
                    dependsOn("A")
                }
            }
        }
    }
})


