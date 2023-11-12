package com.kohls.pws

import com.kohls.base.Directory
import com.kohls.pws.Action.Companion.generateName
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

data class Maven(override val name: String = generateName(), var pomXmlFile : File? = null, var settingsXmlFile : File? = null, val goals: MutableList<String> = mutableListOf()) : Action {

    private val logger : Logger = LoggerFactory.getLogger(Maven::class.java)
    fun goals(vararg goals: String) {
        this.goals.addAll(goals)
    }

    override fun perform(parameters: Parameters): Parameters {
        logger.info("parameters are $parameters")
        val executableScript = BashScript(commandName = "maven", body = Body.fromResource("/bash-scripts/bash-maven.sh")).createExecutableScript(background = true)
        pomXmlFile = pomXmlFile ?: parameters.getOrThrow<Directory>("targetDirectory").asFile("pom.xml")
        val pomXmlFilePath : String = requireNotNull(pomXmlFile) { "Missing pomXmlFile" }.path
        val settingsXmlFilePath = requireNotNull(settingsXmlFile) { "Missing settingsXmlFile" }.path
        val args = listOf(pomXmlFilePath , settingsXmlFilePath) + goals
        executableScript.execute(args)
        return Parameters.create("logFile" to executableScript.logFile)
    }

}