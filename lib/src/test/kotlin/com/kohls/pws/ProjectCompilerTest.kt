package com.kohls.pws

import com.kohls.pws.LocalSource
import com.kohls.pws.LookupTable
import com.kohls.pws.Project
import com.kohls.pws.Workspace
import com.kohls.pws.tasks.Maven
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File

class ProjectCompilerTest : StringSpec({

    val task = Maven(
        id = "task-1",
        args = emptyList(),
        variables = mutableMapOf(),
        background = false,
        settingsXmlFilePath = File("/project/settings.xml"),
        pomXmlFilePath = File("/project/pom.xml"),
        validations = emptyList()
    )
    val projectAfter = Project(name = "name", source = LocalSource("/project"), tasks = listOf(task), parallel = true, dependencies = emptyList(), id = "project-1")
    val projectBefore = projectAfter.copy(tasks = listOf(task.copy(pomXmlFilePath = null, settingsXmlFilePath = null)))
    val workspace = Workspace(id = "workspace-1", listOf(projectBefore))
    val lookupTable = LookupTable(workspace)
    "Run project with compiler" {
        projectBefore.compile(lookupTable) shouldBe projectAfter
    }
})


