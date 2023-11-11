package com.kohls.pws

import com.kohls.base.Directory
import io.kotest.core.annotation.Ignored
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

@Ignored
class GitCloneBranchTest : StringSpec({
    "git clone with branch" {
        val action = GitCloneBranch(name = "Git clone branch", branchName = "OMO-1914", repositoryUrl = "git@gitlab.com:kohls/scps/scf/olm/store-fulfillment.git", targetDirectory = Directory("/tmp/store-fulfillment"))
        action.execute() shouldBe true
    }
})

data class GitCloneBranch(override val name : String, val branchName : String = "main", val repositoryUrl : String, val targetDirectory: Directory) : Action {
    override fun execute() {
//        val commandLine = buildCommandLine(branchName, repositoryUrl, targetDirectory.path)
//        ProcessBuilder(commandLine)
    }

//    private fun buildCommandLine(args: List<Any>): List<String> = listOf(file.path) + args.map { it.toString() }



}

