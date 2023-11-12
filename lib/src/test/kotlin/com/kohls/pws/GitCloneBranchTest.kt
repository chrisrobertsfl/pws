package com.kohls.pws

import com.kohls.base.Directory
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

@Ignored
class GitCloneBranchTest : StringSpec({
    "git clone with branch" {
        val action = GitCloneBranch(name = "Git clone branch", branchName = "OMO-1914", repositoryUrl = "git@gitlab.com:kohls/scps/scf/olm/store-fulfillment.git", targetDirectory = Directory("/tmp/store-fulfillment"))
        action.perform() shouldBe true
    }
})

data class GitCloneBranch(override val name : String, val branchName : String = "main", val repositoryUrl : String, val targetDirectory: Directory) : Action {
    override fun perform(parameters: Parameters): Parameters {
        TODO("Not yet implemented")
    }

}

