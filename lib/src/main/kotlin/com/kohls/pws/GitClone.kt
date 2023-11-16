package com.kohls.pws

import com.kohls.base.Directory
import org.slf4j.LoggerFactory


// TODO: Unit test!
data class GitClone(override val name: String = generateName(), var overwrite: Boolean = true) : Action {
    var repositoryUrl: String? = null
    var targetDirectoryPath: String? = null
    private val logger by lazy { LoggerFactory.getLogger(Maven::class.java) }

    override fun perform(parameters: Parameters): Parameters {
        logger.trace("parameters are {}", parameters)
        logger.trace("targetDirectoryPath is {}", targetDirectoryPath)
        executeScript()
        return Parameters.create("targetDirectoryPath" to targetDirectoryPath.orEmpty())
    }

    private fun executeScript() {
        logger.trace("repositoryUrl is {}", repositoryUrl)
        val args = prepareArguments()
        BASH_SCRIPT.createExecutableScript().execute(args)
    }

    private fun prepareArguments(): List<Any> {
        logger.trace("repositoryUrl is {}", repositoryUrl)
        val validatedRepositoryUrl = requireNotNull(repositoryUrl) { "Missing freaking repositoryUrl" }
        val validatedTargetDirectoryPath = requireNotNull(targetDirectoryPath) { "Missing targetDirectoryPath" }
        handleTargetDirectory(validatedTargetDirectoryPath)
        return listOf(validatedRepositoryUrl, validatedTargetDirectoryPath)
    }

    private fun handleTargetDirectory(path: String) {
        val targetDirectory = Directory(path)
        if (targetDirectory.exists()) {
            when (overwrite) {
                true -> targetDirectory.delete()
                false -> throw IllegalStateException("targetDirectory '${path}' already exists and cannot be overwritten when overwrite is enabled")
            }
        }
    }

    companion object {
        private val BASH_SCRIPT = BashScript(commandName = "git-clone", body = Body.fromResource("/bash-scripts/git-clone.sh"))
    }
}