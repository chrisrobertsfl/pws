package com.kohls.pws

import com.kohls.pws.tasks.Maven
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File

class LookupSpecification : StringSpec({
    "Make look up the project source by a specific task" {
        val task = Maven(
            id = "task-1",
            args = emptyList(),
            variables = mutableMapOf(),
            background = false,
            settingsXmlFilePath = File("/tmp/settings.xml"),
            pomXmlFilePath = File("/tmp/pom.xml"),
            validations = emptyList()
        )

        val workspace = Workspace(
            id = "workspace-1", listOf(
                Project(name = "name", source = LocalSource(Directory("/project")), tasks = listOf(task), parallel = true, dependencies = emptyList(), id = "project-1")
            )
        )
        val lookupTable = LookupTable(workspace)

        lookupTable.using(task).getProjectSourcePath() shouldBe "/project"
    }
})

