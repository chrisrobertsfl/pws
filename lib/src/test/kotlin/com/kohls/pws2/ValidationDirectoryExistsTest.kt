package com.kohls.pws2

import com.kohls.pws.existingDirectory
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

@Ignored
class ValidationDirectoryExistsTest : StringSpec({
    "using action:  .git directory exists" {
        val action = ValidDirectoryExists(name = ".git directory exists", directory = existingDirectory("/some/path/.git"))
        action.execute() shouldBe true
    }
})

