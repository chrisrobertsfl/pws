package com.kohls.pws

import com.kohls.base.Directory
import org.slf4j.LoggerFactory

// TODO: Unit test!
data class GitCheckout(override val name: String = generateName(), var branchName: String = BRANCH, val bashScript: BashScript = BASH_SCRIPT) : Action {
    var targetDirectoryPath: String? = null
    private val logger by lazy { LoggerFactory.getLogger(GitCheckout::class.java) }
    override fun perform(parameters: Parameters): Parameters {
        logger.trace("parameters are {}", parameters)
        val workingDirectory = resolveTargetDirectoryPath(parameters)
        bashScript.createExecutableScript(workingDirectory = workingDirectory).execute(listOf(branchName))
        return Parameters.create("targetDirectoryPath" to workingDirectory.path)
    }

    private fun resolveTargetDirectoryPath(parameters: Parameters): Directory {
        val path = targetDirectoryPath ?: parameters.getOrThrow("targetDirectoryPath")
        return Directory(path).existsOrThrow("Invalid targetDirectoryPath: $targetDirectoryPath does not exist.")
    }

    companion object {
        private val BASH_SCRIPT = BashScript(commandName = "git-checkout", body = Body.fromResource("/bash-scripts/git-checkout.sh"))
        private const val BRANCH = "main"
    }
}