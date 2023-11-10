package com.kohls.pws2

import com.kohls.base.Directory
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class GitCloneBranchTest : StringSpec({

    ".git directory exists for given parent directory" {
        val parentDirectory = Directory("/Users/TKMA5QX/projects/pws")
        parentDirectory.append(".git").exists() shouldBe true
    }

    "using action:  .git directory exists for given parent directory" {
        val action = ValidDirectoryExists(name = ".git directory exists", path = "/Users/TKMA5QX/projects/pws/.git")
        action.execute() shouldBe true

    }
})

data class ValidDirectoryExists(override val name : String, override val dependencies : Set<ActionDependency> = emptySet(), val path : String) : Action {
    override fun execute(): Boolean {
        return Directory(path).exists()
    }
}

