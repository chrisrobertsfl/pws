package com.kohls.pws

import com.kohls.pws.Action.Companion.generateName
import java.io.File

data class Maven(override val name: String = generateName(), var pomXmlFile : File? = null, var settingsXmlFile : File? = null, val goals: MutableList<String> = mutableListOf()) : Action {

    fun goals(vararg goals: String) {
        this.goals.addAll(goals)
    }

    override fun perform(parameters: Parameters): Parameters {
        val executableScript = BashScript(commandName = "maven", body = Body.fromResource("/bash-scripts/bash-maven.sh")).createExecutableScript(background = true)
        val pomXmlFilePath = requireNotNull(pomXmlFile) { "Missing pomXmlFile" }.path
        val settingsXmlFilePath = requireNotNull(settingsXmlFile) { "Missing settingsXmlFile" }.path
        val args = listOf(pomXmlFilePath , settingsXmlFilePath) + goals
        executableScript.execute(args)
        return Parameters.create("logFile" to executableScript.logFile)
    }

}