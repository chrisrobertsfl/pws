package com.kohls.pws.model

import com.kohls.base.Directory
import com.kohls.pws.BashScript
import com.kohls.pws.Body
import com.kohls.pws.Parameters
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlin.io.path.absolutePathString

class GitCheckoutTest : FeatureSpec({
    feature("Git Checkout Action") {
        val targetPath = tempdir().toPath()

        val target = Directory(targetPath.absolutePathString())

        scenario("Successful git checkout with specified branch and directory") {
            val gitCheckout = GitCheckoutAction(
                name = ActionName("Checkout dummy project"), branch = GitBranch("main"), target = target, bashScript = mockk<BashScript>(relaxed = true)
            )
            gitCheckout.perform(Parameters.EMPTY) shouldBe Parameters.create(
                "target" to target
            )
        }
        scenario("Successful git checkout with missing target directory path in parameters") {
            val gitCheckout = GitCheckoutAction(
                name = ActionName("Checkout dummy project"), branch = GitBranch("main"), bashScript = mockk<BashScript>(relaxed = true)
            )
            gitCheckout.perform(Parameters.create("target" to target)) shouldBe Parameters.create(
                "target" to target
            )
        }
        scenario("Failed git checkout with invalid target directory path") {
            shouldThrowExactly<IllegalArgumentException> {
                GitCheckoutAction(
                    name = ActionName("Checkout dummy project"), branch = GitBranch("main"), bashScript = mockk<BashScript>(relaxed = true)
                ).perform(Parameters.EMPTY)
            }.message shouldBe "Missing target"
        }
    }
})

data class GitCheckoutAction(override val name: ActionName, val branch: GitBranch, val target: Directory? = null, val bashScript: BashScript = BASH_SCRIPT) : Action {
    override fun perform(): String {
        TODO("Not yet implemented")
    }

    override fun perform(input: Parameters): Parameters {
        val workingDirectory = target ?: input.getOrThrow("target")
        workingDirectory.existsOrThrow("Invalid targetDirectoryPath: ${workingDirectory.path} does not exist.")
        bashScript.createExecutableScript(workingDirectory = workingDirectory).execute(listOf(branch.name))
        return Parameters.create("target" to workingDirectory)
    }

    companion object {
        private val BASH_SCRIPT = BashScript(commandName = "git-checkout", body = Body.fromResource("/bash-scripts/git-checkout.sh"))
    }
}

data class GitBranch(val name: String)

