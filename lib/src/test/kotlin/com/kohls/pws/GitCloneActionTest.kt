package com.kohls.pws

import com.kohls.base.Directory
import com.kohls.pws.model.ActionName
import com.kohls.pws.model.GitCloneAction
import com.kohls.pws.model.GitRepositoryUrl
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class GitCloneActionTest : FeatureSpec({

    feature("Git Clone Action") {

        val target = mockk<Directory>(relaxed = true)
        val expectedParameters = Parameters.create("target" to target)

        scenario("Successful git clone with overwrite true and existing target directory") {
            every { target.exists() } returns true
            every { target.delete() } returns target
            performAction(target) shouldBe expectedParameters
        }

        scenario("Successful git clone with overwrite true and non-existing target directory") {
            every { target.exists() } returns false
            performAction(target) shouldBe expectedParameters
        }

        scenario("Git clone with overwrite false and existing target directory") {
            every { target.exists() } returns true
            shouldThrowExactly<java.lang.IllegalStateException> {
                performAction(target, overwrite = false)
            }.message shouldBe "target directory '${target.path}' already exists and cannot be overwritten when overwrite is enabled"

        }

        scenario("Git clone with overwrite false and non-existing target directory") {}
        every { target.exists() } returns false
        performAction(target, overwrite = false) shouldBe expectedParameters
    }
})


fun performAction(target: Directory, overwrite: Boolean = true) = GitCloneAction(
    name = mockk<ActionName>(), repositoryUrl = mockk<GitRepositoryUrl>(), target = target, bashScript = mockk<BashScript>(relaxed = true), overwrite = overwrite
).perform(mockk<Parameters>())



