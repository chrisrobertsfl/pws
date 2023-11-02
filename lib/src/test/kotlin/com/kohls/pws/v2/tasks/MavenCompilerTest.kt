package com.kohls.pws.v2.tasks

import com.kohls.pws.v2.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File

class MavenCompilerTest : StringSpec({

    val task = Maven(id = "", args = emptyList(), variables = mutableMapOf(), background = false, settingsXmlFilePath = File("/project/settings.xml"), pomXmlFilePath = File("/project/pom.xml"), validations = emptyList())
    val workspace = Workspace(
        id = "workspace-1", listOf(
            Project(name = "name", source = LocalSource("/project"), tasks = listOf(task), parallel = true, dependencies = emptyList(), id = "project-1")
        )
    )
    val lookupTable = LookupTable(workspace)
    "Missing pom is derived from project source" { task.copy(settingsXmlFilePath = null).compile(lookupTable) shouldBe task }
    "Missing settings is derived from project source" { task.copy(pomXmlFilePath = null).compile(lookupTable) shouldBe task }
    "Nothing happens when all fields are populated" { task.copy(pomXmlFilePath = null).compile(lookupTable) shouldBe task }
})




