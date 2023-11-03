package com.kohls.pws

import com.kohls.pws.tasks.ConfirmationException
import com.kohls.pws.tasks.ConfirmationException.Error
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class WorkspaceConfirmTest : StringSpec({

    "Dependency does not exist" {
        val workspace = Workspace(id = "workspace-1", projects = listOf(project("A", "D"), project("B", "C")))
        shouldThrowExactly<ConfirmationException> {
            workspace.confirm()
        }.asClue {
            it.message shouldBe "Confirmation failed for workspace workspace-1"
            it.errors shouldContainExactlyInAnyOrder listOf(
                Error(text = "Missing dependency 'C' for Project[id : 'project-B', name : 'B']"), Error(text = "Missing dependency 'D' for Project[id : 'project-A', name : 'A']")
            )
        }
    }
    "Circular dependencies" {} // TODO: test
    "Duplicate projects" {} // TODO: test
})


fun project(x: String, y: String) = Project(id = "project-$x", name = "$x", dependencies = listOf("$y"), source = LocalSource(existingDirectory()), tasks = listOf(), parallel = true)