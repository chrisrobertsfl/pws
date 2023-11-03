package com.kohls.pws.tasks

import com.kohls.pws.tasks.ConfirmationException.Error
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.io.File

class MavenConfirmTest : StringSpec({

    val originalTask =
        Maven(id = "abc", args = emptyList(), variables = mutableMapOf(), background = false, settingsXmlFilePath = existingPath(), pomXmlFilePath = existingPath(), validations = emptyList())


    "Success" {
        originalTask.confirm() shouldBe originalTask
    }

    "Settings does not exist" {
        shouldThrowExactly<ConfirmationException> {
            originalTask.copy(settingsXmlFilePath = nonExistingPath("/path/settings.xml")).confirm()
        }.asClue {
            it.message shouldBe "Confirmation failed for task abc"
            it.errors shouldContainExactlyInAnyOrder listOf(Error(text = "Maven settings.xml file could not be found:  /path/settings.xml"))
        }
    }

    "Settings is null" {
        shouldThrowExactly<ConfirmationException> {
            originalTask.copy(settingsXmlFilePath = null).confirm()
        }.asClue {
            it.message shouldBe "Confirmation failed for task abc"
            it.errors shouldContainExactlyInAnyOrder listOf(Error(text = "Missing Maven settings.xml file"))
        }
    }

    "Pom does not exist" {
        shouldThrowExactly<ConfirmationException> {
            originalTask.copy(pomXmlFilePath = nonExistingPath("/path/pom.xml")).confirm()
        }.asClue {
            it.message shouldBe "Confirmation failed for task abc"
            it.errors shouldContainExactlyInAnyOrder listOf(Error(text = "Maven pom.xml file could not be found:  /path/pom.xml"))
        }
    }

    "Pom is null" {
        shouldThrowExactly<ConfirmationException> {
            originalTask.copy(pomXmlFilePath = null).confirm()
        }.asClue {
            it.message shouldBe "Confirmation failed for task abc"
            it.errors shouldContainExactlyInAnyOrder listOf(Error(text = "Missing Maven pom.xml file"))
        }
    }

})

fun existingPath(): File {
    val file = Mockito.mock(File::class.java)
    `when`(file.exists()).thenReturn(true)
    return file
}

fun nonExistingPath(path: String): File {
    val file = Mockito.mock(File::class.java)
    `when`(file.exists()).thenReturn(false)
    `when`(file.absolutePath).thenReturn(path)
    return file
}





