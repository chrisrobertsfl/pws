package com.kohls.pws

import com.kohls.base.Directory
import org.slf4j.LoggerFactory
import java.nio.file.Paths

data class Maven(override val name: String = generateName(), val goals: MutableList<String> = mutableListOf()) : Action {
    var pomXmlFilePath: String? = null
    var settingsXmlFilePath: String? = null
    var workingDirectoryPath: String? = null

    private val logger by lazy { LoggerFactory.getLogger(Maven::class.java) }

    override fun perform(parameters: Parameters): Parameters {
        logger.trace("parameters are {}", parameters)
        val workingDirectory = resolveWorkingDirectoryPath()
        val pomXml = resolvePomXmlFilePath(parameters)
        val settingsXml = resolveSettingsXmlFilePath()
        val args: List<Any> = listOf(pomXml, settingsXml) + goals
        val executableScript = BASH_SCRIPT.createExecutableScript(background = true, workingDirectory = workingDirectory)
        executableScript.execute(args)
        return Parameters.create("logFile" to executableScript.logFile)
    }

    private fun resolvePomXmlFilePath(parameters: Parameters): String =
        pomXmlFilePath ?: requireNotNull(parameters.get<String>("targetDirectoryPath")) { "Missing pomXmlFilePath" }.let { Paths.get(it, "pom.xml").toString() }

    private fun resolveSettingsXmlFilePath(): String = requireNotNull(settingsXmlFilePath) { "Missing settingsXmlFilePath" }

    private fun resolveWorkingDirectoryPath() : Directory? = workingDirectoryPath?.let {
        val directory = Directory(it)
        require(directory.exists()) {"Working directory does not exist: ${workingDirectoryPath}"}
        directory
    }

    companion object {
        val BASH_SCRIPT = BashScript(commandName = "maven", body = Body.fromResource("/bash-scripts/bash-maven.sh"))
    }

}