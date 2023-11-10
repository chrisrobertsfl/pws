package com.kohls.pws.tasks

import com.kohls.base.Directory
import com.kohls.pws.*
import com.kohls.pws.tasks.ConfirmationException.Error
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.io.File

class MavenSpecification : FeatureSpec({

    feature("Maven Compile:") {
        val settingsXmlFilePath = File("/project/settings.xml")
        val pomXmlFilePath = File("/project/pom.xml")
        val task =
            Maven(id = "", args = emptyList(), variables = mutableMapOf(), background = false, settingsXmlFilePath = settingsXmlFilePath, pomXmlFilePath = pomXmlFilePath, validations = emptyList())
        val project = Project(name = "name", source = LocalSource(Directory("/project")), tasks = listOf(task), parallel = true, dependencies = emptyList(), id = "project-1")
        val workspace = Workspace(id = "workspace-1", listOf(project))
        val lookupTable = LookupTable(workspace)

        scenario("Missing pom is derived from project source") { task.copy(settingsXmlFilePath = null).compile(lookupTable) shouldBe task }
        scenario("Missing settings is derived from project source") { task.copy(pomXmlFilePath = null).compile(lookupTable) shouldBe task }
        scenario("Nothing happens when all fields are populated") { task.copy(pomXmlFilePath = null).compile(lookupTable) shouldBe task }


    }

    feature("Maven Confirm:") {
        val task =
            Maven(id = "abc", args = emptyList(), variables = mutableMapOf(), background = false, settingsXmlFilePath = existingFile(), pomXmlFilePath = existingFile(), validations = emptyList())

        scenario("Success") {
            task.confirm() shouldBe task
        }

        scenario("Settings does not exist") {
            shouldThrowExactly<ConfirmationException> {
                task.copy(settingsXmlFilePath = nonExistingFile("/path/settings.xml")).confirm()
            }.asClue {
                it.message shouldBe "Confirmation failed for task abc"
                it.errors shouldContainExactlyInAnyOrder listOf(Error(text = "Maven settings.xml file could not be found:  /path/settings.xml"))
            }
        }

        scenario("Settings is null") {
            shouldThrowExactly<ConfirmationException> {
                task.copy(settingsXmlFilePath = null).confirm()
            }.asClue {
                it.message shouldBe "Confirmation failed for task abc"
                it.errors shouldContainExactlyInAnyOrder listOf(Error(text = "Missing Maven settings.xml file"))
            }
        }

        scenario("Pom does not exist") {
            shouldThrowExactly<ConfirmationException> {
                task.copy(pomXmlFilePath = nonExistingFile("/path/pom.xml")).confirm()
            }.asClue {
                it.message shouldBe "Confirmation failed for task abc"
                it.errors shouldContainExactlyInAnyOrder listOf(Error(text = "Maven pom.xml file could not be found:  /path/pom.xml"))
            }
        }

        scenario("Pom is null") {
            shouldThrowExactly<ConfirmationException> {
                task.copy(pomXmlFilePath = null).confirm()
            }.asClue {
                it.message shouldBe "Confirmation failed for task abc"
                it.errors shouldContainExactlyInAnyOrder listOf(Error(text = "Missing Maven pom.xml file"))
            }
        }
    }

})





