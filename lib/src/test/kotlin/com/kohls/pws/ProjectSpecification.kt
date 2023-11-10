package com.kohls.pws

import com.kohls.base.Directory
import com.kohls.pws.tasks.ConfirmationException
import com.kohls.pws.tasks.Maven
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.io.File

class ProjectSpecification : FeatureSpec({
    feature("Project Compile:") {
        scenario("Successful compilation with substitutions") {
            val task = Maven(id = "task-1", args = emptyList(), variables = mutableMapOf(), background = false, settingsXmlFilePath = null, pomXmlFilePath = null, validations = emptyList())
            val project = Project(id = "id", name = " name", source = LocalSource(Directory("/project")), parallel = true, dependencies = emptyList(), tasks = listOf(task))
            val workspace = Workspace(id = "workspace-1", listOf(project))
            val lookupTable = LookupTable(workspace)
            val expectedProject = project.copy(tasks = listOf(task.copy(settingsXmlFilePath = File("/project/settings.xml"), pomXmlFilePath = File("/project/pom.xml"))))
            project.compile(lookupTable = lookupTable) shouldBe expectedProject
        }
    }
    feature("Project Confirm:") {
        val project = Project(name = "name", source = LocalSource(existingDirectory()), tasks = listOf(), parallel = true, dependencies = emptyList(), id = "project-1")

        scenario("Successful confirmation") {
            project.confirm()
        }

        scenario("Missing name - Blank") {
            shouldThrowExactly<ConfirmationException> {
                project.copy(name = "    ").confirm()
            }.asClue {
                it.message shouldBe "Confirmation failed for project project-1"
                it.errors shouldContainExactlyInAnyOrder listOf(ConfirmationException.Error(text = "Missing name"))
            }
        }

        scenario("Source path does not exist") {
            shouldThrowExactly<ConfirmationException> {
                project.copy(source = LocalSource(path = nonExistingDirectory("/non-existing"))).confirm()
            }.asClue {
                it.message shouldBe "Confirmation failed for project project-1"
                it.errors shouldContainExactlyInAnyOrder listOf(ConfirmationException.Error(text = "Project source path directory could not be found:  /non-existing"))
            }
        }
        scenario("Missing one or more dependencies - Blank") {
            shouldThrowExactly<ConfirmationException> {
                project.copy(dependencies = listOf("A", "  ", "C", "")).confirm()
            }.asClue {
                it.message shouldBe "Confirmation failed for project project-1"
                it.errors shouldContainExactlyInAnyOrder listOf(ConfirmationException.Error(text = "Missing one or more dependencies"))
            }
        }
    }
})