package com.kohls.pws.model

import com.kohls.base.Directory
import com.kohls.pws.BashScript
import com.kohls.pws.ExecutableScript
import com.kohls.pws.Parameters
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

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
                GitCloneAction(
                    name = mockk<ActionName>(), repositoryUrl = mockk<GitRepositoryUrl>(), target = target, bashScript = mockk<BashScript>(relaxed = true), overwrite = false
                ).perform(mockk<Parameters>())
            }.message shouldBe "target directory '${target.path}' already exists and cannot be overwritten when overwrite is enabled"
        }

        scenario("Git clone with overwrite false and non-existing target directory") {}
        every { target.exists() } returns false
        performAction(target, overwrite = false) shouldBe expectedParameters
    }
})


fun performAction(target: Directory, overwrite: Boolean = true): Parameters {
    val repositoryUrl = mockk<GitRepositoryUrl>()
    every { repositoryUrl.path } returns "gitlab@dummyRepo"
    val bashScript = mockk<BashScript>(relaxed = true)
    val executableScript = mockk<ExecutableScript>(relaxed = true)
    every { bashScript.createExecutableScript() } returns executableScript
    val parameters = GitCloneAction(
        name = mockk<ActionName>(), repositoryUrl = repositoryUrl, target = target, bashScript = bashScript, overwrite = overwrite
    ).perform(mockk<Parameters>())
    verify { bashScript.createExecutableScript() }
    verify { executableScript.execute(any()) }
    return parameters
}




