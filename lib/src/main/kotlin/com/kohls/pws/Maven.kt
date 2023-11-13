package com.kohls.pws

import com.kohls.base.Directory
import com.kohls.pws.Action.Companion.generateName
import org.slf4j.LoggerFactory

data class Maven(override val name: String = generateName(), val goals: MutableList<String> = mutableListOf()) : Action {
    var pomXmlFilePath: String? = null
    var settingsXmlFilePath: String? = null
    private val logger by lazy { LoggerFactory.getLogger(Maven::class.java) }

    override fun perform(parameters: Parameters): Parameters {
        logger.trace("parameters are {}", parameters)
        val pomXml = resolvePomXmlFilePath(parameters)
        val settingsXml = resolveSettingsXmlFilePath()
        val args: List<Any> = listOf(pomXml, settingsXml) + goals
        val executableScript = BASH_SCRIPT.createExecutableScript(background = true)
        executableScript.execute(args)
        return Parameters.create("logFile" to executableScript.logFile)
    }

    private fun resolvePomXmlFilePath(parameters: Parameters): String = pomXmlFilePath ?: requireNotNull(parameters["targetDirectory"]) { "Missing pomXmlFilePath" }.let {
        val targetDirectory = it as Directory
        val pomFile = targetDirectory.asFile("pom.xml")
        pomFile.path
    }

    private fun resolveSettingsXmlFilePath(): String = requireNotNull(settingsXmlFilePath) { "Missing settingsXmlFilePath" }

    companion object {
        val BASH_SCRIPT = BashScript(commandName = "maven", body = Body.fromResource("/bash-scripts/bash-maven.sh"))
    }

}