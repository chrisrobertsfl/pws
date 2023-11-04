package com.kohls.pws

import com.kohls.pws.tasks.ConfirmationException
import com.kohls.pws.tasks.ConfirmationException.Error
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe

class WorkspaceSpecification : FeatureSpec({

    feature("Workspace Confirm:") {
        scenario("Dependency does not exist") {
            val workspace = Workspace(id = "workspace-1", projects = listOf(project("A", "D"), project("B", "C")))
            shouldThrowExactly<ConfirmationException> {
                workspace.confirm()
            }.asClue {
                it.message shouldBe "Confirmation failed for workspace-1"
                it.errors shouldBe listOf(
                    Error(text = "Missing projects: names ['C', 'D']")
                )
            }
        }
        scenario("Circular dependencies") {
            val workspace = Workspace(id = "workspace-1", projects = listOf(project("A", "B"), project("B", "C"), project("C", "A")))
            shouldThrowExactly<ConfirmationException> {
                workspace.confirm()
            }.asClue {
                it.message shouldBe "Confirmation failed for workspace-1"
                it.errors shouldBe listOf(
                    Error(text = "Circular referencing projects: names ['A', 'B', 'C']")
                )
            }
        }
        scenario("Duplicate project names") {
            val workspace = Workspace(id = "workspace-1", projects = listOf(project("A", "B"), project("A", "C"), project("B"), project("C"), project("C", "D"), project("D")))
            shouldThrowExactly<ConfirmationException> {
                workspace.confirm()
            }.asClue {
                it.message shouldBe "Confirmation failed for workspace-1"
                it.errors shouldBe listOf(
                    Error(text = "Duplicate projects: names ['A', 'C']")
                )
            }
        }
    }
})


