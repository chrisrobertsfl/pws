package com.kohls.pws.v2

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class WorkspaceBuilderTest : StringSpec({
    "should build a Workspace object with correct properties" {
        val projectBuilder1 = ProjectBuilder().apply { name = "Project1" }
        val projectBuilder2 = ProjectBuilder().apply { name = "Project2" }

        val workspaceBuilder = WorkspaceBuilder().apply {
            targetDirectory("/tmp/workspace")
            projectBuilders.add(projectBuilder1)
            projectBuilders.add(projectBuilder2)
        }

        val workspace: Workspace = workspaceBuilder.build()

        workspace.targetDirectory shouldBe Directory("/tmp/workspace")
        workspace.projects.map { it.name } shouldContainExactlyInAnyOrder listOf("Project1", "Project2")
    }
})