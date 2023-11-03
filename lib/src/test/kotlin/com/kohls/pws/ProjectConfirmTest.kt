package com.kohls.pws

import com.kohls.pws.tasks.ConfirmationException
import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ProjectConfirmTest : StringSpec({
    val originalProject = Project(name = "name", source = LocalSource(existingDirectory()), tasks = listOf(), parallel = true, dependencies = emptyList(), id = "project-1")

    "Missing name - Blank" {
        shouldThrowExactly<ConfirmationException> {
            originalProject.copy(name = "    ").confirm()
        }.asClue {
            it.message shouldBe "Confirmation failed for project project-1"
            it.errors shouldContainExactlyInAnyOrder listOf(ConfirmationException.Error(text = "Missing name"))
        }
    }

    "Source path does not exist" {
        shouldThrowExactly<ConfirmationException> {
            originalProject.copy(source = LocalSource(path = nonExistingDirectory("/non-existing"))).confirm()
        }.asClue {
            it.message shouldBe "Confirmation failed for project project-1"
            it.errors shouldContainExactlyInAnyOrder listOf(ConfirmationException.Error(text = "Project source path directory could not be found:  /non-existing"))
        }
    }
    "Missing one or more dependencies - Blank" {
        shouldThrowExactly<ConfirmationException> {
            originalProject.copy(dependencies = listOf("A", "  ", "C", "")).confirm()
        }.asClue {
            it.message shouldBe "Confirmation failed for project project-1"
            it.errors shouldContainExactlyInAnyOrder listOf(ConfirmationException.Error(text = "Missing one or more dependencies"))
        }
    }
})

fun existingDirectory(): Directory {
    val directory = mock(Directory::class.java)
    `when`(directory.exists()).thenReturn(true)
    return directory
}

fun nonExistingDirectory(path: String): Directory {
    val directory = mock(Directory::class.java)
    `when`(directory.exists()).thenReturn(false)
    `when`(directory.path).thenReturn(path)
    return directory
}


