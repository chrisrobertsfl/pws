package com.kohls.pws

import com.kohls.base.Directory
import org.slf4j.LoggerFactory

// TODO: Unit test!
data class GitCheckout(override val name: String = generateName(), var branchName: String = BRANCH) : Action {
    var targetDirectoryPath: String? = null
    private val logger by lazy { LoggerFactory.getLogger(GitCheckout::class.java) }
    override fun perform(parameters: Parameters): Parameters {
        logger.trace("parameters are {}", parameters)
        val workingDirectory = resolveTargetDirectoryPath(parameters)
        val script = BASH_SCRIPT.createExecutableScript(workingDirectory = workingDirectory)
        script.execute(listOf(branchName))
        return Parameters.create("targetDirectoryPath" to workingDirectory.path)
    }

    private fun resolveTargetDirectoryPath(parameters: Parameters): Directory {
        targetDirectoryPath?.let {
            val directory = Directory(it)
            require(directory.exists()) { "Invalid targetDirectoryPath: $it does not exist." }
            return directory
        }
        return requireNotNull(parameters.get<Directory>("targetDirectory")) { "Missing targetDirectoryPath" }
    }

    companion object {
        private val BASH_SCRIPT = BashScript(commandName = "git-checkout", body = Body.fromResource("/bash-scripts/git-checkout.sh"))
        private const val BRANCH = "main"
    }
}