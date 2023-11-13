package com.kohls.pws

import com.kohls.base.Directory
import org.slf4j.LoggerFactory


// TODO: Unit test!
data class GitClone(override val name: String = generateName(), var overwrite: Boolean = true) : Action {
    lateinit var repositoryUrl: String
    lateinit var targetDirectoryPath: String
    private val logger by lazy { LoggerFactory.getLogger(Maven::class.java) }
    override fun perform(parameters: Parameters): Parameters {
        logger.trace("parameters are {}", parameters)
        handleTargetDirectory()
        val script = BASH_SCRIPT.createExecutableScript()
        script.execute(listOf(repositoryUrl, targetDirectoryPath))
        return Parameters.create("targetDirectoryPath" to targetDirectoryPath)
    }

    private fun handleTargetDirectory() {
        val targetDirectory = Directory(targetDirectoryPath)
        if (targetDirectory.exists()) {
            when (overwrite) {
                true -> targetDirectory.delete()
                false -> throw IllegalStateException("targetDirectory '${targetDirectoryPath}' already exists and cannot be overwritten when overwrite is enabled")
            }
        }
    }

    companion object {
        private val BASH_SCRIPT = BashScript(commandName = "git-clone", body = Body.fromResource("/bash-scripts/git-clone.sh"))
    }

}