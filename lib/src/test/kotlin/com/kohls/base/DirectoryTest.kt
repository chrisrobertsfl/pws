package com.kohls.base

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe

class DirectoryTest : FeatureSpec({
    lateinit var directory: Directory

    feature("Existence") {
        scenario("When the directory does not exists it should return false") {
            directory = Directory(randomPathName())
            withClue({ "Directory with path ${directory.path} should not exist" }) { directory.exists() shouldBe false }
        }

        scenario("When the directory exists it should return be true") {
            directory = Directory(tempdir().absolutePath)
            withClue({ "Directory with path ${directory.path} should exist" }) { directory.exists() shouldBe true }
        }
    }

    feature("Deletion (Assumption: The directory exists)") {
        scenario("When the directory is empty it should be deleted successfully") {
            directory = Directory(tempdir().absolutePath).delete()
            withClue({ "Directory with path ${directory.path} should be deleted" }) { directory.toFile().exists() shouldBe false }
        }

        scenario("When the directory has other files/directories it should be deleted along with its contents successfully") {
            val baseDirectory = tempdir().also {
                it.addChildDirectory()
                it.addChildFile()
            }

            directory = Directory(baseDirectory.absolutePath).delete()
            withClue({ "Directory with path ${directory.path} should be deleted" }) { directory.toFile().exists() shouldBe false }

        }
    }
})


