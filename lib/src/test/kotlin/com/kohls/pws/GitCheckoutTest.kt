package com.kohls.pws

import com.kohls.base.Directory
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class GitCheckoutTest : FeatureSpec({

    feature("Git Checkout Action") {
        val targetDirectoryPath: Path = tempdir().toPath()
        val inputPath = targetDirectoryPath.absolutePathString()
        val gitCheckout = GitCheckout(bashScript = createBashScript(inputPath))
        val expectedParameters = Parameters(mutableMapOf("targetDirectoryPath" to targetDirectoryPath))

        scenario("Successful git checkout with specified branch and directory") {
            gitCheckout.apply { this.targetDirectoryPath = inputPath }.perform(Parameters.EMPTY) shouldBe expectedParameters
        }

        scenario("Successful git checkout with missing target directory path in parameters") {
            gitCheckout.perform(Parameters(mutableMapOf("targetDirectoryPath" to inputPath))) shouldBe expectedParameters
        }

        scenario("Failed git checkout with invalid target directory path") {
            shouldThrowExactly<IllegalArgumentException> { GitCheckout(bashScript = createBashScript(inputPath)).perform(Parameters.EMPTY) }
        }
    }
})

fun createBashScript(inputPath: String): BashScript {
    val bashScript: BashScript = mock(BashScript::class.java)
    `when`(bashScript.createExecutableScript(workingDirectory = Directory(inputPath))).thenReturn(mock(ExecutableScript::class.java))
    return bashScript
}

