package com.kohls.pws

import com.kohls.base.Directory
import com.kohls.pws.model.Action
import com.kohls.pws.model.ActionName
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Paths


data class GitRepositoryUrl(val path: String) {
    init {
        require(path.isNotBlank()) { "Git repository url path cannot be blank ->${path}<-" }
    }
}

data class GitCloneAction(override val name: ActionName, val repositoryUrl: GitRepositoryUrl, private var target: Directory, val bashScript: BashScript, val overwrite: Boolean) : Action {
    override fun perform(): String {
        TODO("Not yet implemented")
    }

    override fun perform(input: Parameters): Parameters {
        if (target.exists()) {
            when (overwrite) {
                true -> target.delete()
                false -> throw IllegalStateException("target directory '${target.path}' already exists and cannot be overwritten when overwrite is enabled")
            }
        }
        return Parameters.create("target" to target)
    }

}

class GitCloneActionTest : FeatureSpec({

    feature("Git Clone Action") {
        val parameters = mockk<Parameters>()

        val target = mockk<Directory>(relaxed = true)
        val expectedParameters = Parameters.create("target" to target)
        scenario("Successful git clone with overwrite true and existing target directory") {
            every { target.exists() } returns true
            every { target.delete() } returns target
            val gitCloneAction = GitCloneAction(
                name = mockk<ActionName>(), repositoryUrl = mockk<GitRepositoryUrl>(), target = target, bashScript = mockk<BashScript>(relaxed = true), overwrite = true
            )
            gitCloneAction.perform(parameters) shouldBe expectedParameters
        }

        scenario("Successful git clone with overwrite true and non-existing target directory") {
            every { target.exists() } returns false
            val gitCloneAction = GitCloneAction(
                name = mockk<ActionName>(), repositoryUrl = mockk<GitRepositoryUrl>(), target = target, bashScript = mockk<BashScript>(relaxed = true), overwrite = true
            )
            gitCloneAction.perform(parameters) shouldBe expectedParameters
        }

        scenario("Git clone with overwrite false and existing target directory") {
            every { target.exists() } returns true

            shouldThrowExactly<java.lang.IllegalStateException> {
                GitCloneAction(
                    name = mockk<ActionName>(),
                    repositoryUrl = mockk<GitRepositoryUrl>(),
                    target = target,
                    bashScript = mockk<BashScript>(relaxed = true),
                    overwrite = false
                ).perform(parameters)
            }.message shouldBe "target directory '${target.path}' already exists and cannot be overwritten when overwrite is enabled"

        }

        scenario("Git clone with overwrite false and non-existing target directory") {}
        every { target.exists() } returns false
        val gitCloneAction = GitCloneAction(
            name = mockk<ActionName>(), repositoryUrl = mockk<GitRepositoryUrl>(), target = target, bashScript = mockk<BashScript>(relaxed = true), overwrite = false
        )
        gitCloneAction.perform(parameters) shouldBe expectedParameters
    }
})



