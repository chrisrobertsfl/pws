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

class GitCheckoutActionTest : FeatureSpec({
    feature("Git Checkout Action") {

        val target = mockk<Directory>(relaxed = true)
        val name = mockk<ActionName>(relaxed = true)
        val branch = mockk<GitBranch>(relaxed = true)
        val bashScript = mockk<BashScript>(relaxed = true)
        val executableScript = mockk<ExecutableScript>(relaxed = true)
        every { bashScript.createExecutableScript(workingDirectory = target) } returns executableScript

        val expectedParameters = Parameters.create("target" to target)
        scenario("Successful git checkout with specified branch and directory") {
            GitCheckoutAction(name = name, branch = branch, target = target, bashScript = bashScript).perform(Parameters.EMPTY) shouldBe expectedParameters
            verify { bashScript.createExecutableScript(workingDirectory = any()) }
            verify { executableScript.execute(any()) }
        }
        scenario("Successful git checkout with missing target directory path in parameters") {
            GitCheckoutAction(name = name, branch = branch, target = target, bashScript = bashScript).perform(Parameters.create("target" to target)) shouldBe expectedParameters
            verify { bashScript.createExecutableScript(workingDirectory = any()) }
            verify { executableScript.execute(any()) }
        }
        scenario("Failed git checkout with invalid target directory path") {
            shouldThrowExactly<IllegalArgumentException> {
                GitCheckoutAction(name = name, branch = branch, bashScript = bashScript).perform(Parameters.EMPTY)
            }.message shouldBe "Missing target"
        }
    }
})

